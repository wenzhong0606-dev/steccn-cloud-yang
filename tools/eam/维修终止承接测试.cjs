const {test}=require('node:test'),assert=require('node:assert/strict'),{createApi}=require('../../docs/eam/prototype/mock/problem-api.js');
test('终止须逐问题承接；错误数据原子回滚，批准后问题保留并转交责任',async()=>{
 const ctx={role:'supervisor',person:'P04',scenario:'normal'},api=createApi({delay:0,context:()=>ctx});
 const path='/work-orders/WO-2001',before=await api.request('GET',path),base={expectedVersion:1,reason:'停机窗口调整，另行安排检修',idempotencyKey:'term-one'};
 await assert.rejects(api.request('POST',path+'/termination-applications',base),/承接|安排/);
 const residuals=before.relatedProblems.map(p=>({kind:p.kind,id:p.id,expectedProblemVersion:p.version,ownerId:'P05',dueAt:'2030-12-20',control:'继续保持隔离挂牌',controlUntil:'2030-12-25T23:59:59+08:00',followupOrderId:''}));
 await assert.rejects(api.request('POST',path+'/termination-applications',{...base,residuals:residuals.slice(1)}),/逐项|承接/);
 await assert.rejects(api.request('POST',path+'/termination-applications',{...base,residuals:residuals.map((r,i)=>i? r:{...r,controlUntil:'2026-01-01'})}),/控制/);
 assert.equal((await api.request('GET',path)).status,'assigned');assert.equal((await api.request('GET','/defects/DF-1001')).version,1);
 const pending=await api.request('POST',path+'/termination-applications',{...base,residuals});assert.equal(pending.status,'assigned');const done=await api.request('POST',path+'/apply-approval-result',{instanceId:pending.terminationApproval.instanceId,applicationRevision:1,sample:'approved'});assert.equal(done.status,'cancelled');assert.equal(done.terminationArrangements.length,3);
 const p=await api.request('GET','/hazards/HZ-1001');assert.equal(p.ownerId,'P05');assert.equal(p.status,'rectifying');assert.equal(p.restriction.status,'active');assert.equal(p.followupArrangement.sourceOrderId,'WO-2001');assert.equal(p.version,2);
 await assert.rejects(api.request('POST',path+'/termination-applications',{...base,idempotencyKey:'another',expectedVersion:done.version,residuals}),/终止|权限|审批/);
});

