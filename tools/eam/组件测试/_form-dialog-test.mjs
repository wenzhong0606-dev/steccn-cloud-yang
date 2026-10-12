// §7 表单与弹窗契约真实运行时验证（jsdom + 桩 DOM）。
// 覆盖：Eam.Form.collect 类型化取值 + 跳过规则（§7.1/§7.2/§9）；
//       Eam.Form.validate 标记（§7.3）；
//       Eam.Dialog.submitGuard 同步 preventDefault / busy 守卫 / 成功关闭 / 失败保留 / 抛错不关闭（§7.4）。
import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const { JSDOM } = require('jsdom');
import { readFileSync } from 'fs';

const ROOT = 'C:/developer/GIT/JAVA/new/steccn-cloud/docs/eam/prototype';
const loaderSrc = readFileSync(ROOT + '/shared/loader.js', 'utf-8');
const formSrc = readFileSync(ROOT + '/shared/form.js', 'utf-8');
const compSrc = readFileSync(ROOT + '/shared/components.js', 'utf-8');

const html = `<!doctype html><html><body data-page="fd"><form id="f"></form></body></html>`;
const dom = new JSDOM(html, { runScripts: 'outside-only', url: 'http://localhost/' });
const { window } = dom;
window.__EamNoAuto = true; // 关闭自动启动，避免与 loader 自动 bootstrap 干扰
window.eval(loaderSrc);
window.eval(formSrc);
window.eval(compSrc);
const doc = window.document;
const Eam = window.Eam;

let pass = 0, fail = 0;
const assert = (n, c, e = '') => { if (c) { pass++; console.log('PASS ' + n); } else { fail++; console.log('FAIL ' + n + ' ' + e); } };
const tick = () => new Promise((r) => setTimeout(r, 0));

// ===== 1. Form.collect 类型化取值 + 跳过规则 =====
const f = doc.getElementById('f');
f.innerHTML = `
<input name="t" value="  hi  ">
<input name="n" type="number" value="5">
<input name="n2" type="number" value="">
<input name="cb" type="checkbox" checked>
<input name="cb2" type="checkbox">
<input name="r" type="radio" value="a" checked>
<input name="r" type="radio" value="b">
<select name="m" multiple><option value="x" selected></option><option value="y" selected></option></select>
<input name="rev" type="hidden" value="3">
<input name="ro" value="roval" readonly>
<input name="dis" value="dv" disabled>
<input name="sk" value="sv" data-collect-skip>
<div hidden><input name="hd" value="hv"></div>`;
const d = Eam.Form.collect(f);
assert('collect:text 自动 trim', d.t === 'hi', 't=' + d.t);
assert('collect:number→Number', d.n === 5, 'n=' + d.n);
assert('collect:number 空→""(不强制0)', d.n2 === '', 'n2=' + JSON.stringify(d.n2));
assert('collect:checkbox 单→boolean(true)', d.cb === true);
assert('collect:checkbox 单→boolean(false)', d.cb2 === false);
assert('collect:radio→选中值', d.r === 'a', 'r=' + d.r);
assert('collect:select[multiple]→string[]', Array.isArray(d.m) && d.m.join(',') === 'x,y', 'm=' + JSON.stringify(d.m));
assert('collect:hidden 正常收集(§9)', d.rev === '3', 'rev=' + d.rev);
assert('collect:readonly 收集', d.ro === 'roval', 'ro=' + d.ro);
assert('collect:disabled 跳过(最高优先)', !('dis' in d), 'dis=' + JSON.stringify(d.dis));
assert('collect:data-collect-skip 跳过', !('sk' in d), 'sk=' + JSON.stringify(d.sk));
assert('collect:[hidden]容器 跳过', !('hd' in d), 'hd=' + JSON.stringify(d.hd));

// 重复字段容器
const f2 = doc.createElement('form');
f2.innerHTML = `<div data-repeat data-collect="parts">
  <div data-repeat-row><input data-field="code" value="A1"><input data-field="qty" type="number" value="2"></div>
  <div data-repeat-row><input data-field="code" value="B2"><input data-field="qty" type="number" value="3"></div>
</div>`;
doc.body.append(f2);
const d2 = Eam.Form.collect(f2);
assert('collect:[data-repeat]→object[]', Array.isArray(d2.parts) && d2.parts.length === 2 && d2.parts[0].code === 'A1' && d2.parts[1].qty === 3,
  'parts=' + JSON.stringify(d2.parts));

