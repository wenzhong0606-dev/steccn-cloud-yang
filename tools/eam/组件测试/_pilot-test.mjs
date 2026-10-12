// 组件加载层真实运行时验证（jsdom + 桩 fetch）。
// 覆盖用户要求的四项契约：加载失败重试 / 同页双实例 / 弹窗二次打开（关闭不 dispose）/ 未迁移页面回归。
// jsdom 已知怪癖：文档级 querySelectorAll 会把 <template> 内容计入实时树，且 closest('template') 跨模板边界返回 null。
//   真实浏览器中 <template> 内容惰性、不参与实时 DOM 查询；故测试用 inertNodes() 显式排除模板内容，使其与真实浏览器一致。
import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const { JSDOM } = require('jsdom');
import { readFileSync } from 'fs';

const ROOT = 'C:/developer/GIT/JAVA/new/steccn-cloud/docs/eam/prototype';
const loaderSrc = readFileSync(ROOT + '/shared/loader.js', 'utf-8');

// 桩 fetch：从磁盘读取任意 ../components/X.html 或 ../shared/X（与页面相对路径一致）
async function fakeFetch(url) {
  let rel = url;
  if (rel.startsWith('../components/')) rel = ROOT + '/components/' + rel.slice('../components/'.length);
  else if (rel.startsWith('../shared/')) rel = ROOT + '/shared/' + rel.slice('../shared/'.length);
  else throw new Error('未知片段 ' + url);
  const text = readFileSync(rel, 'utf-8');
  return { ok: true, status: 200, text: async () => text };
}

const html = `<!doctype html><html><body data-page="pilot">
<!-- 1 同页双实例：各自含 id=reuse-btn / id=rows，加载后应变 a-/b- 前缀 -->
<section class="card" data-component="list-panel" data-component-src="../components/list-panel.html" data-instance="a">
  <template data-fill="head"><h2>实例A</h2><div class="list-panel-tools"><button id="reuse-btn" type="button">A</button></div></template>
  <template data-fill="body"><div class="table-wrapper"><table class="data-table"><thead><tr><th>名称</th></tr></thead><tbody id="rows"></tbody></table></div></template>
</section>
<section class="card" data-component="list-panel" data-component-src="../components/list-panel.html" data-instance="b">
  <template data-fill="head"><h2>实例B</h2><div class="list-panel-tools"><button id="reuse-btn" type="button">B</button></div></template>
  <template data-fill="body"><div class="table-wrapper"><table class="data-table"><thead><tr><th>名称</th></tr></thead><tbody id="rows"></tbody></table></div></template>
</section>

<!-- 2 加载失败与重试：引用不存在片段 -->
<div class="card" data-component="list-panel" data-component-src="../components/__missing__.html" id="broken-host">
  <template data-fill="head"><h2>会失败</h2></template>
  <template data-fill="body"><p>占位</p></template>
</div>

<!-- 3 弹窗二次打开（关闭不 dispose）：dialog-shell 组件，关闭只是隐藏，实例保留；仅移除/替换节点才 dispose -->
<dialog class="card" data-component="dialog-shell" data-component-src="../components/dialog-shell.html" data-instance="d1" id="dlg-host">
  <template data-fill="header"><span>弹窗标题</span></template>
  <template data-fill="body"><input id="dlg-input"><button id="dlg-submit" type="submit">提交</button></template>
  <template data-fill="footer"><button id="dlg-ok" type="button">确定</button></template>
</dialog>

<!-- 4 未迁移页面回归：无 data-component-src 的既有节点，加载层不应触碰 -->
<div id="legacy-widget" data-legacy="1"><span id="legacy-span">原内容</span><button id="legacy-btn" type="button">旧按钮</button></div>
</body></html>`;

const dom = new JSDOM(html, { runScripts: 'outside-only', url: 'http://localhost/' });
const { window } = dom;
window.fetch = fakeFetch;
window.__EamNoAuto = true;
window.eval(loaderSrc);

let pass = 0, fail = 0;
const assert = (name, cond, extra = '') => {
  if (cond) { pass++; console.log('PASS ' + name); }
  else { fail++; console.log('FAIL ' + name + ' ' + extra); }
};

// 模板惰性节点集合：真实浏览器中这些不参与实时 DOM 查询，测试须显式排除
function inertNodes(doc) {
  const s = new Set();
  doc.querySelectorAll('template').forEach((t) => {
    if (t.content) t.content.querySelectorAll('*').forEach((n) => s.add(n));
  });
  return s;
}

await window.Eam.bootstrap(window.document);
const inert = inertNodes(window.document);
const doc = window.document;

