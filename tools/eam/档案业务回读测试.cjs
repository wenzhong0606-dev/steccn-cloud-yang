const test=require('node:test'),assert=require('node:assert/strict');
const {read}=require('../../docs/eam/prototype/mock/archive-maintenance-api.js');
const {createApi}=require('../../docs/eam/prototype/mock/problem-api.js');
test('档案回读同源工单、设备隔离、限制不随工单关闭解除',async()=>{
 const api=createApi({delay:0});const data=await read(api,'EQ-001');
 assert(data.records.some(r=>r.id==='WO-2002'&&r.status==='待技术验收'));
 assert(data.restrictions.some(r=>r.status==='active'&&!r.canRelease));
 assert.equal(data.health,'故障待复核');assert.equal((await read(api,'EQ-OTHER')).records.length,0);
});
test('审计不能泄露来源接口中未授权的工单',async()=>{
 const api={request:async(_,path)=>path==='/work-config'?{labels:{},actionLabels:{}}:{rows:path==='/work-orders'?[{id:'VISIBLE',equipmentId:'EQ-001',status:'closed'}]:[],audit:[{objectId:'HIDDEN',version:2,at:'2026-10-11',action:'close'}]}};
 const data=await read(api,'EQ-001');assert.equal(data.history.length,0);assert.equal(data.pending,0);
});
