const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const file=path.resolve(__dirname,'../../docs/eam/prototype/shared/archive-trend-rules.js'),rules=fs.existsSync(file)?require(file):{};
const config={pointId:'TEMP',metric:'temperature',unit:'°C',quality:'GOOD',upper:75};
test('不合格读数保留在原始数据且切断曲线，零值不丢失',()=>{
 assert.equal(typeof rules.select,'function','缺少运行监测范围规则');
 const rows=[{id:'a',eventAt:'2026-10-10 07:00',temperature:0,quality:'GOOD'},{id:'b',eventAt:'2026-10-10 07:30',temperature:88,quality:'BAD'},{id:'c',eventAt:'2026-10-10 08:00',temperature:76,quality:'GOOD'}];
 const result=rules.select(rows,config,{range:'all'});assert.equal(result.raw.length,3);assert.equal(result.valid.length,2);assert.equal(result.segments.length,2);assert.equal(result.valid[0].temperature,0);assert.equal(result.exceeded.length,1);
});
test('时间范围以采样快照为基准，并按测点范围隔离',()=>{assert.equal(typeof rules.select,'function');const rows=[{pointId:'TEMP',eventAt:'2026-10-09 07:00',temperature:20,quality:'GOOD'},{pointId:'TEMP',eventAt:'2026-10-10 07:00',temperature:21,quality:'GOOD'},{pointId:'OTHER',eventAt:'2026-10-10 07:00',temperature:99,quality:'GOOD'}];assert.equal(rules.select(rows,config,{range:'day',asOf:'2026-10-10 08:00'}).raw.length,1);});
test('自定义时间逆序拒绝，未知时间不伪造采样或参与有限范围',()=>{assert.equal(typeof rules.select,'function');assert.throws(()=>rules.select([],config,{range:'custom',from:'2026-10-10T09:00',to:'2026-10-10T08:00'}),/起始/);const rows=[{eventAt:'未记录',temperature:20,quality:'GOOD'}];assert.equal(rules.select(rows,config,{range:'all'}).raw.length,1);assert.equal(rules.select(rows,config,{range:'all'}).valid.length,0);assert.equal(rules.select(rows,config,{range:'day',asOf:'2026-10-10 08:00'}).raw.length,0);});
