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
  const pending=records.filter(row=>row.kind==='discoveries'?row.sourceState==='submitted':!['closed','cancelled','archived','withdrawn'].includes(row.sourceState)).length;
  const health=records.some(row=>row.kind==='failures'&&row.sourceState!=='closed')?'故障待复核':records.some(row=>row.kind==='defects'&&row.sourceState!=='closed')?'存在缺陷':records.some(row=>row.kind==='discoveries'&&row.sourceState==='submitted')?'待确认':null;
  return {records,restrictions,history,pending,health};
 }
 const exports={read};if(typeof module==='object'&&module.exports)module.exports=exports;else root.EamArchiveMaintenance=exports;
})(typeof window==='undefined'?globalThis:window);
