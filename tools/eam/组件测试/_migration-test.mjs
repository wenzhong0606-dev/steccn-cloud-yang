/* Step 5 迁移回归：验证"真实页面接入 loader 后现有 tab/panel/dialog/Card 行为不回归"。
 * 模式：bootstrap加载完成后由Eam.Tabs统一绑定；
 *       重复bootstrap不重复绑定，页签行为由同一增强器负责。
 * 用 jsdom + 桩 DOM 真实执行（createRequire 解析 jsdom，未污染仓库）。
 */
import { createRequire } from 'module';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';

const require2 = createRequire(import.meta.url);
const { JSDOM } = require2('jsdom');
const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '../../../docs/eam/prototype');

const html = `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"></head><body data-page="inspection" data-layout="detail">
<div class="appbar"><div class="controls"></div><button id="view-toggle" type="button"></button></div>
<div class="review-panel"></div>
<aside id="module-nav"></aside>
<main>
  <div class="title"><div><h1>点检与专项检查</h1></div></div>
  <!-- inspection 风格：单组 tabs + 两 panel -->
  <div class="tabs"><button type="button" data-tab="routine" class="active">例行任务</button><button type="button" data-tab="special">专项/临时检查</button></div>
  <section class="card" data-panel="routine"><h2>例行</h2><p>例行内容</p></section>
  <section class="card" data-panel="special" hidden><h2>专项</h2><p>专项内容</p></section>
  <!-- device 风格：多组 tabs + panel（不同容器） -->
  <div class="tabs"><button type="button" data-tab="overview" class="active">概况</button><button type="button" data-tab="state">状态与许可</button></div>
  <div data-panel="overview">概况内容</div>
  <div data-panel="state" hidden>状态内容</div>
  <!-- 可折叠卡（Card.enhance 应增强） -->
  <section class="card" data-collapsible><div class="card-header">可折叠档案卡</div><div class="card-body">卡体内容</div></section>
  <!-- 弹窗（Dialog.enhanceAll 应打标，无 data-submit-guard 不接入 submitGuard） -->
  <dialog id="dlg1" aria-labelledby="dlg1-h"><h2 id="dlg1-h">弹窗</h2><form><button type="submit">确定</button></form></dialog>
</main>
</body></html>`;

const dom = new JSDOM(html, { runScripts: 'outside-only', url: 'http://localhost/' });
const { window } = dom;
const { document } = window;

// stub 必要环境（ui.js 用 matchMedia / sessionStorage；jsdom 缺 matchMedia）
window.matchMedia = () => ({ matches: false, media: '', addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {}, onchange: null });
// 阻止 loader 自动 bootstrap，手动控制；并提供 ui.js/interactions.js 所需最小 Eam 辅助
window.__EamNoAuto = true;
window.Eam ||= {};
window.Eam.s = { restrictions: [], person: 'tester', drafts: {} };
window.Eam.can = () => true;
window.Eam.text = () => {};
window.Eam.value = () => '';
window.Eam.run = () => true;
window.Eam.attach = () => {};
window.Eam.confirm = () => true;
// interactions.js / page-table-components.js 顶层依赖 domain（演示桩，无后端）
window.EamDomain = {
  operating: () => '在用',
  health: () => '良好',
  personName: () => '测试人',
  people: [],
  initial: () => ({ restrictions: [], person: 'tester', drafts: {}, version: 1 }),
  command: () => {},
};

const load = (f) => window.eval(readFileSync(resolve(root, 'shared/' + f), 'utf8'));
// 加载顺序同真实页面：loader → state(此处用最小 stub 替代) → form → components → ui → interactions
load('loader.js');
load('form.js');
load('components.js');
window.EamPageRoutes = window.EamPageRoutes || {}; // page-table-components 的 pageHeader 依赖
load('page-table-components.js'); // 真实页面在 ui.js 之前；提供 EamUiComponents（decorateTables 等）
load('ui.js');        // 注册组件就绪后的公共初始化
load('interactions.js'); // 不再重复注册Tabs行为

let pass = 0, fail = 0;
const ok = (name, cond) => { if (cond) { pass++; console.log('  PASS', name); } else { fail++; console.log('  FAIL', name); } };

