/* 验证跨域历史样本身份和关键业务关系，不改动会话。 */
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const context={window:{}};
vm.runInNewContext(fs.readFileSync(require('node:path').resolve(__dirname,'../../docs/eam/prototype/mock/archive-data.js'),'utf8'),context);
const fixture=context.window.EAM_ARCHIVE_DEMO,eq=fixture.equipment['EQ-001'];
const ids=new Set();let count=0;
for(const rows of Object.values(eq))for(const row of rows){assert.equal(row.length,4);assert(!ids.has(row[0]),'重复编号 '+row[0]);ids.add(row[0]);count++;}
for(const [domain,id,refs] of [['failure','FLT-001',['REP-001']],['repair','REP-001',['FLT-001','RPL-001']],['replacement','RPL-001',['REP-001']],['maintenance','PMR-001',['PM-001']],['work','IR-001',['IP-001']]]){
 const row=eq[domain].find(x=>x[0]===id);assert(row);for(const ref of refs){assert(ids.has(ref));assert(row.join(' ').includes(ref));}
}
assert.equal(10200-10000,200);assert.equal(8000-200,7800);
assert(eq.replacement.find(x=>x[0]==='LIFE-001')[2].includes('7800'));
assert.equal(new Date('2026-09-25T00:00:00Z').getTime()+30*86400000,new Date('2026-10-25T00:00:00Z').getTime());
assert(!fixture.equipment['NONEXISTENT']);
console.log(`模拟档案：${Object.keys(eq).length}领域 / ${count}记录；身份唯一、故障维修更换及计划关联通过。`);
