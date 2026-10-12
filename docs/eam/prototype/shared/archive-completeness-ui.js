/* 完整性统计和筛选：统计始终使用本次完整结果，清单可按缺口与领域查看。 */
window.EamArchiveCompletenessUi=(()=>{
 const el=(tag,text,cls='')=>{const n=document.createElement(tag);n.textContent=text;n.className=cls;return n;};
 const labels={'待补录':'缺失','待修正':'待修正','未核验':'未核验','通过':'通过','不适用':'不适用'};
 function matches(row,s){return (s.view!=='gaps'||!['通过','不适用'].includes(row.status))&&(!s.checkStatus||row.status===s.checkStatus)&&(!s.group||row.group===s.group);}
 function render(host,rows,display,s,change){
  if(!s.view)s.view='gaps';const info=display.completeness;
  if(!info){host.append(el('p','本次未取得完整性检查结果。','alert alert-secondary'));return;}
  const box=el('section','','card mb-3');box.setAttribute('aria-label','完整性检查汇总');box.append(el('h3','本次检查','card-header h6 mb-0'));const body=el('div','','card-body');
  const time=Number.isFinite(Date.parse(info.checkedAt))?new Date(info.checkedAt).toLocaleString('zh-CN',{timeZone:'Asia/Shanghai',hour12:false})+'（北京时间）':'未提供';body.append(el('p','规则 '+info.ruleVersion+' · 检查时间 '+time+' · 分类 '+(info.classification||'未提供')+' · 生命周期 '+(info.lifecycle||'未提供'),'small text-secondary'));
  body.append(el('p','登记检查项 '+info.total+' · 明确适用必需项 '+info.required+' · 不适用 '+info.notApplicable+'；未知适用项不计入明确必需项分母。','small'));
  body.append(el('p',info.coverage,'small text-secondary'));
  if(!info.total)body.append(el('p','接口返回空结果，本次没有完成检查；不能解释为全部通过。','alert alert-secondary'));
  const stats=el('div','','d-flex flex-wrap gap-2');for(const [status,key]of [['通过','passed'],['待补录','missing'],['待修正','invalid'],['未核验','unverified'],['不适用','notApplicable']]){const b=el('button',labels[status]+' '+info[key],'btn btn-sm '+(s.checkStatus===status?'btn-primary':'btn-outline-secondary'));b.type='button';b.dataset.completeStatus=status;b.setAttribute('aria-pressed',String(s.checkStatus===status));b.onclick=()=>{s.view='all';s.checkStatus=s.checkStatus===status?'':status;s.page=1;change();};stats.append(b);}body.append(stats);box.append(body);host.append(box);
  const nav=el('div','','nav nav-pills gap-2 mb-2');for(const [key,title]of [['gaps','缺口优先'],['all','全部检查项']]){const b=el('button',title,'nav-link'+(s.view===key?' active':''));b.type='button';b.dataset.completeView=key;b.setAttribute('aria-pressed',String(s.view===key));b.onclick=()=>{s.view=key;s.checkStatus='';s.page=1;change();};nav.append(b);}host.append(nav);
  const controls=el('div','','d-flex flex-wrap gap-2 mb-3');for(const [key,title,values]of [['group','检查领域',[...new Set(rows.map(row=>row.group))].filter(Boolean).map(v=>[v,v])],['checkStatus','检查结果',Object.entries(labels)]]){const wrap=el('label',title,'small d-flex flex-column gap-1'),select=document.createElement('select');select.className='form-select form-select-sm';select.setAttribute('aria-label',title);select.append(new Option('全部',''));for(const [value,text]of values)select.append(new Option(text,value));select.value=s[key]||'';select.onchange=()=>{s[key]=select.value;if(key==='checkStatus')s.view='all';s.page=1;change();};wrap.append(select);controls.append(wrap);}const reset=el('button','重置检查筛选','btn btn-sm btn-outline-secondary');reset.type='button';reset.onclick=()=>{for(const key of ['query','status','source','group','checkStatus'])s[key]='';s.view='gaps';s.page=1;change();};controls.append(reset);host.append(controls);
 }
 return {render,matches,labels};
})();
