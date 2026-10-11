/* 模板规则为演示JSON，不代表实际厂家设计边界。 */
(function(root){
 const json={ownerOptions:[{value:'P04',label:'周平'},{value:'P05',label:'赵工'}],parameters:{'PAR-MX-01':{type:'number',unit:'kW',min:0.1,max:500,precision:2,template:'MIXER-V3'},'PAR-MX-02':{type:'number',unit:'m³',min:0.01,max:100,precision:3,template:'MIXER-V3'},'PAR-MX-03':{type:'number',unit:'r/min',min:1,max:300,precision:2,template:'MIXER-V3'},'PAR-MX-04':{type:'range',unit:'°C',min:-50,max:150,precision:1,template:'MIXER-V3'}},fields:[['code','设备编码','always'],['name','设备名称','always'],['model','型号规格','always'],['location','所属位置','always'],['ownerId','设备负责人','always'],['manufacturer','制造商','in_service'],['serial','序列号','in_service'],['commissionedAt','投用日期','in_service']]};
 function validate(rule,content){
  if(!rule)throw Error('尚未绑定参数定义，不能按自由文本修改');
  const text=String(content||'').trim(),suffix=' '+rule.unit;if(!text.endsWith(suffix))throw Error('单位必须为 '+rule.unit);
  const raw=text.slice(0,-suffix.length),parts=rule.type==='range'?raw.split('～'):[raw];if(parts.length!==(rule.type==='range'?2:1))throw Error('参数范围必须同时填写下限与上限');
  const values=parts.map(part=>{if(!/^-?\d+(?:\.\d+)?$/.test(part))throw Error('参数必须是有效数值');if((part.split('.')[1]||'').length>rule.precision)throw Error('最多保留 '+rule.precision+' 位小数');const value=Number(part);if(!Number.isFinite(value)||value<rule.min||value>rule.max)throw Error('数值须在 '+rule.min+'～'+rule.max+' '+rule.unit+' 内');return value;});
  if(values.length===2&&values[0]>values[1])throw Error('范围下限不能大于上限');return {type:rule.type,unit:rule.unit,value:values.length===1?values[0]:null,lower:values.length===2?values[0]:null,upper:values.length===2?values[1]:null,template:rule.template};
 }
 const exports={json,validate};if(typeof module==='object'&&module.exports)module.exports=exports;else root.EamArchiveRules=exports;
})(typeof window==='undefined'?globalThis:window);
