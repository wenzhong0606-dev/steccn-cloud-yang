// §11 Step 4 · Tabs 行为契约（jsdom + 桩 DOM）
// 验证：多实例 id 唯一无冲突、panel 实际 id 配对(aria-controls/labelledby)、
//       active 初始态、点击切换 active+panel 显隐、键盘导航(左/右/Home/End)、
//       尊重前缀化已有 id(不覆盖)、不破坏既有 .tabs、增强幂等。
import { createRequire } from 'module';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';

const require = createRequire(import.meta.url);
const { JSDOM } = require('jsdom');
const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '../../../docs/eam/prototype');

let pass = 0, fail = 0;
const results = [];
function assert(name, cond, extra = '') {
  if (cond) { pass++; results.push('PASS ' + name); }
  else { fail++; results.push('FAIL ' + name + (extra ? ' ' + extra : '')); }
}

const loaderSrc = readFileSync(resolve(root, 'shared/loader.js'), 'utf8');
const formSrc = readFileSync(resolve(root, 'shared/form.js'), 'utf8');
const componentsSrc = readFileSync(resolve(root, 'shared/components.js'), 'utf8');

const dom = new JSDOM('<!doctype html><html><body><main id="root"></main></body></html>',
  { runScripts: 'outside-only', url: 'http://localhost/' });
const { window } = dom;
const { document } = window;
window.__EamNoAuto = true; // 关闭自动启动，手动 bootstrap 以便精确断言

window.eval(loaderSrc);
window.eval(formSrc);
window.eval(componentsSrc);

const rootEl = document.getElementById('root');