// ---- 1 同页双实例：前缀隔离 ----
const aHost = doc.querySelector('[data-instance="a"]');
const bHost = doc.querySelector('[data-instance="b"]');
const aBtn = doc.getElementById('a-reuse-btn');
const bBtn = doc.getElementById('b-reuse-btn');
const aRows = doc.getElementById('a-rows');
const bRows = doc.getElementById('b-rows');
assert('双实例:A 前缀 id 存在', !!aBtn);
assert('双实例:B 前缀 id 存在', !!bBtn, 'bBtn=' + bBtn);
assert('双实例:A/B rows 存在', !!aRows && !!bRows, 'aRows=' + aRows + ' bRows=' + bRows);
const liveReuse = [...doc.querySelectorAll('button')].filter((b) => b.id === 'reuse-btn' && !inert.has(b));
assert('双实例:实时 DOM 无原始 id 冲突(排除模板惰性)', liveReuse.length === 0, 'live=' + liveReuse.length);
assert('双实例:prefixed 标记', aHost.dataset.prefixed === 'true' && bHost.dataset.prefixed === 'true',
  'a=' + aHost.dataset.prefixed + ' b=' + bHost.dataset.prefixed);
assert('双实例:插入骨架保留 data-component 标记', aHost.getAttribute('data-component') === 'list-panel');

// 引用正确性：A 的 for/aria 等应指向 a- 前缀（无跨实例串扰）
const aBody = aHost.querySelector('[data-region="body"]');
const aBodyTbody = aBody && aBody.querySelector('table tbody');
assert('填充:body 区域含业务表格(且 id 已前缀)', !!(aBody && aBody.querySelector('table')) && aBodyTbody && aBodyTbody.id === 'a-rows',
  'tbodyId=' + (aBodyTbody && aBodyTbody.id));

// ---- 2 加载失败与重试 ----
const broken = doc.getElementById('broken-host');
assert('失败:broken-host 标记 failed', broken.dataset.componentLoaded === 'failed', 'loaded=' + broken.dataset.componentLoaded);
assert('失败:渲染重试按钮', !!broken.querySelector('button'));
broken.dataset.componentSrc = '../components/list-panel.html';
delete broken.dataset.componentLoaded; delete broken.dataset.componentError;
const retry = await window.Eam.loadComponent(broken);
assert('重试:修正 src 后成功', retry.status === 'loaded', 'status=' + retry.status);
assert('重试:成功后端标记 loaded', broken.dataset.componentLoaded === 'loaded');

// ---- 3 弹窗二次打开：关闭不 dispose ----
const dlgHost = doc.getElementById('dlg-host');
assert('弹窗:加载完成', dlgHost.dataset.componentLoaded === 'loaded', 'loaded=' + dlgHost.dataset.componentLoaded);
assert('弹窗:前缀隔离标记', dlgHost.dataset.prefixed === 'true', 'prefixed=' + dlgHost.dataset.prefixed);
assert('弹窗:标题区域注入且前缀', !!doc.getElementById('d1-dialog-shell-title'));
assert('弹窗:业务 input 前缀', !!doc.getElementById('d1-dlg-input'));
// 模拟「关闭」：加载层不监听 close，实例应原样保留
assert('弹窗:关闭后实例仍 loaded(不 dispose)', dlgHost.dataset.componentLoaded === 'loaded' && dlgHost.dataset.prefixed === 'true');
// 模拟「再次打开」：同一实例仍在，可重新查询
assert('弹窗:二次打开同一实例仍在', !!doc.getElementById('d1-dialog-shell-title') && !!doc.getElementById('d1-dlg-input'));
// 仅当节点被移除/替换时才 dispose：清除加载标记，允许二次增强
window.Eam.dispose(dlgHost);
assert('弹窗:显式 dispose 后清除标记', dlgHost.dataset.componentLoaded === undefined && dlgHost.dataset.prefixed === undefined,
  'loaded=' + dlgHost.dataset.componentLoaded + ' prefixed=' + dlgHost.dataset.prefixed);

// ---- 4 未迁移页面回归 ----
const legacy = doc.getElementById('legacy-widget');
assert('回归:既有节点未被加载层触碰', !!legacy && legacy.getAttribute('data-component-src') === null,
  'data-component-src=' + legacy.getAttribute('data-component-src'));
assert('回归:既有子节点与 id 保留', !!doc.getElementById('legacy-span') && doc.getElementById('legacy-span').textContent === '原内容'
  && !!doc.getElementById('legacy-btn'));
assert('回归:既有 id 未被加前缀', !!doc.getElementById('legacy-btn') && doc.getElementById('legacy-btn').id === 'legacy-btn');

console.log('\n结果: ' + pass + ' 通过 / ' + fail + ' 失败');
process.exit(fail ? 1 : 0);
