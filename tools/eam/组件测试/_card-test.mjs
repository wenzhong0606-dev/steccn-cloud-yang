// §11 Step 3 · Card/Section 组件契约（jsdom + 桩 DOM）
// 验证：增强幂等、data-collapsible 点击切换、data-collapse-default=collapsed 初始折叠、
//       多实例前缀隔离(id/for 同步)、不破坏既有 section.card（无 data-collapsible 不变）。
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
const componentsSrc = readFileSync(resolve(root, 'shared/components.js'), 'utf8');
const cardFrag = readFileSync(resolve(root, 'components/card.html'), 'utf8');

// 桩 fetch：从磁盘读片段
async function fakeFetch(url) {
  const u = String(url);
  if (u.endsWith('/components/card.html')) {
    return { ok: true, status: 200, text: async () => cardFrag };
  }
  throw new Error('unexpected fetch ' + u);
}

const html = `<!doctype html><html><body>
  <div id="mk"></div>
  <main id="root"></main>
</body></html>`;

const dom = new JSDOM(html, { runScripts: 'outside-only', url: 'http://localhost/' });
const { window } = dom;
const { document } = window;
window.fetch = fakeFetch;
window.__EamNoAuto = true; // 关闭自动启动，手动 bootstrap 以便精确断言

// 注入 loader + components（顺序：loader → components）
window.eval(loaderSrc);
window.eval(componentsSrc);

const rootEl = document.getElementById('root');

(async () => {
  // 注入两个 card 组件实例（c1 可折叠 / c2 普通）+ 一个既有 section.card（非组件，无折叠）
  rootEl.innerHTML = `
    <section class="card" data-component="card" data-component-src="../components/card.html" data-instance="c1" data-collapsible>
      <template data-fill="head"><h3>卡片一</h3></template>
      <template data-fill="body"><label id="c1-note" for="c1-input">备注<textarea id="c1-input"></textarea></label></template>
    </section>
    <section class="card" data-component="card" data-component-src="../components/card.html" data-instance="c2">
      <template data-fill="head"><h3>卡片二</h3></template>
      <template data-fill="body"><label id="c2-note" for="c2-input">备注<textarea id="c2-input"></textarea></label></template>
    </section>
    <section class="card ledger-results" id="legacy-card"><h3>既有业务卡片</h3><p>不应被折叠增强改动</p></section>
  `;

  await window.Eam.bootstrap(rootEl);

  const c1 = document.querySelector('[data-instance=c1]');
  const c2 = document.querySelector('[data-instance=c2]');
  const c1Head = c1.querySelector('.card-header');
  const c1Body = c1.querySelector('.card-body');
  const c2Head = c2.querySelector('.card-header');

  // 1 组件加载 + 前缀隔离（loader 完成标记为 data-component-loaded="" 空串，用 hasAttribute 判定）
  assert('Card: 组件已加载', c1.hasAttribute('data-component-loaded') && c2.hasAttribute('data-component-loaded'));
  assert('Card: 双实例前缀 id 隔离', !!document.getElementById('c1-c1-note') && !!document.getElementById('c2-c2-note')
    && document.getElementById('c1-c1-note').getAttribute('for') === 'c1-c1-input'
    && document.getElementById('c2-c2-note').getAttribute('for') === 'c2-c2-input');
  assert('Card: 无原始 id 冲突', document.querySelectorAll('#c1-note').length === 0 && document.querySelectorAll('#c2-note').length === 0);

  // 2 可折叠增强（c1）
  assert('Card: c1 头部可折叠 role=button', c1Head.getAttribute('role') === 'button');
  assert('Card: c1 初始 aria-expanded=true', c1Head.getAttribute('aria-expanded') === 'true');
  assert('Card: c1 点击后 body.hidden + aria-expanded=false', (() => {
    c1Head.dispatchEvent(new window.MouseEvent('click', { bubbles: true }));
    const ok = c1Body.hidden === true && c1Head.getAttribute('aria-expanded') === 'false';
    c1Head.dispatchEvent(new window.MouseEvent('click', { bubbles: true })); // 还原
    return ok;
  })());
  assert('Card: 键盘 Enter 切换', (() => {
    c1Head.dispatchEvent(new window.KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    const hidden = c1Body.hidden === true;
    c1Head.dispatchEvent(new window.KeyboardEvent('keydown', { key: ' ', bubbles: true }));
    return hidden && c1Body.hidden === false;
  })());

  // 3 普通卡片 c2 不增强
  assert('Card: c2 未声明折叠不被增强(无 role)', c2Head.getAttribute('role') === null);

  // 4 不破坏既有 section.card
  const legacy = document.getElementById('legacy-card');
  assert('Card: 既有 section.card 不变(无 role/aria)', legacy.getAttribute('role') === null && legacy.getAttribute('aria-expanded') === null);
  assert('Card: 既有卡片内容保留', !!legacy.querySelector('p') && legacy.textContent.includes('不应被折叠'));

  // 5 增强幂等（重复调用不重复绑定/不报错）
  const before = c1Head.getAttribute('aria-expanded');
  window.Eam.Card.enhance(rootEl);
  window.Eam.Card.enhance(rootEl);
  assert('Card: 重复 enhance 幂等(标记不变)', c1.dataset.cardEnhanced === 'true' && c1Head.getAttribute('aria-expanded') === before);

  // 6 data-collapse-default=collapsed 初始折叠
  const r2 = document.createElement('div');
  r2.innerHTML = `<section class="card" data-component="card" data-component-src="../components/card.html" data-instance="d" data-collapsible data-collapse-default="collapsed">
    <template data-fill="head"><h3>默认折叠</h3></template>
    <template data-fill="body"><p>内容</p></template>
  </section>`;
  rootEl.appendChild(r2);
  await window.Eam.loadComponent(r2.querySelector('[data-component="card"]'));
  window.Eam.Card.enhance(r2);
  const dHost = r2.querySelector('[data-instance=d]');
  const dHead = dHost.querySelector('.card-header');
  const dBody = dHost.querySelector('.card-body');
  assert('Card: data-collapse-default=collapsed 初始 body.hidden', dBody.hidden === true && dHead.getAttribute('aria-expanded') === 'false');

  console.log(results.join('\n'));
  console.log('\nCard 契约: ' + pass + ' 通过 / ' + fail + ' 失败');
  process.exit(fail ? 1 : 0);
})().catch(e => { console.error('RUNNER ERROR', e); process.exit(2); });
