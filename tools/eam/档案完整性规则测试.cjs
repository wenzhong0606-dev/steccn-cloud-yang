const test=require('node:test'),assert=require('node:assert/strict'),r=require('../../docs/eam/prototype/mock/archive-rules.js');
const context={equipment:{id:'EQ-001',model:'MX-2400',classification:'生产设备 / 混合机',lifecycle:'active'},files:[],meters:[{pointId:'P1',unit:'°C'}],parameters:[{id:'PAR-MX-01'}],definitions:r.json.parameters};
test('模板行存在不等于适用关系已经核验，未知专业规则不判通过',()=>{
 const rows=r.complete([{id:'serial',name:'序列号',status:'通过'}],context);
 assert.equal(rows.find(x=>x.id==='TEMPLATE').status,'未核验');
 assert.equal(rows.find(x=>x.id==='DOMAIN-risk').status,'未核验');
 assert.equal(rows.find(x=>x.id==='FILE-model').status,'不适用');
 assert(rows.some(x=>x.id==='MISSING-PAR-MX-04'&&x.status==='待补录'));
 const other=r.complete([],{...context,equipment:{id:'OTHER',model:'OTHER',lifecycle:'active'}});assert.equal(other.find(x=>x.id==='FILE-image').status,'未核验');
 for(const equipment of [{id:'OTHER',model:'OTHER',lifecycle:'active'},{...context.equipment,lifecycle:'unknown'}]){
  const unknown=r.complete([{id:'CHECK-PAR-MX-01',status:'通过',repairTab:'parameters'}],{...context,equipment});
  assert.equal(unknown.find(x=>x.id==='CHECK-PAR-MX-01').status,'未核验');
  assert.equal(r.completeSummary(unknown).passed,0);
 }
});
test('资料检查隔离设备、移除、示例与仅元数据，本机内容读取失败保留未核验',()=>{
 let rows=r.complete([],{...context,files:[{id:'S',equipmentId:'EQ-001',category:'image',status:'active',isSample:true},{id:'O',equipmentId:'OTHER',category:'document',status:'active',contentVerified:true}]});
 assert.equal(rows.find(x=>x.id==='FILE-image').status,'未核验');assert.equal(rows.find(x=>x.id==='FILE-document').status,'待补录');
 rows=r.complete([],{...context,files:[{equipmentId:'EQ-001',category:'image',status:'active',contentVerified:true},{equipmentId:'EQ-001',category:'document',status:'active',localContent:true,contentVerified:false}]});assert.equal(rows.find(x=>x.id==='FILE-image').status,'通过');assert.equal(rows.find(x=>x.id==='FILE-document').status,'未核验');
});
test('统计区分明确适用与未知适用，不适用不进入明确适用分母，缺口优先',()=>{
 const rows=r.complete([{id:'serial',status:'待补录'},{id:'name',status:'通过'}],context),s=r.completeSummary(rows,'2026-10-12T00:00:00Z');
 assert.equal(s.total,rows.length);assert.equal(s.total,s.passed+s.missing+s.invalid+s.unverified+s.notApplicable);assert(s.required<s.total);assert.equal(rows[0].status,'待补录');assert.equal(s.ruleVersion,'COMPLETE-DEMO-V3');
 const draft=r.complete([],{...context,equipment:{...context.equipment,lifecycle:'draft'}});assert.equal(draft.find(x=>x.id==='FILE-document').status,'不适用');
 const unknown=r.complete([{id:'serial',field:'serial',status:'不适用'}],{...context,equipment:{...context.equipment,lifecycle:'unknown'}});assert.equal(unknown.find(x=>x.id==='serial').status,'未核验');assert.equal(unknown.find(x=>x.id==='FILE-document').status,'未核验');
});
