/* 表单行为：类型化取值（§7.1）+ 跳过规则（§7.2/§9）+ 校验（§7.3）。
 * 不构建 DOM、不创建弹窗；弹窗提交守卫在 components.js 的 Eam.Dialog。
 * 合并式初始化：扩展 window.Eam.Form，不覆盖既有 Eam 成员。
 */
window.Eam ||= {};
(function (ns) {
  'use strict';
  const Form = ns.Form || (ns.Form = {});

  // ---- 跳过判定（§7.2 / §9）-----------------------------------------------
  // disabled 最高优先；显式 data-collect-skip；[hidden] 容器内的字段（整块不适用）。
  // 收集：type=hidden（id/revision/version）、readonly（值应回传，仅禁编辑）。
  function shouldSkip(ctrl) {
    if (ctrl.disabled) return true;
    if (ctrl.hasAttribute('data-collect-skip')) return true;
    if (ctrl.closest('[hidden]')) return true; // 祖先整块 hidden → 跳过
    return false;
  }

  // 取值键：name 优先，其次 id，再 data-collect（显式重命名）
  function fieldName(ctrl) {
    return ctrl.getAttribute('data-collect') || ctrl.getAttribute('name') || ctrl.dataset.localId || ctrl.id || '';
  }

  function isEmpty(ctrl) {
    if (ctrl.type === 'checkbox' || ctrl.type === 'radio') return !ctrl.checked;
    const v = (ctrl.value || '').trim();
    return v === '';
  }

  // ---- 单控件取值（按类型，§7.1）-----------------------------------------
  function readValue(ctrl) {
    const tag = ctrl.tagName.toLowerCase();
    const t = (ctrl.type || tag).toLowerCase();
    if (tag === 'select' && ctrl.multiple) {
      return [...ctrl.selectedOptions].map((o) => o.value);
    }
    if (tag === 'textarea') return ctrl.value; // 不强制 trim（可能含有意空白）
    switch (t) {
      case 'checkbox':
        return ctrl.checked; // 单 checkbox → boolean
      case 'radio':
        return ctrl.checked ? ctrl.value : '';
      case 'number': {
        const v = (ctrl.value || '').trim();
        return v === '' ? '' : Number(v); // 空返回 ''（不强制 0，§7.1）
      }
      case 'file':
        return [...(ctrl.files || [])]; // File[]
      case 'hidden':
        return ctrl.value; // 收集（id/revision/version 等元数据，§7.2/§9）
      case 'date':
      case 'datetime-local':
        return (ctrl.value || '').trim();
      default: // text/search/email/url/tel/password/readonly
        return (ctrl.value || '').trim();
    }
  }

  // 重复字段容器（[data-repeat]）：返回行对象数组
  function collectRepeat(container, root) {
    const rows = [];
    const rowEls = [...container.children].filter((c) => c.matches('[data-repeat-row],.repeat-row,li,tr,div'));
    if (!rowEls.length) return rows;
    for (const rowEl of rowEls) {
      const row = collect(rowEl, rowEl, true);
      rows.push(row);
    }
    return rows;
  }

  // ---- 收集（§7.1）-------------------------------------------------------
  // root 限定查找范围（多实例隔离）；默认 form 自身。
  function collect(form, root, repeatRow = false) {
    const r = root || form;
    const multiGroups = {}; // checkbox[data-multi] 同名组 → 聚合为 string[]
    const data = {};
    const controls = [...r.querySelectorAll('input,select,textarea')];
    for (const c of controls) {
      if (shouldSkip(c)) continue;
      if (!repeatRow && c.closest('[data-repeat]'))continue;
      const t = (c.type || c.tagName.toLowerCase()).toLowerCase();
      if (t === 'checkbox' && c.hasAttribute('data-multi')) {
        const name = c.getAttribute('data-field') || fieldName(c);
        if (!name) continue;
        multiGroups[name] ||= []; if(c.checked) multiGroups[name].push(c.value);
        continue;
      }
      const name = c.getAttribute('data-field') || fieldName(c);
      if (!name) continue;
      if (t === 'radio') {
        if (c.checked) data[name] = c.value;
        else if (!(name in data)) data[name] = ''; // 未选 → ''
        continue;
      }
      data[name] = readValue(c);
    }
    // 重复字段容器
    r.querySelectorAll('[data-repeat]').forEach((container) => {
      const name = container.getAttribute('data-collect') || container.dataset.localId || container.id || container.getAttribute('data-field');
      if (!name) return;
      data[name] = collectRepeat(container, root);
    });
    // 聚合 checkbox 组
    for (const [k, v] of Object.entries(multiGroups)) data[k] = v;
    return data;
  }

  // 自定义校验器注册表（data-validate="name" 引用）
  Form.validators = Form.validators || {};
  Form.registerValidator = (name, fn) => { Form.validators[name] = fn; };

  // ---- 校验（§7.3）-------------------------------------------------------
  // 原生 required/pattern/min/max/type + 自定义 data-validate；
  // 失败标 is-invalid + aria-invalid="true"，错误文案写入关联 [aria-errormessage] 或 .field-error。
  function validate(form, root) {
    const r = root || form;
    let ok = true;
    const controls = [...r.querySelectorAll('input,select,textarea')];
    for (const c of controls) {
      if (c.disabled || c.hasAttribute('data-collect-skip')) continue;
      const t = (c.type || c.tagName.toLowerCase()).toLowerCase();
      c.classList.remove('is-invalid');
      c.removeAttribute('aria-invalid');
      let msg = '';
      if(shouldSkip(c)) continue;
      if(t==='checkbox'&&c.hasAttribute('data-multi')){
        const group=controls.filter(other=>other.type==='checkbox'&&other.hasAttribute('data-multi')&&fieldName(other)===fieldName(c)&&!shouldSkip(other));
        if(group.some(other=>other.required)&&!group.some(other=>other.checked))msg='请至少选择一项';
      }else if(c.willValidate && !c.validity.valid) msg=c.validationMessage || '格式不符要求';
      if(!msg && c.hasAttribute('data-validate')){const fn=Form.validators[c.dataset.validate];const result=fn?fn(readValue(c),c):'校验规则不存在';if(result!==true)msg=typeof result==='string'?result:'校验未通过';}
      const errId=c.getAttribute('aria-errormessage');
      const associated=errId?[...r.querySelectorAll('[id]')].find(el=>el.id===errId):null;
      const oldError=associated||c.closest('.field,.form-group')?.querySelector('.field-error');if(oldError)oldError.textContent='';
      if (msg) {
        ok = false;
        c.classList.add('is-invalid');
        c.setAttribute('aria-invalid', 'true');
        // 错误文案写入关联节点
        let errEl = associated;
        if (!errEl) errEl = c.closest('.field,.form-group')?.querySelector('[aria-errormessage],.field-error');
        if (!errEl) errEl = c.parentElement?.querySelector('.field-error');
        if (errEl) errEl.textContent = msg;
      }
    }
    return ok;
  }

  Form.collect = collect;
  Form.validate = validate;
  Form.shouldSkip = shouldSkip;
})(window.Eam);
