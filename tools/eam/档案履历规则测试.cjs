const test=require('node:test'),assert=require('node:assert/strict');
const r=require('../../docs/eam/prototype/shared/archive-trace-rules.js');
test('普通字段修改不混入生命周期，业务状态变更保留',()=>{
 assert.equal(r.business({name:'参数修改',before:{value:1},after:{value:2}}),false);
 assert.equal(r.business({before:{status:'running'},after:{status:'stopped'}}),true);
 assert.equal(r.business({milestone:true,name:'首次投用'}),true);
 assert.equal(r.business({name:'已批准',action:'approve'}),false);
 assert.equal(r.business({before:'active',after:'stopped'}),true);
 assert.deepEqual(r.diff('active','stopped'),[{field:'lifecycle',before:'active',after:'stopped'}]);
});
test('差异保留零值、嵌套字段、新增删除，不把缺失当空字符串',()=>{
 const changes=r.diff({x:0,typed:{value:1},removed:false},{x:1,typed:{value:2},new:''});
 assert.deepEqual(changes.map(x=>x.field),['new','removed','typed.value','x']);
 assert.equal(changes.find(x=>x.field==='x').before,'0');
 assert.equal(changes.find(x=>x.field==='new').before,'未记录');
 assert.equal(changes.find(x=>x.field==='new').after,'（空值）');
});
test('筛选共享时间条件，未知时间不伪造；关联链按真实引用分组',()=>{
 const rows=[{id:'A',eventAt:'2026-10-10 07:00',actor:'张工',before:{x:0},after:{x:1}},{id:'B',eventAt:'—'}];
 assert.deepEqual(r.filter(rows,{from:'2026-10-10',to:'2026-10-10',actor:'张',field:'x'}).map(x=>x.id),['A']);
 assert.throws(()=>r.filter(rows,{from:'2026-10-11',to:'2026-10-10'}),/起始/);
 const groups=r.groups([{id:'A',kind:'defects',links:[{kind:'failures',id:'B'}]},{id:'B',kind:'failures'},{id:'C',kind:'hazards'}]);
 assert.deepEqual(groups.map(g=>g.map(x=>x.id)),[['A','B'],['C']]);
});
