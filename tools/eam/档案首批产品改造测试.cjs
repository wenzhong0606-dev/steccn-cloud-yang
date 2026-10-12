const {test}=require('node:test'),assert=require('node:assert/strict');
const problem=require('../../docs/eam/prototype/mock/problem-api.js');
const projection=require('../../docs/eam/prototype/mock/archive-maintenance-api.js');
const pm=require('../../docs/eam/prototype/mock/maintenance-archive-api.js');
test('保养退回后V1履历保留原提交证据，不混入V2记录',async()=>{
 let ctx={role:'supervisor',person:'P04',scenario:'normal'};
 const api=require('../../docs/eam/prototype/mock/maintenance-api.js').createApi({delay:0,businessDate:()=>'2026-10-31',context:()=>ctx});
 const g=await api.request('POST','/maintenance/plans/MP-002/generate',{activeVersion:1,expectedRevision:1,windowKeys:['MP-002:V1:2026-10-31'],idempotencyKey:'archive-history'}),url='/maintenance/tasks/'+g.items[0].taskId;
 ctx={role:'technician',person:'P03',scenario:'normal'};
 let t=await api.request('POST',url+'/start',{expectedRevision:1,confirmed:true,permitId:'PERMIT-EQ-001'});
 t=await api.request('PATCH',url+'/execution',{expectedRevision:t.revision,executionVersion:1,results:[{itemId:'MI-001',conclusion:'normal',evidence:'V1-原始照片'}],laborMinutes:20,trial:'V1-原试运行'});
 t=await api.request('POST',url+'/submit',{expectedRevision:t.revision,executionVersion:1,reason:'首次提交'});
 ctx={role:'supervisor',person:'P04',scenario:'normal'};
 t=await api.request('POST',url+'/acceptance',{expectedRevision:t.revision,executionVersion:1,decision:'returned',itemsVerified:true,trialVerified:true,evidence:'退回复核',reason:'重新核实'});
 const v1=(await pm.read(api,'EQ-001')).records.find(r=>r.id==='SUB-'+t.id+'-V1');
 const fields=v1.detailSections.flatMap(s=>s.fields);
 assert.equal(fields.find(f=>f[0]==='提交证据')[1],'V1-原始照片');
 assert.equal(fields.find(f=>f[0]==='提交结论')[1],'V1-原试运行');
 ctx={role:'technician',person:'P03',scenario:'normal'};
 t=await api.request('POST',url+'/resume',{expectedRevision:t.revision,confirmed:true,permitId:'PERMIT-EQ-001',reason:'重新安全确认'});
 t=await api.request('PATCH',url+'/execution',{expectedRevision:t.revision,executionVersion:2,results:[{itemId:'MI-001',conclusion:'normal',evidence:'V2-新照片'}],laborMinutes:25,trial:'V2-新试运行'});
 await api.request('POST',url+'/submit',{expectedRevision:t.revision,executionVersion:2,reason:'重做提交'});
 const old=(await pm.read(api,'EQ-001')).records.find(r=>r.id===v1.id);
 assert.equal(old.detailSections.flatMap(s=>s.fields).find(f=>f[0]==='提交证据')[1],'V1-原始照片');
 assert(!JSON.stringify(old.detailSections).includes('V2-新照片'));
});
test('故障已恢复时间产生停机小时，独立验证证据进入专业详情',async()=>{
 const api=problem.createApi({delay:0});
 await api.request('POST','/defects/DF-1002/verify',{expectedVersion:2,decision:'pass',evidence:'独立复核证据-CHECK-22',reason:'已核对'});
 const data=await projection.read(api,'EQ-001');
 assert.equal(data.records.find(r=>r.id==='FL-1002').downtime,0.75);
 assert(JSON.stringify(data.records.find(r=>r.id==='DF-1002').detailSections).includes('独立复核证据-CHECK-22'));
});
test('未结业务对象计数与档案默认未结清单一致，分诊对象不重复计入',async()=>{
 const data=await projection.read(problem.createApi({delay:0}),'EQ-001');
 assert.equal(data.records.filter(r=>r.isPending===true).length,data.pending);
 assert(data.records.every(r=>r.objectType&&r.query.equipment==='EQ-001'));
 const other=await projection.read(problem.createApi({delay:0}),'EQ-002');
 assert.equal(other.pending,0);assert.equal(other.records.length,0);
});
test('保养计划与任务履历明确对象类型，默认任务统计不混入计划',async()=>{
 const api=require('../../docs/eam/prototype/mock/maintenance-api.js').createApi({delay:0});
 const data=await pm.read(api,'EQ-001');
 assert.equal(data.records.filter(r=>r.objectType==='任务').length,data.taskCount);
 assert(data.records.filter(r=>r.source==='保养计划').every(r=>r.objectType==='计划'&&r.detailSections));
 assert.equal(data.records.filter(r=>r.objectType==='计划').length,3);
});
