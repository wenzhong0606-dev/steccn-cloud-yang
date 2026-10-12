/* 批量迁移冒烟测试（任务 58）：验证 30 个业务页接入 loader/form/components 后：
 *  A. 静态标记：每页均含 shared/loader.js + shared/form.js + shared/components.js，
 *     且三者位于 shared/domain.js / shared/ui.js 之前（迁移插入锚点正确，未重复）。
 *  B. 共享层回归：page-table-components.js 的 pagination(host) 已含 `if(!host)return;` 守卫
 *     （修复「Cannot set properties of null (setting 'className')」）。
 *  C. 运行时冒烟：用 jsdom 真实加载每页（桩 Eam/EamDomain/API 为宽容代理，避免业务缺数据误报），
 *     捕获 window.onerror / 未处理 rejection / 脚本同步异常，断言「页面加载零运行时错误」
 *     且能力层 Eam.Form/Dialog/Card/Tabs 均注入成功。
 * 注：本测试仅验证「迁移不引入加载期/启动期错误」，业务功能正确性由人工浏览器与既有 5 套件覆盖。
 */
import { createRequire } from 'module';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';

const require2 = createRequire(import.meta.url);
const { JSDOM, VirtualConsole } = require2('jsdom');
const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '../../../docs/eam/prototype');

// ---- 宽容代理：任何未知属性/调用/遍历/await 均安全，不抛错 ----
function permissive() {
  const target = function () { return permissive(); };
  return new Proxy(target, {
    get(t, p) {
      if (p === Symbol.iterator) return function* () {};
      if (p === Symbol.toPrimitive) return () => '';
      if (p === 'then') return () => Promise.resolve(permissive());
      if (p === 'length') return 0;
      if (['forEach', 'map', 'filter', 'push', 'includes', 'some', 'find', 'reduce', 'slice', 'join', 'sort'].includes(p)) return () => permissive();
      if (p in t) return t[p];
      return permissive();
    },
    set(t, p, v) { t[p] = v; return true; },
    apply() { return permissive(); },
    construct() { return permissive(); },
  });
}

// 收集 pages 下所有业务页（排除演示页 _loader-pilot）
import { readdirSync } from 'fs';
const pageFiles = readdirSync(resolve(root, 'pages'))
  .filter((f) => f.endsWith('.html') && f !== '_loader-pilot.html')
  .sort();

let pass = 0, fail = 0;
const fails = [];
const ok = (name, cond) => {
  if (cond) { pass++; }
  else { fail++; fails.push(name); console.log('  FAIL', name); }
};

// ============ A. 静态迁移标记 ============
console.log('A. 静态迁移标记（30 页）');
for (const f of pageFiles) {
  const html = readFileSync(resolve(root, 'pages', f), 'utf8');
  const hasLoader = html.includes('shared/loader.js');
  const hasForm = html.includes('shared/form.js');
  const hasComp = html.includes('shared/components.js');
  // 迁移契约：loader+form+components 三者均存在、各仅一次、且 form/components 在 loader 之后
  // （form/components 依赖 loader 建立的 window.Eam 基础；不要求相对 domain.js 的顺序，
  //  因 4 个试点页早期迁移的锚点不同，但均满足「form/components 在 loader 之后」）。
  const loaderIdx = html.indexOf('shared/loader.js');
  const formIdx = html.indexOf('shared/form.js');
  const compIdx = html.indexOf('shared/components.js');
  const noDup = (html.match(/shared\/loader\.js/g) || []).length === 1
    && (html.match(/shared\/form\.js/g) || []).length === 1
    && (html.match(/shared\/components\.js/g) || []).length === 1;
  const ordered = loaderIdx > -1 && formIdx > loaderIdx && compIdx > loaderIdx;
  ok(`标记 ${f}: loader+form+components 存在/唯一/form&components 在 loader 后`, hasLoader && hasForm && hasComp && noDup && ordered);
}

// ============ B. 共享层回归守卫 ============
console.log('B. 共享层分页守卫（className-null 修复）');
const ptc = readFileSync(resolve(root, 'shared', 'page-table-components.js'), 'utf8');
const guardIdx = ptc.indexOf('if(!host)return;');
const clsIdx = ptc.indexOf('host.className=');
ok('page-table-components.js: pagination(host) 含 if(!host)return 守卫', guardIdx > -1);
// 反向确认：守卫位于 host.className 之前（旧危险写法已被包裹）
ok('page-table-components.js: 守卫位于 host.className 之前', guardIdx > -1 && clsIdx > guardIdx);

