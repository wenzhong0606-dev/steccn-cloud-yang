/* 组件增强层：Dialog / Card / Tabs（统一大写命名空间，区别于 state.js 旧小写 helper）。
 * 加载顺序：loader.js（建 Eam）→ state.js → form.js → 本文件 → ui.js → 页面脚本。
 * 全部增强器幂等（dataset 标记防重复）；Tabs 是点击、键盘和ARIA的唯一公共入口。
 * 不创建 Shadow DOM；片段插入普通 DOM，复用全局 Bootstrap 样式。
 */
window.Eam ||= {};
(function (ns) {
  'use strict';
  const nodes=(root,selector)=>[...(root.matches?.(selector)?[root]:[]),...root.querySelectorAll(selector)];
  const listen=(el,type,fn)=>ns.listen?ns.listen(el,type,fn):el.addEventListener(type,fn);

  // ===== Dialog（submitGuard 统一归属 Dialog，§7.4 / §8）===================
  const Dialog = ns.Dialog = ns.Dialog || {};

  // 打开：showModal + 焦点（data-autofocus 优先）；body 滚动锁定由 ui.js 的 dialog 处理负责。
  Dialog.open = function (el, opts = {}) {
    if (!el) return;
    el.showModal();
    if (opts.noFocus) return;
    const focusTarget = el.querySelector('[data-autofocus]') || el.querySelector('input,select,textarea,button:not([type=submit]):not([disabled])');
    if (focusTarget) focusTarget.focus();
  };

  const dialogState=new WeakMap();
  Dialog.requestClose=function(el){
    if(!el?.open)return true;
    const state=dialogState.get(el);
    if(el.querySelector('form[data-busy="true"]'))return false;
    if(state?.dirty&&el.dataset.confirmExit!=='false'){
      if(state.exit)return false;
      const check=document.createElement('dialog');check.className='eam-dialog';check.setAttribute('aria-label','放弃未保存的内容');
      const title=document.createElement('h2');title.textContent='放弃未保存的内容？';
      const message=document.createElement('p');message.textContent='本次输入尚未保存，关闭后将丢弃。';
      const actions=document.createElement('div');actions.className='actions';
      const keep=document.createElement('button');keep.type='button';keep.className='btn btn-outline-secondary';keep.textContent='继续编辑';
      const leave=document.createElement('button');leave.type='button';leave.className='btn btn-danger';leave.textContent='放弃并关闭';
      const finish=discard=>{check.close();check.remove();state.exit=null;if(discard){state.dirty=false;el.close();}else el.querySelector('input,select,textarea,button')?.focus();};
      keep.onclick=()=>finish(false);leave.onclick=()=>finish(true);check.addEventListener('cancel',event=>{event.preventDefault();finish(false);});
      actions.append(keep,leave);check.append(title,message,actions);document.body.append(check);state.exit=check;check.showModal();keep.focus();return false;
    }
    el.close();return true;
  };
  Dialog.close = Dialog.requestClose;

  // 弹窗提交守卫（必修④）：进入即同步 preventDefault（拦截 method="dialog" 默认关闭）；
  // busy 守卫防重复提交；仅 onSubmit 返回 true 才关闭（失败/异常保留已输入内容）。
  Dialog.submitGuard = function (form, onSubmit, opts = {}) {
    if (!form) return;
    if (form.dataset.submitGuardBound === 'true') return; // 幂等
    form.dataset.submitGuardBound = 'true';
    listen(form,'submit', async (e) => {
      e.preventDefault(); // 同步阻止（先于任何 await）
      if (form.dataset.busy === 'true') return;
      if(ns.Form&&!ns.Form.validate(form))return; // 防重复提交
      form.dataset.busy = 'true';
      const submitButtons=[...form.querySelectorAll('[type="submit"]')].map(el=>[el,el.disabled]);
      submitButtons.forEach(([el])=>el.disabled=true);
      try {
        const payload=opts.typed||form.dataset.payload==='json'?ns.Form.collect(form):new FormData(form);
        const result = await onSubmit(payload, form, e);
        if (result === true) {
          const dlg = form.closest('dialog');
          if (dlg) dlg.close(); // 仅成功关闭
        }
        // result !== true：保留弹窗与输入，由 onSubmit 自行提示错误
      } catch (err) {
        // 提交异常（含版本冲突 409 模拟）：不关闭，显示内联错误
        const errEl =
          form.querySelector('[data-errormessage]') ||
          (form.getAttribute('aria-errormessage') && document.getElementById(form.getAttribute('aria-errormessage'))) ||
          form.querySelector('[role="alert"]') ||
          form.querySelector('.field-error');
        if (errEl) errEl.textContent = err && err.message ? err.message : String(err);
      } finally {
        delete form.dataset.busy;
        submitButtons.forEach(([el,disabled])=>el.disabled=disabled);
      }
    });
  };

  // 兼容既有页面的onsubmit：由页面继续决定成功后的业务跳转/关闭，
  // 公共层接管校验、busy与按钮恢复，不改变原回调的数据读取方式。
  Dialog.adaptLegacy=function(root){
    nodes(root||document,'dialog form').forEach(form=>{
      if(form.dataset.submitGuardBound==='true'||typeof form.onsubmit!=='function')return;
      const callback=form.onsubmit;form.onsubmit=null;
      Dialog.submitGuard(form,async(_payload,_form,event)=>{await callback.call(form,event);return false;});
    });
  };

  // 增强单个弹窗：声明 data-submit-guard 的 form 自动接入 submitGuard；
  // onSubmit 通过 form 的 data-onsubmit（函数名）或页面显式调用 submitGuard 提供。
  Dialog.enhance = function (el) {
    if (!el || el.dataset.dialogEnhanced === 'true') return;
    el.dataset.dialogEnhanced = 'true';
    const form = el.querySelector('form');
    const state={opener:null,dirty:false};dialogState.set(el,state);
    listen(el,'beforetoggle',event=>{if(event.newState==='open'){state.opener=document.activeElement;state.dirty=false;}});
    listen(el,'input',()=>{state.dirty=true;});
    listen(el,'change',()=>{state.dirty=true;});
    listen(el,'cancel',event=>{event.preventDefault();Dialog.requestClose(el);});
    const closeClick=event=>{const b=event.target.closest('button');if(!b||b.type==='submit')return;if(b.hasAttribute('data-dialog-close')||/^(关闭|取消)$/.test(b.textContent.trim())){event.preventDefault();event.stopImmediatePropagation();Dialog.requestClose(el);}};
    el.addEventListener('click',closeClick,true);ns.onDispose?.(el,()=>{el.removeEventListener('click',closeClick,true);state.exit?.remove();dialogState.delete(el);});
    listen(el,'close',()=>{if(state.opener?.isConnected)state.opener.focus();state.dirty=false;});
    if (form && form.hasAttribute('data-submit-guard')) {
      const fnName = form.getAttribute('data-onsubmit');
      const onSubmit = fnName && typeof ns[fnName] === 'function' ? ns[fnName] : null;
      if (onSubmit) Dialog.submitGuard(form, onSubmit);
    }
  };

  // 批量增强（由 bootstrap 调用）：仅处理本页 dialog（含 dialog-shell 组件）。
  Dialog.enhanceAll = function (root) {
    nodes(root||document,'dialog').forEach((d) => Dialog.enhance(d));
  };

  // ===== Card（可折叠等，幂等）============================================
  // 仅对声明 data-collapsible 的卡片增强折叠行为；其余 section.card（如 list-panel、
  // 设备导航树）只打 cardEnhanced 标记、不改动其业务结构（避免破坏既有容器）。
  // head/body 用 class 定位（.card-header/.card-body），不被 prefixIds 前缀化影响（前缀只改 id/for/aria/href）。
  const Card = ns.Card = ns.Card || {};
  Card.enhance = function (root) {
    nodes(root||document,'section.card,[data-component="card"],[data-card]').forEach((section) => {
      if (section.dataset.cardEnhanced === 'true') return;
      section.dataset.cardEnhanced = 'true';
      if (!section.hasAttribute('data-collapsible')) return;
      const head = section.querySelector(':scope>.card-title,:scope>.card-header,[data-card-head]');
      const body = section.querySelector(':scope>.card-body,[data-card-body]');
      if (!head || !body) return;
      head.setAttribute('role', 'button');
      head.tabIndex = 0;
      const collapsed = head.hasAttribute('aria-expanded') ? head.getAttribute('aria-expanded')==='false' : section.getAttribute('data-collapse-default') === 'collapsed';
      head.setAttribute('aria-expanded', String(!collapsed));
      if (collapsed) body.hidden = true;
      const toggle = () => {
        const expanded = head.getAttribute('aria-expanded') !== 'false';
        head.setAttribute('aria-expanded', String(!expanded));
        body.hidden = expanded;
      };
      listen(head,'click',e=>{if(e.target!==head&&e.target.closest('button,a,input,select,textarea'))return;toggle();});
      listen(head,'keydown', (e) => {
        if(e.target!==head&&e.target.closest('button,a,input,select,textarea'))return;
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggle(); }
      });
    });
  };

  // ===== Tabs（幂等、限定作用域）=========================
  const Tabs = ns.Tabs = ns.Tabs || {};
  let _tabSeq = 0;
  Tabs.enhance = function (root) {
    nodes(root||document,'.tabs').forEach((group) => {
      // 增强标记防重复绑定，业务控制器可监听eam:tab-select或追加业务适配
      if (group.dataset.tabsEnhanced === 'true') return;
      group.dataset.tabsEnhanced = 'true';
      group.setAttribute('role', 'tablist');
      const tabs = [...group.querySelectorAll('[data-tab]')];
      const scope=group.closest('[data-tabs-scope]')||group.parentElement;
      const paired=new Map();
      // 配对 tab ↔ panel，生成/复用唯一 id（尊重 prefixIds 已前缀化的结果，不覆盖；同页多实例靠计数器防撞）
      tabs.forEach((b, idx) => {
        const name = b.dataset.tab || ('t' + idx);
        const panel = [...scope.querySelectorAll('[data-panel]')].find(p=>p.dataset.panel===name);
        paired.set(b,panel);
        if (!b.id) b.id = 'tab-' + name + '-' + (++_tabSeq);
        b.setAttribute('role', 'tab');
        b.setAttribute('aria-selected', String(b.classList.contains('active')));
        b.tabIndex = b.classList.contains('active') ? 0 : -1; // roving tabindex
        if (panel) {
          if (!panel.id) panel.id = 'panel-' + name + '-' + (++_tabSeq);
          panel.setAttribute('role', 'tabpanel');
          panel.setAttribute('aria-labelledby', b.id);
          b.setAttribute('aria-controls', panel.id);
          b._panelId = panel.id;
          panel.hidden = !b.classList.contains('active'); // 初始显隐与 active 同步（避免业务未设 hidden 时全显示）
        } else {
          b._panelId = null;
        }
      });
      const select = (tab) => {
        if(!tab||tab.hidden||tab.disabled||tab.getAttribute('aria-disabled')==='true')return;
        tabs.forEach((t) => {
          const on = t === tab;
          t.classList.toggle('active', on);
          t.setAttribute('aria-selected', String(on));
          t.tabIndex = on ? 0 : -1;
          const p = paired.get(t);
          if (p) p.hidden = !on;
        });
        tab.focus();
        group.dispatchEvent(new CustomEvent('eam:tab-select',{bubbles:true,detail:{key:tab.dataset.tab,tab,panel:paired.get(tab)}}));
      };
      listen(group,'click', (e) => {
        const b = e.target.closest('[data-tab]');
        if (b) { e.preventDefault(); select(b); }
      });
      listen(group,'keydown', (e) => {
        if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(e.key)) return;
        e.preventDefault();
        const visible = tabs.filter((t) => !t.hidden && !t.disabled);
        const i = visible.indexOf(document.activeElement);
        const next = e.key === 'Home' ? 0
          : e.key === 'End' ? visible.length - 1
          : (i + visible.length + (e.key === 'ArrowLeft' ? -1 : 1)) % visible.length;
        if(visible.length)visible[next].click();
      });
    });
  };
})(window.Eam);
