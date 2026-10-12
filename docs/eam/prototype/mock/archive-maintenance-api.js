/* 档案只读投影：读取来源接口，不复制或改写业务状态。 */
(function(root){
 const kinds={'discoveries':['异常事件','abnormal-events'],defects:['缺陷','defect-list'],failures:['故障','failure-events'],'work-orders':['维修工单','order'],hazards:['隐患','hazards']};
 async function read(api,equipmentId){
  const [config,people]=await Promise.all([api.request('GET','/work-config'),api.request('GET','/people')]);
  const responses=await Promise.all(Object.keys(kinds).map(async kind=>({kind,...await api.request('GET','/'+kind)})));
  const records=[],audit=new Map();
  for(const response of responses){
   const scoped=response.rows.filter(row=>row.equipmentId===equipmentId),allowed=new Set(scoped.map(row=>row.id));
   for(const row of scoped){const [source,target]=kinds[response.kind];records.push({...row,kind:response.kind,name:row.description||row.scope||source,content:config.labels[row.status]||row.status,status:config.labels[row.status]||row.status,sourceState:row.status,source,sourceId:row.id,revision:row.version||1,eventAt:row.occurredAt||row.createdAt||'—',updatedAt:row.updatedAt||row.occurredAt||row.createdAt||'—',executor:row.assigneeId||row.executorId||'—',target,query:target==='order'?{order:row.id}:{id:row.id}});}
   for(const entry of response.audit||[])if(allowed.has(entry.objectId))audit.set([entry.objectId,entry.action,entry.version,entry.at].join('|'),entry);
  }
  for(const row of records){const actor=row.assigneeId||row.executorId;row.executor=people.rows?.find(person=>person.id===actor)?.name||actor||'—';row.eventAt=row.execution?.completedAt||row.occurredAt||row.createdAt||'—';}
  const restrictions=records.filter(row=>row.restriction).map(row=>({...row,id:'LIMIT-'+row.id,name:row.restriction.reason,content:row.restriction.status==='active'?'有效限制':'已解除',status:row.restriction.status,evidence:'限制范围 '+row.restriction.scope,canRelease:false}));
  const history=[...audit.values()].map(entry=>{const row=records.find(r=>r.id===entry.objectId);return {...row,id:'AUD-'+entry.objectId+'-'+entry.version+'-'+entry.action,name:config.actionLabels[entry.action]||entry.action,content:(entry.before?.status||'新建')+' → '+(entry.after?.status||'—'),evidence:entry.reason||'—',updatedAt:entry.at,revision:entry.version};});
  const person=id=>people.rows?.find(p=>p.id===id)?.name||id||'未记录';
  for(const row of records){
   row.objectType=kinds[row.kind][0];row.query.equipment=equipmentId;
   row.isPending=row.kind==='discoveries'?row.sourceState==='submitted':!['closed','cancelled','archived','withdrawn'].includes(row.sourceState);
   row.owner=person(row.ownerId||row.assigneeId||row.executorId);
   row.priorityLabel=config.labels[row.priority]||row.priority||'未记录';
   if(row.kind==='failures'){const duration=Date.parse(row.restoredAt)-Date.parse(row.occurredAt);row.downtime=Number.isFinite(duration)&&duration>=0?Number((duration/3600000).toFixed(2)):'待恢复 / 未核实';}
   const arrangements=row.terminationArrangements||row.residuals||(row.followupArrangement?[row.followupArrangement]:[]);
   row.detailSections=[
    {title:'对象与责任',fields:[['对象类型',row.objectType],['状态',row.status],['责任人',row.owner],['期限',row.dueAt],['等级',row.priorityLabel],['关联问题',(row.problemRefs||row.links||[]).map(r=>(kinds[r.kind]?.[0]||r.kind)+' / '+r.id).join('；')]]},
    {title:'执行与验收',fields:[['原因诊断',row.execution?.diagnosis||row.failureMode],['功能影响',row.impact],['处理措施',row.execution?.measures||row.control?.description],['执行人',row.execution?.executorId?person(row.execution.executorId):row.executor],['完成 / 恢复时间',row.execution?.completedAt||row.restoredAt],['试运行',row.execution?.trial],['材料引用',row.execution?.materialRef],['工时 / 分钟',row.execution?.laborMinutes],['执行证据',row.execution?.evidence||row.evidence],['验收记录',(row.acceptances||[]).map(a=>person(a.reviewerId)+' / '+(a.decision==='pass'?'通过':'退回')+' / '+a.reviewedAt+' / '+a.reason).join('；')]]},
    {title:'后续与限制',fields:[['终止原因',row.cancelReason],['现场恢复证据',row.cancelEvidence],['逐项后续安排',arrangements.map(a=>(kinds[a.kind]?.[0]||a.kind)+' / '+a.id+'：责任人 '+person(a.ownerId)+'；期限 '+a.dueAt+'；控制 '+a.control+'；后续工单 '+(a.followupOrderId||'责任人直接承接')).join('；')],['使用限制',row.restriction?.reason],['解除条件',row.restriction?'须由来源流程完成整改、复查与专业放行':'未记录']]}
   ];
   if(row.authorityReceipt){const receipt=row.authorityReceipt;row.detailSections[1].fields.push(['权威来源',row.authority],['权威回执',receipt.id],['权威结果',config.labels[receipt.status]||receipt.status],['权威复核人',receipt.verifiedBy],['权威回执时间',receipt.at],['权威证据',receipt.evidence]);}
   const verification=row.verification||row.review;
   if(verification)row.detailSections[1].fields.push(['验证 / 复查结论',verification.decision==='pass'?'通过':'退回'],['验证 / 复查人',row.verifiedBy||verification.reviewerId?person(row.verifiedBy||verification.reviewerId):'来源未提供'],['验证 / 复查时间',verification.at],['验证 / 复查原因',verification.reason],['验证 / 复查证据',verification.evidence]);
  }
  for(const row of restrictions){row.owner=person(row.ownerId||row.assigneeId);row.dueAt=row.dueAt||row.control?.validUntil;row.releaseCondition='来源整改、复查和专业放行全部完成后解除';row.query.equipment=equipmentId;}
  const pending=records.filter(row=>row.isPending).length;
  const health=records.some(row=>row.kind==='failures'&&row.sourceState!=='closed')?'故障待复核':records.some(row=>row.kind==='defects'&&row.sourceState!=='closed')?'存在缺陷':records.some(row=>row.kind==='discoveries'&&row.sourceState==='submitted')?'待确认':null;
  return {records,restrictions,history,pending,health};
 }
 const exports={read};if(typeof module==='object'&&module.exports)module.exports=exports;else root.EamArchiveMaintenance=exports;
})(typeof window==='undefined'?globalThis:window);