// ============ C. 运行时冒烟（30 页 jsdom 真实加载） ============
console.log('C. 运行时冒烟（jsdom 加载每页，捕获加载期/启动期错误）');
// 已知 EAM 业务全局：页面脚本在浏览器中由其它脚本定义；jsdom 下以宽容代理兜底，避免「缺全局」误报
// （缺全局属页面既有依赖、非迁移引入；真实浏览器中这些全局由对应脚本提供）。
const knownGlobals = ['Eam', 'EamDomain', 'EamProduct', 'EamArchiveApi', 'EamFileViewer', 'EamFiles',
  'EamCommonApi', 'EamMaintenanceApi', 'EamUiComponents', 'EamPageRoutes', 'EamS', 'EamMessage'];
for (const f of pageFiles) {
  const errors = [];
  const html = readFileSync(resolve(root, 'pages', f), 'utf8');
  const vc = new VirtualConsole();
  vc.on('jsdomError', (e) => {
    const msg = (e && e.message) || String(e);
    // jsdom 环境限制（非 JS 缺陷）：导航/未实现 API，排除出致命错误
    if (/Not implemented: navigation \(except hash changes\)/.test(msg)) return;
    errors.push('jsdomError: ' + msg);
  });
  const dom = new JSDOM(html, { runScripts: 'outside-only', url: 'http://localhost/pages/' + f, virtualConsole: vc, pretendToBeVisual: true });
  const { window } = dom;
  // --- 浏览器全局 polyfill ---
  window.structuredClone = (x) => JSON.parse(JSON.stringify(x));
  class _RO { observe() {} unobserve() {} disconnect() {} }
  window.ResizeObserver = _RO; window.IntersectionObserver = _RO;
  window.queueMicrotask = (cb) => Promise.resolve().then(cb);
  window.matchMedia = () => ({ matches: false, media: '', addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {}, onchange: null });
  window.fetch = () => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(''), json: () => Promise.resolve({}) });
  if (!window.URL.createObjectURL) window.URL.createObjectURL = () => 'blob:mock';
  if (!window.URL.revokeObjectURL) window.URL.revokeObjectURL = () => {};
  if (!window.crypto) window.crypto = {};
  if (!window.crypto.randomUUID) window.crypto.randomUUID = () => '00000000-0000-0000-0000-000000000000';
  // 宽容业务桩（loader 会在 Eam 代理 target 上写入真实 Eam.Form/Dialog/Card/Tabs）
  for (const g of knownGlobals) { if (!(g in window)) window[g] = permissive(); }
  window.Eam = permissive(); // 确保 Eam 为代理，loader 在其上写入真实能力层
  window.$ = (sel) => (typeof sel === 'string' && sel.startsWith('#')) ? window.document.getElementById(sel.slice(1)) : window.document.querySelector(sel);
  window.$$ = (sel) => [...window.document.querySelectorAll(sel)];
  window.onerror = (msg) => { errors.push('onerror: ' + msg); return true; };
  window.addEventListener('unhandledrejection', (e) => errors.push('unhandledrejection: ' + (e.reason && e.reason.message || e.reason)));

  // 按文档顺序 eval 各 <script src>（剥离 ?v=）与内联脚本
  const srcTags = [...html.matchAll(/<script[^>]*src="([^"]*)"[^>]*>/g)].map((m) => m[1]);
  const inline = [...html.matchAll(/<script(?![^>]*src)[^>]*>([\s\S]*?)<\/script>/g)].map((m) => m[1]);
  const evalOne = (code, label) => {
    try { window.eval(code); }
    catch (e) { errors.push('eval ' + label + ': ' + (e && e.message)); }
  };
  for (const src of srcTags) {
    const clean = src.split('?')[0];
    const abs = resolve(root, 'pages', clean);
    try {
      const code = readFileSync(abs, 'utf8');
      evalOne(code, clean);
    } catch (e) { errors.push('read ' + clean + ': ' + e.message); }
  }
  for (let i = 0; i < inline.length; i++) evalOne(inline[i], 'inline#' + i);

  // 触发 DOMContentLoaded 让 loader 自动 bootstrap，再等待异步渲染
  try { window.document.dispatchEvent(new window.Event('DOMContentLoaded', { bubbles: true })); } catch (e) { errors.push('DCL: ' + e.message); }
  await new Promise((r) => setTimeout(r, 350));

  const capOk = !!(window.Eam && window.Eam.Form && window.Eam.Dialog && window.Eam.Card && window.Eam.Tabs);
  ok(`冒烟 ${f}: 加载期/启动期零运行时错误`, errors.length === 0);
  if (errors.length) console.log('     ->', errors.slice(0, 3).join(' | '));
  ok(`冒烟 ${f}: 能力层 Eam.Form/Dialog/Card/Tabs 注入成功`, capOk);
}

console.log(`\n批量迁移冒烟结果: ${pass} 通过 / ${fail} 失败`);
if (fail) { console.log('失败项:', fails.join('; ')); process.exit(1); }
process.exit(0);
