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
 const completeVersion='COMPLETE-DEMO-V3';
 const uncovered=[['state','状态与许可'],['structure','设备结构'],['installation','位置与安装'],['work','点检'],['maintenance','保养'],['failure','缺陷与故障'],['repair','维修'],['bom','设备BOM'],['replacement','更换与寿命'],['trend','运行监测'],['risk','安全合规'],['cost','资产成本'],['history','生命周期'],['related','关联业务'],['audit','变更审计']];
 const ranks={'待补录':0,'待修正':1,'未核验':2,'待配置':2,'通过':3,'不适用':4};
 function complete(base,ctx){
  const equipment=ctx.equipment,known=equipment.classification==='生产设备 / 混合机'&&equipment.model==='MX-2400',inService=['active','stopped','pending_scrap','scrapped'].includes(equipment.lifecycle),knownStage=inService||equipment.lifecycle==='draft';
  const rows=base.filter(row=>row.id!=='TEMPLATE').map(row=>{const copy={...row,group:row.field?'基础资料':'技术参数',applicability:row.status==='不适用'?'optional':'required'};if(row.field&&json.fields.some(([key,,stage])=>key===row.field&&stage!=='always')&&!knownStage){copy.status=copy.content='未核验';copy.applicability='unknown';copy.evidence='生命周期未匹配适用规则，不能判定不适用';}if(!row.field&&(!known||!knownStage)){copy.status=copy.content='未核验';copy.applicability='unknown';copy.evidence='设备分类或生命周期未匹配参数适用规则；原数值校验结果不能证明适用关系';}return copy;});
  const add=(id,name,group,status,evidence,extra={})=>rows.push({id,name,group,status,content:status,evidence,source:'完整性演示规则 '+completeVersion,revision:1,applicability:status==='不适用'?'optional':'unknown',...extra});
  const binding=ctx.binding,bound=known&&binding?.verified===true&&binding.template==='MIXER-V3'&&binding.classification===equipment.classification&&binding.model===equipment.model;
  add('TEMPLATE','参数模板适用关系','技术参数',bound?'通过':'未核验',bound?'已有明确适用关系及核验标记；规则仍为本地演示配置':'来源没有提供已核验的分类、型号与模板绑定；已有参数行不能证明适用关系', {target:'equipment-templates',query:{equipment:equipment.id},applicability:known?'required':'unknown'});
  if(known)for(const id of Object.keys(ctx.definitions||{}))if(!(ctx.parameters||[]).some(row=>row.id===id))add('MISSING-'+id,'必需参数 '+id,'技术参数','待补录','示例混合机模板规定的参数缺失',{repairTab:'parameters',repairLabel:'打开技术参数',applicability:'required'});
  for(const [category,name,tab]of [['image','设备图片资料','media'],['document','设备文档资料','documents'],['model','设备3D模型','media']]){
   const files=(ctx.files||[]).filter(file=>file.equipmentId===equipment.id&&file.status==='active'&&file.category===category),actual=files.filter(file=>!file.isSample),verified=actual.filter(file=>file.contentVerified===true);
   const applicable=known&&knownStage&&inService&&category!=='model';let status,evidence;
   if(!known||!knownStage){status='未核验';evidence='设备分类或生命周期未匹配演示规则，须配置专业适用条件';}
   else if(!applicable){status='不适用';evidence=category==='model'?'本演示混合机规则不要求3D模型；未上传模型不构成缺口':'草稿阶段不要求在用资料；投用后需重新校验';}
   else if(verified.length){status='通过';evidence='有效关联 '+files.length+' 条，其中非示例内容读取成功 '+verified.length+' 条；仅核验内容存在，不评价资料业务有效性';}
   else if(files.length){status='未核验';evidence='有效关联 '+files.length+' 条，示例 '+(files.length-actual.length)+' 条；元数据、示例或本机内容读取失败不能证明必需资料齐全';}
   else {status='待补录';evidence='没有本设备有效关联的'+name+'；已移除及其他设备资料不参与检查';}
   add('FILE-'+category,name,'资料与媒体',status,evidence,{repairTab:tab,repairLabel:'打开'+(tab==='media'?'图片与3D':'文档资料'),applicability:applicable?'required':status==='不适用'?'optional':'unknown'});
  }
  const meters=ctx.meters||[],valid=meters.length&&meters.every(point=>point.pointId&&point.unit&&point.metric);
  add('METER-CONFIG','测点配置完整性','测点与运行',!known||!knownStage?'未核验':!inService?'不适用':valid?'通过':meters.length?'待修正':'待补录',!known||!knownStage?'设备分类或生命周期未匹配测点适用规则':!inService?'草稿阶段不要求运行测点':valid?'已配置 '+meters.length+' 个测点的编号、单位及读值字段；不代表实时信号正常':'测点缺少编号、单位或读值字段，或尚未配置',{repairTab:'meters',repairLabel:'打开测点与计量器',applicability:known&&inService?'required':knownStage&&known?'optional':'unknown'});
  for(const [tab,name]of uncovered)add('DOMAIN-'+tab,name+'专业规则覆盖','专业规则待配置','未核验','尚未配置该专业的必需项和适用条件；有记录、无记录均不能推断完整性',{repairTab:tab,repairLabel:'打开'+name});
  for(const row of rows){row.ruleVersion=completeVersion;if(row.status==='待配置')row.status=row.content='未核验';}
  return rows.sort((a,b)=>(ranks[a.status]??2)-(ranks[b.status]??2)||(a.applicability==='required'?0:1)-(b.applicability==='required'?0:1)||a.group.localeCompare(b.group,'zh-CN'));
 }
 function completeSummary(rows,checkedAt){const count=status=>rows.filter(row=>row.status===status).length;return {ruleVersion:completeVersion,checkedAt,total:rows.length,required:rows.filter(row=>row.applicability==='required').length,passed:count('通过'),missing:count('待补录'),invalid:count('待修正'),unverified:count('未核验'),notApplicable:count('不适用'),coverage:'仅校验登记的基础字段、参数数值和资料／测点配置；未配置专业规则保留未核验，不计算全档案完整率'};}
 const exports={json,validate,complete,completeSummary};if(typeof module==='object'&&module.exports)module.exports=exports;else root.EamArchiveRules=exports;
})(typeof window==='undefined'?globalThis:window);
