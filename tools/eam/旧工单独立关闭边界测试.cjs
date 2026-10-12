// 回归：历史演示引擎的技术验收与业务关闭必须遵守独立对象状态边界。
// 执行：node --test tools/eam/旧工单独立关闭边界测试.cjs
const test=require('node:test');
const assert=require('node:assert/strict');
const D=require('../../docs/eam/prototype/shared/domain.js');
const actor={inspector:{role:'inspector',person:'P02'},supervisor:{role:'supervisor',person:'P04'},technician:{role:'technician',person:'P03'}};
const run=(s,who,action,p={})=>D.command(s,actor[who],action,p,'2026-10-12T04:00:00Z');
function completedOrder(severity='medium'){
  const s=D.initial();
  run(s,'inspector','report',{description:'设备异响',severity,observedAt:'2026-10-12T03:00:00Z'});
  run(s,'supervisor','dispatch',{assignee:'P03',scope:'检查并恢复'});
  run(s,'technician','start',{isolation:true,guard:true});
  run(s,'technician','finish',{diagnosis:'轴承润滑不足',repair:'补充润滑',trial:'空载测试正常',labor:120});
  return s;
}
const approval={restored:true,trialConfirmed:true,defectVerified:true,opinion:'现场复核合格'};
test('技术验收仅进入accepted，关联异常仍独立待处理',()=>{
  const s=completedOrder();
  const before=s.reports[0].status;
  run(s,'supervisor','accept',approval);
  assert.equal(s.orders[0].status,'accepted');
  assert.equal(s.executions[0].status,'accepted');
  assert.equal(s.reports[0].status,before);
  assert.equal(s.acceptances.length,1);
  assert.equal(s.orders[0].closedAt,undefined);
});
test('来源异常未独立关闭时，历史工单业务关闭必须阻断并保持事务不变',()=>{
  const s=completedOrder();
  run(s,'supervisor','accept',approval);
  const snapshot=JSON.stringify(s);
  assert.throws(()=>run(s,'supervisor','closeOrder',{reason:'准备关闭'}),/关联异常未独立关闭/);
  assert.equal(JSON.stringify(s),snapshot);
});
test('历史工单仅在来源异常独立关闭后才允许业务关闭',()=>{
  const s=completedOrder();
  run(s,'supervisor','accept',approval);
  // 模拟外部独立异常核实完成；该旧引擎不提供新模型的逐问题验证命令。
  s.reports[0].status='closed';
  assert.throws(()=>run(s,'supervisor','closeOrder',{}),/业务关闭依据/);
  run(s,'supervisor','closeOrder',{reason:'独立验证已完成'});
  assert.equal(s.orders[0].status,'closed');
  assert.equal(s.orders[0].closeReason,'独立验证已完成');
  assert.equal(s.reports[0].status,'closed');
  assert.throws(()=>run(s,'supervisor','closeOrder',{reason:'再次关闭'}),/仅已技术验收/);
});
test('紧急限制不会随技术验收和旧工单业务关闭隐式解除',()=>{
  const s=completedOrder('urgent');
  assert.equal(s.restrictions[0].status,'active');
  run(s,'supervisor','accept',approval);
  assert.equal(s.restrictions[0].status,'active');
  s.reports[0].status='closed';
  run(s,'supervisor','closeOrder',{reason:'核实完成'});
  assert.equal(s.restrictions[0].status,'active');
});
test('旧引擎点检草稿及提交继续可用',()=>{
  const s=D.initial();
  run(s,'inspector','draft',{temperature:'65',lubrication:'normal',guard:'normal'});
  assert.equal(s.draft.temperature,'65');
  run(s,'inspector','inspect',{temperature:'65',lubrication:'normal',guard:'normal'});
  assert.equal(s.task.status,'complete');
  assert.equal(s.reports.length,0);
});
