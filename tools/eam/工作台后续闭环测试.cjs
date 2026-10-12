const {test}=require('node:test'),assert=require('node:assert/strict');
const maintenance=require('../../docs/eam/prototype/mock/maintenance-api.js');
test('终止批准生成后续执行入口，责任人提交、独立复核、版本与权限守卫',async()=>{
 const ctx={role:'supervisor',person:'P04',scenario:'normal'},api=maintenance.createApi({businessDate:()=>'2026-10-31',context:()=>ctx,delay:0});
 const g=await api.request('POST','/maintenance/plans/MP-002/generate',{activeVersion:1,expectedRevision:1,windowKeys:['MP-002:V1:2026-10-31'],idempotencyKey:'wb'}),path='/maintenance/tasks/'+g.items[0].taskId;
 Object.assign(ctx,{role:'technician',person:'P03'});let t=await api.request('POST',path+'/start',{expectedRevision:1,confirmed:true,permitId:'PERMIT-EQ-001'});
 t=await api.request('POST',path+'/request-termination',{expectedRevision:t.revision,executionVersion:1,reason:'窗口调整',completedWork:'检查完成',residualRisk:'未润滑',controlMeasures:'保持停机',followup:'另安排停机润滑',followupOwnerId:'P05',followupDueAt:'2026-11-20',evidence:'排程证据'});
 Object.assign(ctx,{role:'supervisor',person:'P04'});t=await api.request('POST',path+'/review-termination',{expectedRevision:t.revision,executionVersion:1,terminationRevision:t.termination.revision,instanceId:t.termination.instanceId,sample:'approved',reason:'独立核实',confirmed:true});
 assert.equal(t.followup.status,'pending');assert.equal(t.followup.ownerId,'P05');assert.equal(t.actions['submit-followup'],false);
 Object.assign(ctx,{role:'engineer',person:'P05'});t=await api.request('GET',path);assert.equal(t.actions['submit-followup'],true);
 await assert.rejects(api.request('POST',path+'/submit-followup',{expectedRevision:t.revision,executionVersion:1,reason:'完成',evidence:''}),/证据/);
 t=await api.request('POST',path+'/submit-followup',{expectedRevision:t.revision,executionVersion:1,reason:'后续润滑已执行',evidence:'后续单 PMT-02'});assert.equal(t.followup.status,'pending_review');assert.equal(t.actions['review-followup'],false);
 Object.assign(ctx,{role:'supervisor',person:'P04'});t=await api.request('GET',path);
 await assert.rejects(api.request('POST',path+'/review-followup',{expectedRevision:t.revision-1,executionVersion:1,decision:'passed',reason:'核实',evidence:'复核记录'}),/版本/);
 t=await api.request('POST',path+'/review-followup',{expectedRevision:t.revision,executionVersion:1,decision:'returned',reason:'证据不足',evidence:'复核记录'});assert.equal(t.followup.status,'pending');
 Object.assign(ctx,{role:'engineer',person:'P05'});t=await api.request('POST',path+'/submit-followup',{expectedRevision:t.revision,executionVersion:1,reason:'补充执行结果',evidence:'结果证据'});
 Object.assign(ctx,{role:'supervisor',person:'P04'});t=await api.request('POST',path+'/review-followup',{expectedRevision:t.revision,executionVersion:1,decision:'passed',reason:'核实后续完成',evidence:'独立复核证据'});assert.equal(t.followup.status,'completed');assert.equal(t.status,'terminated');assert.equal(t.followup.history.length,4);
});
test('工作台读取各源、拒绝伪造角色、按设备范围过滤并随来源变化刷新',async()=>{
 const {createApi}=require('../../docs/eam/prototype/mock/workbench-api.js'),ctx={role:'inspector',person:'P02',equipmentIds:['EQ-001']};
 let status='pending';const api=createApi({context:()=>ctx,people:[{id:'P02',name:'李敏',roles:['inspector']}],equipment:()=>[{id:'EQ-001',name:'混合机',ownerId:'P02'}],inspection:{request:async()=>({items:[{id:'I1',equipmentId:'EQ-001',assignee:'P02',status,window:'2026-10-11 08:00—20:00'},{id:'I2',equipmentId:'EQ-002',assignee:'P02',status:'pending'}]})},maintenance:{request:async(m,p)=>p.includes('config')?{asOfDate:'2026-10-11',taskStates:[]}: {items:[],total:0}},problem:{request:async()=>({rows:[]})}});
 let r=await api.request('GET','/workbench');assert.deepEqual(r.items.map(x=>x.sourceId),['I1']);assert.equal(r.metrics.pending,1);status='complete';r=await api.request('GET','/workbench');assert.equal(r.items.length,0);
 ctx.role='supervisor';await assert.rejects(api.request('GET','/workbench'),/角色/);
});