(async () => {
  // 1 ui.js 装饰生效：tabs 组被设 role=tablist
  const groups = [...document.querySelectorAll('.tabs')];
  await window.Eam.bootstrap(document);
  ok('统一Tabs: 所有 .tabs 组 role=tablist', groups.length === 2 && groups.every(g => g.getAttribute('role') === 'tablist'));

  // 2 手动 bootstrap：Eam.Tabs 应因 role=tablist 已设而避让（不重复设 / 不绑切换）
  await window.Eam.bootstrap(document);
  ok('Eam.Tabs 统一增强且重复启动幂等',
    groups.every(g => g.dataset.tabsEnhanced === 'true'));
  ok('统一Tabs: 保留role=tablist', groups.every(g => g.getAttribute('role') === 'tablist'));

  // 3 点击 special tab → interactions.js 切 panel（active + hidden 同步），ui.js 更新 aria
  document.querySelector('[data-tab="special"]').click();
  const specialPanel = document.querySelector('[data-panel="special"]');
  const routinePanel = document.querySelector('[data-panel="routine"]');
  ok('切 panel: special 显示', specialPanel.hidden === false);
  ok('切 panel: routine 隐藏', routinePanel.hidden === true);
  ok('切 panel: special tab 有 active 类', document.querySelector('[data-tab="special"]').classList.contains('active'));
  ok('切 panel: routine tab 无 active 类', !document.querySelector('[data-tab="routine"]').classList.contains('active'));
  ok('切 panel: special tab aria-selected=true', document.querySelector('[data-tab="special"]').getAttribute('aria-selected') === 'true');
  ok('切 panel: routine tab aria-selected=false', document.querySelector('[data-tab="routine"]').getAttribute('aria-selected') === 'false');

  // 4 迁移保留全局 handler 既有行为（interactions.js 的 onclick 为 document 级 [data-panel] 切换，非按组作用域）
  //   注意：真实 device.html 有 7 个 .tabs 组共用 data-panel，迁移前即不具备"组间隔离"——Step 5 仅接入 loader，
  //         行为与原页面一致，未引入回归、也未顺带重构。该既有特性在 §20 单独记录。
  document.querySelector('[data-tab="state"]').click();
  ok('全局 handler 保留: device state panel 显示', document.querySelector('[data-panel="state"]').hidden === false);
  ok('全局 handler 保留: device overview panel 隐藏', document.querySelector('[data-panel="overview"]').hidden === true);
  ok('全局 handler 保留: state tab active + aria-selected=true',
    document.querySelector('[data-tab="state"]').classList.contains('active') &&
    document.querySelector('[data-tab="state"]').getAttribute('aria-selected') === 'true');
  ok('全局 handler 保留: overview tab 无 active + aria-selected=false',
    !document.querySelector('[data-tab="overview"]').classList.contains('active') &&
    document.querySelector('[data-tab="overview"]').getAttribute('aria-selected') === 'false');

  // 5 Card.enhance：可折叠卡增强且不破坏非折叠 section.card
  const collapsible = document.querySelector('section.card[data-collapsible]');
  ok('Card: 可折叠卡已增强标记', collapsible.dataset.cardEnhanced === 'true');
  ok('Card: 可折叠卡 head 为 button(role)', collapsible.querySelector('.card-header').getAttribute('role') === 'button');
  const body = collapsible.querySelector('.card-body');
  const before = body.hidden;
  collapsible.querySelector('.card-header').click();
  ok('Card: 点击 head 切换 body 显隐', body.hidden === !before);
  // 非折叠 section.card（routine/special panel）只打标、不 hidden、不破坏
  ok('Card: 非折叠 panel 仅打标不打 hidden', routinePanel.dataset.cardEnhanced === 'true' && routinePanel.hidden === true /* hidden 由 tab 状态决定，非 Card */);

  // 6 Dialog.enhanceAll：打标但不接入 submitGuard（无 data-submit-guard）
  const dlg = document.getElementById('dlg1');
  ok('Dialog: 弹窗已增强标记', dlg.dataset.dialogEnhanced === 'true');
  ok('Dialog: 无 data-submit-guard 不接入 submitGuard', dlg.querySelector('form').dataset.submitGuardBound === undefined);

  console.log(`\n迁移回归结果: ${pass} 通过 / ${fail} 失败`);
  process.exit(fail ? 1 : 0);
})().catch(e => { console.error('测试异常:', e); process.exit(2); });
