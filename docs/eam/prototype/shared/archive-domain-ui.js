/* 档案对象分视图与专业只读详情；Bootstrap 样式，数据仍由来源接口提供。 */
(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.EamArchiveDomainUi=api;})(typeof window==='undefined'?globalThis:window,function(){
 const views={
  work:[['tasks','待执行任务'],['history','历史记录'],['strategy','适用策略']],
  maintenance:[['tasks','保养任务'],['plans','保养计划'],['history','执行履历']],
  failure:[['defects','缺陷'],['failures','故障']],
  related:[['pending','未结业务对象'],['all','全部关联对象']]
 };
 function matches(domain,view,row){
  if(domain==='related')return view==='all'||row.isPending===true;
  if(domain==='failure')return view==='defects'?row.kind==='defects':row.kind==='failures';
  if(domain==='maintenance')return row.objectType===({tasks:'任务',plans:'计划',history:'履历'})[view];
  if(domain==='work')return view==='strategy'?row.objectType==='策略':view==='history'?row.objectType==='记录':row.objectType==='任务';
  return true;
 }
 function columns(domain,view,fallback){
  if(domain==='maintenance')return [['name',view==='plans'?'计划名称':'任务 / 履历'],['status','状态'],['assignee','主责 / 审核人'],['frequency','周期 / 窗口'],['dueAt','期限']];
  if(domain==='work')return [['name','对象名称'],['objectType','类型'],['status','状态 / 结论'],['executor','执行人'],['window','执行窗口'],['eventAt','完成 / 发生时间']];
  if(domain==='failure')return [['name',view==='defects'?'缺陷描述':'故障描述'],['priorityLabel','等级'],['eventAt','发生时间'],...(view==='failures'?[['downtime','停机 / h']]:[]),['status','处理状态'],['owner','责任人']];
  if(domain==='related')return [['name','对象名称'],['objectType','对象类型'],['status','状态'],['owner','责任人'],['dueAt','期限']];
  if(domain==='repair')return [['name','维修内容'],['objectType','类型'],['status','状态'],['owner','责任人'],['dueAt','期限']];
  if(domain==='restrictions')return [['name','限制原因'],['status','限制状态'],['owner','责任人'],['dueAt','期限'],['releaseCondition','解除条件']];
  return fallback;
 }
 function controls(host,domain,current,rows,onChange){
  if(!views[domain])return;
  const nav=document.createElement('div');nav.className='nav nav-pills gap-2 mb-3';nav.setAttribute('aria-label','业务对象视图');
  for(const [key,label]of views[domain]){const button=document.createElement('button');button.type='button';button.className='nav-link'+(key===current?' active':'');button.dataset.archiveView=key;button.setAttribute('aria-pressed',String(key===current));button.textContent=label+'（'+rows.filter(row=>matches(domain,key,row)).length+'）';button.onclick=()=>onChange(key);nav.append(button);}
  host.append(nav);
  if(domain==='related'){const note=document.createElement('p');note.className='small text-secondary';note.textContent='按业务对象计数；同一问题链可包含多个对象，不代表独立问题数。已分诊异常和已终结对象不计入未结数量。';host.append(note);}
 }
 function details(host,row,domain){
  host.replaceChildren();
  const sections=row.detailSections||[{title:'业务记录',fields:[['业务内容',row.content],['关联依据',row.evidence],['责任人',row.owner||row.executor||row.assignee],['期限',row.dueAt],['解除条件',row.releaseCondition]]}];
  for(const section of sections){const card=document.createElement('section');card.className='card mb-3';const title=document.createElement('h3');title.className='card-header h6 mb-0';title.textContent=section.title;const body=document.createElement('dl');body.className='card-body row mb-0';
   for(const [label,value]of section.fields){const dt=document.createElement('dt'),dd=document.createElement('dd');dt.className='col-sm-4';dd.className='col-sm-8 text-break';dt.textContent=label;dd.textContent=value===null||value===undefined||value===''?'来源未提供':String(value);body.append(dt,dd);}card.append(title,body);host.append(card);
  }
  const note=document.createElement('p');note.className='small text-secondary';note.textContent='只读来源投影；执行、验收、终止和限制解除由来源流程处理。历史样本不作为当前业务闭环证据。';host.append(note);
 }
 return {views,matches,columns,controls,details};
});
