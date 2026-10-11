/* 设备业务导航上下文；只携带设备ID和固定页签，不接受任意返回URL。 */
(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.EamArchiveContext=api;})(typeof window==='undefined'?globalThis:window,function(){
 const pages=new Set(['maintenance-acceptance','maintenance-task-list','maintenance-task','maintenance-plan-list','maintenance-plan','inspection-list','check-list','inspection','abnormal-events','defect-list','failure-events','hazards','order-list','order','acceptance','defect']);
 const tabs=new Set(['overview','state','parameters','structure','installation','work','maintenance','failure','repair','bom','replacement','meters','trend','risk','cost','media','documents','history','related','audit','completeness']);
 function navigate(page,query,search,currentPage){
  const result={...query},params=new URLSearchParams(search);
  const origin=currentPage==='device'?params.get('equipment')||'EQ-001':params.get('returnEquipment');
  const tab=currentPage==='device'?params.get('tab')||'overview':params.get('returnTab');
  if(page==='device'&&origin){result.equipment ||= origin;result.tab ||= result.equipment===origin&&tabs.has(tab)?tab:'overview';return result;}
  if(!pages.has(page)||!origin)return result;
  const equipment=result.equipment||params.get('equipment')||origin;
  result.equipment=equipment;
  if(equipment===origin){result.returnEquipment=origin;result.returnTab=tabs.has(tab)?tab:'overview';}
  return result;
 }
 function filter(rows,search){const equipment=new URLSearchParams(search).get('equipment');return equipment?rows.filter(row=>row.equipmentId===equipment):rows;}
 function assertRecord(record,search){const equipment=new URLSearchParams(search).get('equipment');if(equipment&&record.equipmentId!==equipment)throw new Error('记录不属于当前设备范围');return record;}
 return {navigate,filter,assertRecord};
});