(async () => {
  // 两个隔离的 tab 容器 a/b（同名 data-tab，验证多实例隔离）
  rootEl.innerHTML = `
    <div class="tabbed" data-tabs-demo="a">
      <div class="tabs"><button type="button" data-tab="routine" class="active">例行任务</button><button type="button" data-tab="special">专项检查</button></div>
      <section class="card" data-panel="routine"><p>a 例行面板</p></section>
      <section class="card" data-panel="special" hidden><p>a 专项面板</p></section>
    </div>
    <div class="tabbed" data-tabs-demo="b">
      <div class="tabs"><button type="button" data-tab="routine" class="active">例行任务</button><button type="button" data-tab="special">专项检查</button></div>
      <section class="card" data-panel="routine"><p>b 例行面板</p></section>
      <section class="card" data-panel="special" hidden><p>b 专项面板</p></section>
    </div>
    <div class="tabs" id="legacy-tabs"><button type="button" data-tab="x">仅导航</button></div>
  `;

  await window.Eam.bootstrap(rootEl);

  const aGroup = document.querySelector('[data-tabs-demo="a"] .tabs');
  const bGroup = document.querySelector('[data-tabs-demo="b"] .tabs');
  const aTabs = [...aGroup.querySelectorAll('[data-tab]')];
  const bTabs = [...bGroup.querySelectorAll('[data-tab]')];
  const aIds = aTabs.map((b) => b.id);
  const bIds = bTabs.map((b) => b.id);

  // 1 多实例 id 唯一
  assert('Tabs: a 组 id 唯一', new Set(aIds).size === aIds.length, 'ids=' + aIds);
  assert('Tabs: b 组 id 唯一', new Set(bIds).size === bIds.length, 'ids=' + bIds);
  assert('Tabs: 跨组 id 无冲突', !aIds.some((id) => bIds.includes(id)));

  // 2 panel 实际 id 配对
  const pairedOk = (tabs) => tabs.every((t) => {
    const pid = t.getAttribute('aria-controls');
    const p = pid && document.getElementById(pid);
    return p && p.getAttribute('role') === 'tabpanel' && p.getAttribute('aria-labelledby') === t.id;
  });
  assert('Tabs: a 组 panel 配对(tabpanel+aria)', pairedOk(aTabs));
  assert('Tabs: b 组 panel 配对(tabpanel+aria)', pairedOk(bTabs));

  // 3 active 初始态
  const aRoutine = aTabs[0], aSpecial = aTabs[1];
  const aRoutinePanel = document.getElementById(aRoutine.getAttribute('aria-controls'));
  const aSpecialPanel = document.getElementById(aSpecial.getAttribute('aria-controls'));
  assert('Tabs: active tab aria-selected=true', aRoutine.getAttribute('aria-selected') === 'true' && aSpecial.getAttribute('aria-selected') === 'false');
  assert('Tabs: 初始显隐与 active 同步', aRoutinePanel.hidden === false && aSpecialPanel.hidden === true);
  assert('Tabs: roving tabindex(active=0,其他=-1)', aRoutine.tabIndex === 0 && aSpecial.tabIndex === -1);

  // 4 点击切换
  aSpecial.click();
  assert('Tabs: 点击 special → special active', aSpecial.classList.contains('active') && aSpecial.getAttribute('aria-selected') === 'true');
  assert('Tabs: 点击 special → panel 显隐切换', aSpecialPanel.hidden === false && aRoutinePanel.hidden === true);
  aRoutine.click(); // 还原

  // 5 键盘导航：右箭头
  aRoutine.focus();
  aRoutine.dispatchEvent(new window.KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
  assert('Tabs: 右箭头 → special active', aSpecial.classList.contains('active') && aRoutine.classList.contains('active') === false);
  // 6 键盘导航：Home/End
  aRoutine.dispatchEvent(new window.KeyboardEvent('keydown', { key: 'Home', bubbles: true }));
  assert('Tabs: Home → routine active', aRoutine.classList.contains('active'));
  aRoutine.dispatchEvent(new window.KeyboardEvent('keydown', { key: 'End', bubbles: true }));
  assert('Tabs: End → special active', aSpecial.classList.contains('active'));
  aRoutine.click(); // 还原为 routine active

  // 7 不破坏既有 .tabs（无对应 panel 也安全）
  const legacy = document.getElementById('legacy-tabs');
  assert('Tabs: 既有无 panel 的 .tabs 仍 role=tablist(不报错)', legacy.getAttribute('role') === 'tablist' && legacy.querySelector('[data-tab]').getAttribute('role') === 'tab');

  // 8 尊重前缀化已有 id（组件化加载后 prefixIds 已给 tab/panel 加前缀，enhance 不覆盖）
  const fx = document.createElement('div');
  fx.innerHTML = `<div class="tabs" id="fg"><button id="a-routine" data-tab="routine" class="active">例行</button><button id="a-special" data-tab="special">专项</button></div>
    <section id="a-panel-routine" data-panel="routine"></section><section id="a-panel-special" data-panel="special" hidden></section>`;
  document.body.appendChild(fx);
  window.Eam.Tabs.enhance(fx);
  const rt = fx.querySelector('[data-tab="routine"]'), sp = fx.querySelector('[data-tab="special"]');
  assert('Tabs: 尊重已有前缀 id(不覆盖)', rt.id === 'a-routine' && sp.id === 'a-special');
  assert('Tabs: 前缀 panel 配对(aria-controls=前缀 panel)', sp.getAttribute('aria-controls') === 'a-panel-special'
    && fx.querySelector('#a-panel-special').getAttribute('role') === 'tabpanel'
    && fx.querySelector('#a-panel-special').getAttribute('aria-labelledby') === 'a-special');

  // 9 增强幂等
  const beforeMark = aGroup.dataset.tabsEnhanced;
  window.Eam.Tabs.enhance(rootEl);
  window.Eam.Tabs.enhance(rootEl);
  assert('Tabs: 重复 enhance 幂等(标记不变)', aGroup.dataset.tabsEnhanced === beforeMark && aGroup.getAttribute('role') === 'tablist');

  console.log(results.join('\n'));
  console.log('\nTabs 契约: ' + pass + ' 通过 / ' + fail + ' 失败');
  process.exit(fail ? 1 : 0);
})().catch((e) => { console.error('RUNNER ERROR', e); process.exit(2); });
