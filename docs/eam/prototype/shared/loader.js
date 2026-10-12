/* 组件加载与初始化层（最先加载）。
 * 职责边界：本文件只负责「加载片段 → 填充区域 → 前缀隔离 → 错误重试 → 启动协调」。
 * 增强与业务绑定由 bootstrap 按序调用（ui/decorate、pageHeader、Card/Dialog/Tabs、pageInit），
 * 不在 loadComponent 内做增强，保证「首次加载」与「重试」走同一套实例流程。
 * 不创建 Shadow DOM：片段插入普通 DOM，复用全局 Bootstrap 样式与现有 DOM 查询。
 */
window.Eam ||= {};
(function (ns) {
  'use strict';

  // ---- 请求共享与缓存 ----------------------------------------------------
  // 同一 src 只 fetch 一次；失败的 Promise 必须清除，否则重试仍返回同一次失败。
  const pending=new WeakMap();
  const cache = new Map(); // src -> Promise<string>
  async function fetchText(src) {
    // 组件依赖 fetch()，file:// 下浏览器会阻止本地跨文件请求；必须使用 HTTP(S) 静态服务。
    if (location.protocol === 'file:') {
      throw new Error('当前通过 file:// 打开页面，浏览器禁止读取本地HTML组件。请在 prototype 目录双击“启动原型服务.cmd”，然后通过 http://127.0.0.1:8765/ 访问；或使用 VS Code Live Server。');
    }
    if (cache.has(src)) return cache.get(src);
    const p = fetch(src, { cache: 'no-store' })
      .then((r) => {
        if (!r.ok) throw new Error('HTTP ' + r.status + ' 加载组件失败：' + src);
        return r.text();
      })
      .catch((err) => {
        cache.delete(src); // 关键：失败清缓存，重试才会真正重新请求
        const detail = err?.message || String(err);
        throw new Error('组件请求失败：' + src + '（' + detail + '）。请确认静态服务正在运行、组件URL可访问，并查看浏览器 Network/Console。');
      });
    cache.set(src, p);
    return p;
  }

  // ---- 循环检测：按「DOM 祖先链」判断，与异步时序无关 -------------------
  // 两个兄弟列表同时加载同一片段不应被误判为循环；只有某 host 的 src 出现在它自身的
  // DOM 祖先（已加载组件的 host）的 data-component-src 集合中，才是真正的循环引用。
  function ancestorSrcs(host) {
    const set = new Set();
    let el = host.parentElement;
    while (el) {
      const v = el.getAttribute && el.getAttribute('data-component-src');
      if (v) set.add(new URL(v,document.baseURI).href);
      el = el.parentElement;
    }
    return set;
  }

  // ---- 区域填充：页面 [data-fill] 模板 → 组件 [data-region] ----------------
  // 只处理本实例自身的模板与节点；未知区域 / 重复区域报配置错误，不静默忽略。
  function fillRegions(host) {
    const filled = new Set();
    host.querySelectorAll(':scope > template[data-fill]').forEach((tpl) => {
      const name = tpl.getAttribute('data-fill');
      const region = [...host.querySelectorAll('[data-region]')].find(r=>r.dataset.region===name&&r.closest('[data-component-src]')===host);
      if (!region) {
        reportConfigError(host, '组件缺少区域 [data-region="' + name + '"]，无法注入业务内容');
        return;
      }
      if (filled.has(name)) {
        reportConfigError(host, '区域 "' + name + '" 被多个 [data-fill] 重复注入');
        return;
      }
      filled.add(name);
      region.replaceChildren();
      region.append(tpl.content.cloneNode(true)); // 克隆：模板保留，可重试 / dispose 后重填
    });
    // 声明的必需区域若页面未提供 data-fill：作为加载失败处理（见 loadComponent 的 required 判定）
    host.querySelectorAll('[data-region][data-required="true"]').forEach((region) => {
      if(region.closest('[data-component-src]')!==host)return;
      if (!filled.has(region.getAttribute('data-region'))) {
        reportConfigError(host, '组件必需区域 "' + region.getAttribute('data-region') + '" 未由页面 data-fill 提供');
      }
    });
  }

  function reportConfigError(host, msg) {
    host.dataset.componentConfigError = msg;
    const box = document.createElement('div');
    box.className = 'component-error alert alert-warning';
    box.setAttribute('role', 'alert');
    box.textContent = '组件配置错误：' + msg;
    host.prepend(box);
  }

  // ---- 多实例隔离：id 加前缀并同步所有引用 --------------------------------
  function replaceToken(el, attr, oldId, newId) {
    const v = el.getAttribute(attr);
    if (!v) return;
    const toks = v.split(/\s+/);
    if (toks.includes(oldId)) {
      el.setAttribute(attr, toks.map((t) => (t === oldId ? newId : t)).join(' '));
    }
  }
  function prefixIds(root, prefix) {
    if (root.dataset.prefixed === 'true') return; // 防重复前缀导致 x-x-id
    // 先收集需改写的引用，再改 id，避免自身引用被误判
    const refs = [];
    root.querySelectorAll('[id]').forEach((el) => {
      const oldId = el.id;
      el.dataset.localId ||= oldId;
      const newId = prefix + '-' + oldId;
      root.querySelectorAll('[for="' + oldId + '"]').forEach((l) => refs.push(['for', l, newId]));
      root.querySelectorAll('[aria-labelledby~="' + oldId + '"]').forEach((l) => refs.push(['aria-labelledby', l, newId]));
      root.querySelectorAll('[aria-controls="' + oldId + '"]').forEach((l) => refs.push(['aria-controls', l, newId]));
      root.querySelectorAll('[aria-describedby~="' + oldId + '"]').forEach((l) => refs.push(['aria-describedby', l, newId]));
      root.querySelectorAll('[aria-errormessage~="' + oldId + '"]').forEach((l) => refs.push(['aria-errormessage', l, newId]));
      root.querySelectorAll('a[href="#' + oldId + '"]').forEach((l) => refs.push(['href', l, '#' + newId]));
    });
    root.querySelectorAll('[id]').forEach((el) => {
      el.id = prefix + '-' + el.id;
    });
    for (const [attr, el, val] of refs) {
      if (attr === 'href') el.setAttribute('href', val);
      else replaceToken(el, attr, val.replace(prefix + '-', ''), val);
    }
    root.dataset.prefixed = 'true';
  }

  // ---- 错误 + 重试 UI ----------------------------------------------------
  function renderError(host, err) {
    host.dataset.componentLoaded = 'failed';
    host.dataset.componentError = err.message;
    const box = document.createElement('div');
    box.className = 'component-error alert alert-danger';
    box.setAttribute('role', 'alert');
    const strong = document.createElement('strong');
    strong.textContent = '组件加载失败';
    const p = document.createElement('p');
    p.className = 'small mb-0';
    p.textContent = err.message;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn btn-sm btn-outline-secondary mt-2';
    btn.textContent = '重试';
    btn.onclick = () => {
      box.remove();
      delete host.dataset.componentLoaded;
      delete host.dataset.componentError;
      // 重试只重加载该实例；成功后由 enhance+bind 补做（见 retrySuccess）
      loadComponent(host).then(async inst=>{if(inst.status==='loaded'){const res=await loadComponents(host);enhance(host);await runInits(document,res);ns.Dialog?.adaptLegacy(document);retrySuccess(inst);}});
    };
    box.append(strong, p, btn);
    host.prepend(box);
  }
  function retrySuccess(inst) {
    if (inst.status !== 'loaded') return;
    // 仅该实例：派发事件让页面补做增强与业务绑定，不重跑整页 bootstrap
    host_dispatch(inst.host, 'eam:component-retry', { host: inst.host });
  }
  function host_dispatch(host, type, detail) {
    host.dispatchEvent(new CustomEvent(type, { bubbles: true, detail }));
  }

  // ---- 单实例加载 --------------------------------------------------------
  async function loadComponent(host) {
    if(host.getAttribute('data-component-loaded')==='loaded')return {host,status:'loaded',src:host.dataset.componentSrc};
    if(pending.has(host))return pending.get(host);
    const promise=performLoad(host);pending.set(host,promise);try{return await promise;}finally{pending.delete(host);}
  }
  async function performLoad(host) {
    const src = host.dataset.componentSrc;
    if (!src) {
      host.dataset.componentLoaded = 'loaded';
      return { host, status: 'loaded' };
    }
    if (ancestorSrcs(host).has(new URL(src,document.baseURI).href)) {
      const err = new Error('循环引用组件：' + src);
      renderError(host, err);
      return { host, status: 'failed', error: err };
    }
    try {
      const html = await fetchText(src);
      const tpl = document.createElement('template');
      tpl.innerHTML = html;
      // 保留 host 既有 data-component 等标记（增强器依赖），仅追加加载来源与完成标记
      host.dataset.componentLoaded = 'loaded';
      // 骨架插入到 host 开头；业务 [data-fill] 模板在 host 内、惰性，填充时克隆进 region
      for(const n of [...host.childNodes])if(!(n.nodeType===1&&n.matches('template[data-fill]')))n.remove();
      delete host.dataset.componentConfigError;delete host.dataset.prefixed;
      host.insertBefore(tpl.content, host.firstChild);
      fillRegions(host);
      if(host.dataset.componentConfigError)throw new Error(host.dataset.componentConfigError);
      const inst = host.dataset.instance;
      if (inst) prefixIds(host, inst);
      return { host, status: 'loaded', src };
    } catch (err) {
      renderError(host, err);
      return { host, status: 'failed', error: err };
    }
  }

  // ---- 递归加载（含嵌套组件）+ 实例状态收集 ------------------------------
  async function loadComponents(root) {
    const instances = new Map();
    const queue = [...root.querySelectorAll('[data-component-src]:not([data-component-loaded])')];
    if(root.matches?.('[data-component-src]:not([data-component-loaded])'))queue.unshift(root);
    let i = 0;
    while (i < queue.length) {
      const host = queue[i++];
      if (host.hasAttribute('data-component-loaded')) continue; // 已加载（含失败）跳过
      const inst = await loadComponent(host);
      instances.set(host, inst);
      if (inst.status === 'loaded') {
        // 嵌套组件：片段/填充后新生的 host 需继续加载
        host.querySelectorAll('[data-component-src]:not([data-component-loaded])').forEach((h) => queue.push(h));
      }
    }
    const failed = [...root.querySelectorAll('[data-component-loaded="failed"]')];
    if(root.matches?.('[data-component-loaded="failed"]'))failed.unshift(root);
    return { ok: failed.length === 0, instances, failed };
  }

  // ---- 唯一启动流程 ------------------------------------------------------
  // 顺序：loadComponents → fillRegions(内部) → 各增强器 → 页面 init。
  // 增强与绑定不在 loadComponent 内执行，保证首加载/重试同一流程。
  // 幂等：自动启动（DOMContentLoaded）与页面显式调用可能并发/重复触发，
  //   若不拦截会导致同一 host 被多次插入片段（id 重复、前缀失效）。用单一 Promise 合并。
  const cleanups=new WeakMap();
  ns.find=(root,id)=>[...(root.matches?.('[id]')?[root]:[]),...root.querySelectorAll('[id]')].find(el=>el.id===id||el.dataset.localId===id)||null;
  ns.onDispose=(el,fn)=>{let callbacks=cleanups.get(el);if(!callbacks)cleanups.set(el,callbacks=[]);callbacks.push(fn);};
  ns.listen=(el,type,fn,options)=>{el.addEventListener(type,fn,options);ns.onDispose(el,()=>el.removeEventListener(type,fn,options));};
  const inits = [];
  const initialized=new Set();
  async function runInits(root,res){
    if(document.querySelector('[data-component-required="true"][data-component-loaded="failed"]'))return;
    for(const fn of inits)if(!initialized.has(fn)){await fn(root,res);initialized.add(fn);const main=document.querySelector('main[data-component-booting]');if(main)main.inert=true;}
  }
  function enhance(root){ns.Card?.enhance(root);ns.Tabs?.enhance(root);ns.Dialog?.enhanceAll(root);window.EamUiComponents?.decorateTables();}

  let bootPromise = null;
  ns.registerInit = (fn) => {
    if (typeof fn === 'function') inits.push(fn);
  };
  ns.bootstrap = async function (root, pageInit) {
    if (bootPromise) return bootPromise; // 已启动则复用同一结果，避免双重加载
    const r = root || document;
    const main=r.querySelector?.('main');
    const previousInert=main?.inert||false;
    if(main){main.inert=true;main.dataset.componentBooting='true';main.setAttribute('aria-busy','true');}
    bootPromise = (async () => {
      const res = await loadComponents(r);
      // 增强器（已在各公共脚本内，由 bootstrap 触发，而非 loadComponent 内）
      if (typeof ns.ui?.init === 'function') ns.ui.init(r);
      if (typeof window.EamUiComponents?.pageHeader === 'function') window.EamUiComponents.pageHeader(r);
      // 组件增强（§3：加载→填充→前缀→增强→页面 init）：均带 ?. 保护，未加载对应模块则跳过
      if (typeof ns.Card?.enhance === 'function') ns.Card.enhance(r);
      if (typeof ns.Tabs?.enhance === 'function') ns.Tabs.enhance(r);
      if (typeof ns.Dialog?.enhanceAll === 'function') ns.Dialog.enhanceAll(r);
      await runInits(r,res); // 页面 init（延后到组件加载后）
      if (typeof pageInit === 'function'&&!r.querySelector('[data-component-required="true"][data-component-loaded="failed"]')) await pageInit(res);
      ns.Dialog?.adaptLegacy(r);
      return res;
    })().finally(()=>{if(main){main.inert=previousInert;delete main.dataset.componentBooting;main.removeAttribute('aria-busy');}});
    return bootPromise;
  };

  // 暴露底层 API（组件 / 页面按需调用）
  ns.loadComponents = loadComponents;
  ns.loadComponent = loadComponent;
  ns.fillRegions = fillRegions;
  ns.prefixIds = prefixIds;
  ns.dispose = function (root) {
    // 仅移除 / 替换节点时调用；普通关闭弹窗不调用（保留实例）。
    // 销毁必须同步清除初始化标记，避免二次增强误判已增强。
    // 同时清除 root 自身（root 即组件 host 时，其 data-component-src 不在子孙查询结果内）。
    const targets = [root, ...root.querySelectorAll('*')];
    targets.forEach((h) => {
      for(const fn of cleanups.get(h)||[])fn();cleanups.delete(h);
      host_dispatch(h, 'eam:dispose', { host: h });
      for(const key of ['cardEnhanced','tabsEnhanced','dialogEnhanced','submitGuardBound'])delete h.dataset[key];
      delete h.dataset.componentLoaded;
      delete h.dataset.prefixed;
      delete h.dataset.componentConfigError;
    });
  };

  // 弹窗提交守卫（submitGuard）已迁移至 shared/components.js 的 Eam.Dialog（§8 要求归属 Dialog）。
  // 本文件只保留加载层职责（获取/填充/隔离/重试/启动协调）。

  // 页面就绪后自动启动（仅引入本脚本的页面生效；未引入的页面保持原有立即执行，不受影响）
  // 测试可通过 window.__EamNoAuto=true 关闭自动启动，改为显式调用 bootstrap，避免并发双跑。
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {if(!window.__EamNoAuto)ns.bootstrap(document);});
  } else if (!window.__EamNoAuto) {
    ns.bootstrap(document);
  }
})(window.Eam);