// ===== 2. Form.validate 标记 =====
const fv = doc.createElement('form');
fv.innerHTML = `<label class="field">必填<input name="req" required><span class="field-error"></span></label>`;
doc.body.append(fv);
const reqInput = fv.querySelector('[name="req"]');
const okEmpty = Eam.Form.validate(fv);
assert('validate:必填空→返回 false', okEmpty === false);
assert('validate:标 is-invalid', reqInput.classList.contains('is-invalid'));
assert('validate:标 aria-invalid', reqInput.getAttribute('aria-invalid') === 'true');
reqInput.value = '填了';
const okFilled = Eam.Form.validate(fv);
assert('validate:填值后→返回 true', okFilled === true);
assert('validate:清除 is-invalid', !reqInput.classList.contains('is-invalid'));

// ===== 3. Dialog.submitGuard（§7.4）=====
const dlg = doc.createElement('dialog');
dlg.innerHTML = `<form id="sf"><input name="x"><p role="alert" class="field-error"></p><button type="submit">go</button></form>`;
doc.body.append(dlg);
const form = dlg.querySelector('form');
const input = form.querySelector('[name="x"]');
let calls = 0, closed = false;
// jsdom 可能未实现 dialog.close；提供桩以观察关闭行为
if (typeof dlg.close !== 'function') {
  dlg.close = function () { closed = true; dlg.open = false; };
} else {
  const origClose = dlg.close.bind(dlg);
  dlg.close = function () { closed = true; origClose(); };
}
// onSubmit：x==='ok'→成功(关闭)；x==='conflict'→抛版本冲突；其余→失败(保留)
Eam.Dialog.submitGuard(form, async (fd) => {
  calls++;
  const v = fd.get('x');
  if (v === 'ok') { await tick(); return true; }
  if (v === 'conflict') { await tick(); throw new Error('版本冲突'); }
  await tick(); return false;
});

// 3a 同步 preventDefault（busy 守卫）
input.value = 'ok';
const ev = new window.Event('submit', { cancelable: true, bubbles: true });
form.dispatchEvent(ev);
assert('submitGuard:进入即同步 preventDefault', ev.defaultPrevented === true);
await tick();
assert('submitGuard:成功→调用 onSubmit 1 次', calls === 1, 'calls=' + calls);
assert('submitGuard:成功→关闭弹窗', closed === true, 'closed=' + closed);

// 3b 失败保留（不关闭）
calls = 0; closed = false;
input.value = 'bad';
const ev2 = new window.Event('submit', { cancelable: true, bubbles: true });
form.dispatchEvent(ev2);
assert('submitGuard:失败也同步 preventDefault', ev2.defaultPrevented === true);
await tick();
assert('submitGuard:失败→调用 onSubmit', calls === 1, 'calls=' + calls);
assert('submitGuard:失败→不关闭(保留输入)', closed === false, 'closed=' + closed);
assert('submitGuard:失败→输入保留', input.value === 'bad');

// 3c 抛错（版本冲突）不关闭
calls = 0; closed = false;
input.value = 'conflict';
const ev3 = new window.Event('submit', { cancelable: true, bubbles: true });
form.dispatchEvent(ev3);
await tick();
assert('submitGuard:抛错→不关闭', closed === false, 'closed=' + closed);
const errEl = form.querySelector('[role="alert"],.field-error');
assert('submitGuard:抛错→写入内联错误', !!errEl && /版本冲突/.test(errEl.textContent), 'err=' + (errEl && errEl.textContent));

// 3d busy 守卫：同步双提交仅一次 onSubmit
calls = 0; closed = false; input.value = 'ok';
const ev4 = new window.Event('submit', { cancelable: true, bubbles: true });
const ev5 = new window.Event('submit', { cancelable: true, bubbles: true });
form.dispatchEvent(ev4); // 第一次：进入 await，busy=true
form.dispatchEvent(ev5); // 第二次（同步）：busy 守卫拦截
await tick();
assert('submitGuard:busy 守卫→仅 1 次 onSubmit', calls === 1, 'calls=' + calls);
assert('submitGuard:busy 守卫→仍关闭', closed === true);

console.log('\nForm/Dialog 结果: ' + pass + ' 通过 / ' + fail + ' 失败');
process.exit(fail ? 1 : 0);
