/* 所有档案读取经此模拟接口；JSON资源与页面控制器分离。 */
window.EAM_ARCHIVE_JSON = {
  media: {'EQ-001':[{id:'SAMPLE-1',name:'设备外观（示意）',revision:'V1',category:'image',status:'active',previewUrl:'../assets/mixer.svg'},{id:'SAMPLE-2',name:'轴承部位（示意）',revision:'V1',category:'image',status:'active',previewUrl:'../assets/mixer-detail.svg'},{id:'SAMPLE-DOC',name:'设备说明示例.txt',revision:'V1.2',category:'document',status:'active',previewUrl:'../assets/设备说明示例.txt'}]},
  profiles: {'EQ-001':{classification:'生产设备 / 混合机',department:'原料生产部',management:'设备动力部',team:'机械维修班',assetCode:'AS-2024-018',functionalPosition:'FP-MIX-01',image:'../assets/mixer.svg',supplier:'示例机械有限公司',purchaseContract:'DEMO-PO-001',acceptanceDate:'2024-03-15',acceptanceCode:'ACC-001',warrantyUntil:'2025-03-14',designLife:'15 年'}},
  schemas: {
    completeness:[['name','检查项'],['status','检查结果'],['evidence','适用规则 / 校验说明']],
    parameters:[['name','参数名称'],['content','参数值 / 单位'],['kind','参数性质'],['valueType','数据类型'],['template','模板版本']],
    bom:[['name','物料名称'],['material','物料编码'],['quantity','数量 / 单位'],['part','装配部位']],
    replacement:[['name','更换部位'],['installedAt','更换日期'],['oldSerial','原件序列号'],['newSerial','新件序列号'],['order','维修工单']],
    meters:[['name','测点 / 计量器'],['part','归属部位'],['reading','最新值'],['quality','数据质量']],
    trend:[['name','采样时间'],['temperature','轴承温度 / °C'],['hours','运行小时 / h'],['quality','数据质量']],
    cost:[['name','费用项目'],['amount','金额 / CNY'],['accountingAt','核算日期'],['voucher','来源凭证']],
    installation:[['name','安装事件'],['eventAt','执行日期'],['position','功能位置'],['acceptance','验收单据']],
    failure:[['name','故障描述'],['eventAt','发生时间'],['downtime','停机 / h'],['status','处置状态']],
    repair:[['name','维修内容'],['eventAt','执行日期'],['executor','执行人'],['status','工单状态']],
    maintenance:[['name','策略 / 记录'],['frequency','周期 / 日期'],['assignee','岗位 / 主责'],['dueAt','下次到期']]
  },
  trendConfig:{'EQ-001':{pointId:'PT-TEMP-01',name:'驱动端轴承温度',unit:'°C',metric:'temperature',upper:75,thresholdSource:'MIXER-V3 / PAR-MX-04',quality:'GOOD'}},
  structured: {
    'PAR-MX-01':{kind:'额定值',template:'MIXER-V3'},'PAR-MX-02':{kind:'设计值',template:'MIXER-V3'},'PAR-MX-03':{kind:'额定值',template:'MIXER-V3'},'PAR-MX-04':{kind:'允许范围',template:'MIXER-V3'},
    'BOM-001':{material:'MAT-6208',quantity:'1 件',part:'驱动端轴承'},'BOM-002':{material:'MAT-BELT-2400',quantity:'2 条',part:'传动组件'},'BOM-003':{material:'MAT-GREASE-EP2',quantity:'0.5 kg',part:'润滑系统'},
    'RPL-001':{installedAt:'2026-09-18',oldSerial:'BRG-20240315',newSerial:'BRG-20260918',order:'REP-001'},
    'PT-TEMP-01':{part:'驱动端轴承',reading:'68.2 °C',quality:'GOOD'},'CTR-HOURS-01':{part:'设备整机',reading:'10200 h',quality:'GOOD'},
    'EVT-001':{eventAt:'2024-03-15 08:00'},'EVT-002':{eventAt:'2024-03-18 08:00'},'EVT-003':{eventAt:'2026-09-18 11:10'},
    'READ-001':{eventAt:'2026-10-10 07:00',temperature:67.8,hours:10199,quality:'GOOD'},'READ-002':{eventAt:'2026-10-10 07:30',temperature:68,hours:10199.5,quality:'GOOD'},'READ-003':{eventAt:'2026-10-10 08:00',temperature:68.2,hours:10200,quality:'GOOD'},
    'COST-001':{amount:185000,accountingAt:'2024-03-18',voucher:'FIN-DEMO-001'},'COST-002':{amount:780,accountingAt:'2026-09-30',voucher:'REP-001'},
    'INS-001':{eventAt:'2024-03-15',position:'FP-MIX-01',acceptance:'ACC-001'},'INS-002':{eventAt:'2026-09-18',position:'FP-MIX-01',acceptance:'ACC-002'},
    'FLT-001':{eventAt:'2026-09-18 09:10',downtime:2,status:'已关闭'},'FLT-002':{eventAt:'2026-08-12',downtime:0,status:'已关闭'},
    'REP-001':{eventAt:'2026-09-18',executor:'王强',status:'已关闭'},'REP-002':{eventAt:'2026-08-12',executor:'王强',status:'已关闭'},
    'PM-001':{frequency:'每30天',assignee:'原料维修岗 / 王强',dueAt:'2026-10-25'},'PMR-001':{frequency:'2026-09-25',assignee:'王强',dueAt:'已完成'},'PM-002':{frequency:'每90天',assignee:'机械维修岗',dueAt:'2026-12-17'}
  },
  lifetime: {'EQ-001':{instance:'BRG-20260918',target:8000,baseline:10000,current:10200,unit:'h',asOf:'2026-10-10 08:00'}},
  domains: Object.fromEntries(Object.entries(EAM_ARCHIVE_DEMO.equipment).map(([id,domains])=>[id,Object.fromEntries(Object.entries(domains).map(([key,rows])=>[key,rows.map(r=>({id:r[0],name:r[1],content:key==='parameters'?r[2].split(' / ')[0]:r[2],evidence:r[3],source:'档案登记',updatedAt:EAM_ARCHIVE_DEMO.asOf,revision:1}))]))])),
  stateLabels:{active:'在用',stopped:'停用',pending_scrap:'待报废',scrapped:'已报废',draft:'草稿'},
  fieldLabels:{name:'设备名称',code:'设备编码',model:'型号规格',serial:'序列号',manufacturer:'制造商',commissionedAt:'投用日期',owner:'设备负责人',location:'所属位置',classification:'设备分类',department:'使用部门',management:'管理部门',team:'维修班组',assetCode:'资产编号',functionalPosition:'功能位置'},
  stateSources:[{key:'lifecycle',name:'生命周期',source:'设备台账',time:'2026-10-10 08:00',tone:'success'}, {key:'operating',name:'运行状态',source:'运行信号',time:'2026-10-10 08:00',tone:'success'}, {key:'health',name:'健康状态',source:'异常与故障记录',time:'2026-10-10 08:00',tone:'success'}, {key:'metrology',name:'计量状态',source:'设备用途登记',time:'2026-10-10 08:00',tone:'neutral'}, {key:'permission',name:'使用许可',source:'专业限制',time:'2026-10-10 08:00',tone:'success'}]
};
window.EamArchiveApi = (()=>{
  const clone=x=>JSON.parse(JSON.stringify(x));
  const db=EAM_ARCHIVE_JSON;
  const storedKey='eam-archive-json-v2';
  try{const saved=JSON.parse(sessionStorage.getItem(storedKey)||'null');if(saved?.version===2){db.domains=saved.domains;}}catch{}
  const save=()=>sessionStorage.setItem(storedKey,JSON.stringify({version:2,domains:db.domains}));
  async function request(method,path,body={}){
    await new Promise(resolve=>setTimeout(resolve,180));
    const scenario=Eam.s.scenario;
    if(scenario==='forbidden')throw Error('无设备档案访问权限');
    if(scenario==='failure')throw Error('档案服务暂时不可用，请重试');
    if(scenario==='loading')return {code:202,data:null};
    if(method==='GET'&&path==='/equipment')return {code:200,data:{items:clone(Eam.s.equipmentRecords)}};
    const match=path.match(/^\/equipment\/([^/]+)\/(archive|domains\/([^/]+))$/);
    if(!match)throw Error('未定义的模拟接口');
    const [,id,,domain]=match,s=Eam.s;
    const equipment=s.equipmentRecords.find(x=>x.id===id);if(!equipment)throw Error('设备不存在或不在授权范围');
    if(method==='PATCH'){
      if(domain==='base'){
        if(!['engineer','supervisor'].includes(s.role))throw Error('无设备资料维护权限');
        if(scenario==='conflict'||equipment.revision!==body.revision)throw Error('设备版本冲突，请刷新后重试');
        const rule=EamArchiveRules.json.fields.find(([key])=>key===body.field);if(!rule)throw Error('此字段需由来源业务维护');
        if(!String(body.value||'').trim()||!body.reason?.trim())throw Error('字段值与补录原因必填');
        if(body.field==='commissionedAt'&&(!/^\d{4}-\d{2}-\d{2}$/.test(body.value)||!Number.isFinite(Date.parse(body.value))||new Date(body.value).toISOString().slice(0,10)!==body.value))throw Error('请填写有效的投用日期YYYY-MM-DD');
        const additional={};for(const [key,value]of Object.entries(body.values||{})){if(!EamArchiveRules.json.fields.some(([field,,stage])=>field===key&&stage==='always')||String(equipment[key]||'').trim())throw Error('额外补录仅允许尚缺的基础必填字段');additional[key]=value;}
        EamDomain.command(s,{role:s.role,person:s.person},'saveEquipment',{...equipment,...additional,[body.field]:body.value,id,revision:body.revision,reason:body.reason});Eam.save();return {code:200,data:clone(s.equipmentRecords.find(record=>record.id===id))};
      }
      if(!['engineer','supervisor'].includes(s.role))throw Error('没有参数维护权限');
      if(domain!=='parameters')throw Error('此领域须由来源业务维护');
      if(scenario==='conflict')throw Error('记录版本已变化，请刷新后重新修改');
      if(equipment.lifecycle==='scrapped')throw Error('已报废设备不可修改参数');
      const row=db.domains[id]?.parameters.find(x=>x.id===body.id);if(!row)throw Error('参数不存在');
      if(row.revision!==body.revision)throw Error('版本冲突，请刷新后重试');
      if(!body.content?.trim()||!body.reason?.trim())throw Error('参数值和变更原因必填');
      const definition=EamArchiveRules.json.parameters[row.id],typed=EamArchiveRules.validate(definition,body.content);
      const before=row.content;let beforeTyped=null;try{beforeTyped=EamArchiveRules.validate(definition,before);}catch{}row.content=body.content.trim();row.typed=typed;row.revision++;row.updatedAt=new Date().toLocaleString('sv-SE');
      db.domains[id].audit ||= [];db.domains[id].audit.unshift({id:'AUD-'+crypto.randomUUID(),name:'技术参数修改',objectId:row.id,before:{content:before,typed:beforeTyped},after:{content:row.content,typed:clone(typed)},templateVersion:definition.template,content:before+' → '+row.content,evidence:body.reason+' / '+EamDomain.personName(s.person),source:'参数维护',updatedAt:row.updatedAt,revision:1});save();
      return {code:200,data:clone(row)};
    }
    if(method==='POST'&&domain==='lifecycle'){
      if(id!=='EQ-001')throw Error('该设备生命周期流程尚未开放');
      EamDomain.command(s,{role:s.role,person:s.person},'lifecycle',body);Eam.save();return {code:200,data:{lifecycle:s.lifecycle}};
    }
    if(method==='POST'&&domain==='restrictions'){
      if(id!=='EQ-001')throw Error('限制来源不存在');
      EamDomain.command(s,{role:s.role,person:s.person},'releaseRestriction',body);Eam.save();return {code:200,data:{id:body.id}};
    }
    if(method!=='GET')throw Error('不支持的接口方法');
    const projection=typeof EamArchiveMaintenance!=='undefined'?await EamArchiveMaintenance.read(EamProblemApi,id):null;
    const pmProjection=typeof EamMaintenanceArchive!=='undefined'&&(!domain||['maintenance','history','audit'].includes(domain))?await EamMaintenanceArchive.read(EamMaintenanceApi,id):null;
    if(domain){
      if(domain==='maintenance'&&pmProjection)return {code:200,data:{items:scenario==='empty'?[]:pmProjection.records,total:scenario==='empty'?0:pmProjection.records.length,columns:pmProjection.columns,source:'保养计划与任务模拟接口',asOf:pmProjection.records.map(row=>row.updatedAt).filter(value=>Number.isFinite(Date.parse(value))).sort((a,b)=>Date.parse(b)-Date.parse(a))[0]||'未记录',readAt:new Date().toLocaleString('sv-SE')}};
      if(domain==='restrictions'){const items=(id==='EQ-001'?s.restrictions.map(x=>({id:x.id,name:x.reason,content:x.status==='active'?'有效限制':'已解除',status:x.status,evidence:'限制范围 '+x.scope,source:'专业限制',updatedAt:x.createdAt||'—',revision:1,canRelease:true,owner:x.ownerId?EamDomain.personName(x.ownerId):'未记录',dueAt:x.validUntil||'未记录',releaseCondition:'来源专业验证及有权人员放行'})):[]).concat(projection?.restrictions||[]);return {code:200,data:{items,total:items.length}};}
      if(domain==='media')return {code:200,data:{items:clone(scenario==='empty'?[]:[...(db.media[id]||[]).map(f=>({...f,equipmentId:id,isSample:true})),...s.assetFiles.filter(f=>f.equipmentId===id)] )}};
      if(domain==='cost'&&!['engineer','manager','auditor'].includes(s.role))throw Object.assign(new Error('当前角色无成本查看权限'),{code:'FORBIDDEN'});
      let rows=clone(db.domains[id]?.[domain]||[]);
      const eventRows=s.events.filter(e=>EamProduct.eventEquipment({...e,details:e.details||{}})===id).map((e,i)=>({id:e.id||'SESSION-'+i,name:e.text||e.command||'业务操作',eventAt:e.time,before:e.details?.before,after:e.details?.after,content:(e.actor||'')+' / '+EamProduct.time(e.time),evidence:e.details?.reason||e.details?.opinion||'来源对象 '+(e.objectId||e.command||'业务记录'),updatedAt:e.time||'',source:'业务操作',revision:1}));
      if(['history','audit'].includes(domain))rows=(pmProjection?.history||[]).concat(eventRows,rows);
      if(domain==='structure')rows=s.parts.filter(x=>x.equipmentId===id).map(x=>({id:x.id,name:x.name,content:x.code,code:x.code,parentId:x.parentId||'',evidence:'父部位 '+(x.parentId||'根'),source:'设备结构',updatedAt:'—',revision:1}));
      if(id==='EQ-001'&&domain==='failure')rows=s.reports.map(x=>({id:x.id,name:x.description,content:EamProduct.label(x.status),status:EamProduct.label(x.status),eventAt:x.observedAt||'—',downtime:'待核实',evidence:'异常报告',source:'异常与缺陷',updatedAt:x.observedAt||'—',revision:1,target:'defect',query:{report:x.id}})).concat(rows);
      if(id==='EQ-001'&&domain==='repair')rows=s.orders.map(x=>({id:x.id,name:x.scope||'维修工单',content:EamProduct.label(x.status),status:EamProduct.label(x.status),eventAt:x.createdAt||'—',executor:EamDomain.personName(x.assignee),evidence:'维修来源记录',source:'维修工单',updatedAt:x.createdAt||'—',revision:1,target:'order',query:{order:x.id}})).concat(rows);
      if(id==='EQ-001'&&domain==='work'){
        const taskRows=[s.task,...s.taskInstances].filter(x=>!x.equipmentId||x.equipmentId===id).map(x=>({id:x.id,name:'点检任务',objectType:x.status==='pending'?'任务':'记录',status:EamProduct.label(x.status),window:x.window,executor:EamDomain.personName(x.assignee),content:EamProduct.label(x.status),evidence:'标准 '+x.snapshot.revision,source:'点检任务',updatedAt:x.createdAt||'—',revision:x.revision||1,target:'inspection',query:{task:x.id,equipment:id},detailSections:[{title:'任务与责任',fields:[['状态',EamProduct.label(x.status)],['执行人',EamDomain.personName(x.assignee)],['执行窗口',x.window],['标准版本',x.snapshot.revision],['执行方法',x.snapshot.method]]}]}));
        const resultRows=s.results.filter(r=>taskRows.some(t=>t.id===r.taskId)).map(r=>({id:r.id,name:'点检完成记录',objectType:'记录',status:r.exception||'已提交',executor:EamDomain.personName(r.executedBy),eventAt:r.executedAt,content:r.items.map(i=>i.code+'：'+i.value+(i.unit||'')).join('；'),evidence:r.evidence,source:'点检结果',updatedAt:r.receivedAt,revision:r.submissionRevision,target:'inspection',query:{task:r.taskId,equipment:id},detailSections:[{title:'测量与证据',fields:[['执行人',EamDomain.personName(r.executedBy)],['完成时间',r.executedAt],['测量结果',r.items.map(i=>i.code+'：'+i.value+(i.unit||'')).join('；')],['异常说明',r.exception],['证据',r.evidence]]}]}));
        rows=taskRows.concat(resultRows,{id:'CURRENT-INS-PLAN',name:s.plan.route,objectType:'策略',status:EamProduct.label(s.plan.status),content:s.plan.frequency+' / '+s.plan.time,evidence:'计划 V'+s.plan.revision+' / 标准 '+s.standard.revision,source:'点检计划',updatedAt:'—',revision:s.plan.revision,target:'plans',query:{equipment:id}},rows.map(r=>({...r,objectType:'记录',source:'历史样本（未关联当前流程）'})));
      }
      if(domain==='completeness'){
        rows=EamArchiveRules.json.fields.map(([key,name,stage])=>{const applicable=stage==='always'||['active','stopped','pending_scrap','scrapped'].includes(equipment.lifecycle),missing=applicable&&!String(equipment[key]||'').trim();return {id:key,name,content:applicable?(missing?'待补录':'已维护'):'不适用（草稿阶段）',status:missing?'待补录':applicable?'通过':'不适用',field:key,options:key==='ownerId'?clone(EamArchiveRules.json.ownerOptions):[],currentValue:equipment[key]||'',canRepair:missing&&equipment.lifecycle!=='scrapped'&&['engineer','supervisor'].includes(s.role),evidence:stage==='always'?'全阶段基础字段':'在用及后续阶段必填',source:'完整性规则 V2',updatedAt:equipment.updatedAt||'—',revision:equipment.revision};});
        for(const row of rows){let invalid='';const value=String(equipment[row.field]||'');if(row.status==='通过'){if(row.field==='ownerId'&&!EamArchiveRules.json.ownerOptions.some(option=>option.value===value))invalid='设备负责人不在当前可选人员范围';if(row.field==='commissionedAt'&&(!/^\d{4}-\d{2}-\d{2}$/.test(value)||!Number.isFinite(Date.parse(value))||new Date(value).toISOString().slice(0,10)!==value))invalid='投用日期不是有效日历日期';if(row.field==='code'&&s.equipmentRecords.some(other=>other.id!==id&&other.code?.toLowerCase()===value.toLowerCase()))invalid='设备编码重复';}if(invalid){row.status='待修正';row.content='待修正';row.evidence=invalid;row.canRepair=equipment.lifecycle!=='scrapped'&&['engineer','supervisor'].includes(s.role);}}
        for(const parameter of db.domains[id]?.parameters||[]){let status='通过',evidence='参数类型、单位、精度及范围校验通过';try{EamArchiveRules.validate(EamArchiveRules.json.parameters[parameter.id],parameter.content);}catch(error){status='待修正';evidence=error.message;}rows.push({id:'CHECK-'+parameter.id,name:parameter.name,content:status,status,evidence,source:'参数模板校验',updatedAt:parameter.updatedAt,revision:parameter.revision,repairTab:'parameters'});}
        for(const row of rows)if(row.canRepair)row.requiredRepairs=rows.filter(other=>other.field&&other.status==='待补录'&&other.id!==row.id&&EamArchiveRules.json.fields.some(([key,,stage])=>key===other.field&&stage==='always')).map(other=>({field:other.field,name:other.name,options:other.options}));
        rows.push({id:'TEMPLATE',name:'参数模板适用关系',content:db.domains[id]?.parameters?.length?'演示模板 MIXER-V3':'待配置',status:db.domains[id]?.parameters?.length?'通过':'待配置',evidence:'当前模板仅适用于示例混合机；其他设备须独立配置适用关系',source:'模板适用检查',updatedAt:'—',revision:1,target:'equipment-templates',query:{}});
      }
      if(projection){
        if(['failure','repair','risk','related'].includes(domain)){
          const kinds={failure:['defects','failures'],repair:['work-orders'],risk:['hazards'],related:['discoveries','defects','failures','work-orders','hazards']}[domain];
          rows=projection.records.filter(row=>kinds.includes(row.kind)).concat(clone(db.domains[id]?.[domain]||[]).map(row=>({...row,source:'历史样本（未关联当前流程）'})));
        }
        if(['history','audit'].includes(domain))rows=projection.history.concat(rows);
      }
      rows=rows.map(x=>({...x,...(db.structured[x.id]||{})}));
      if(domain==='failure')rows=rows.map(row=>({...row,kind:row.kind||'failures',objectType:row.objectType||'历史故障',priorityLabel:row.priorityLabel||'未记录'}));
      if(['audit','history'].includes(domain))rows.sort((a,b)=>(Date.parse(b.eventAt||b.updatedAt)||0)-(Date.parse(a.eventAt||a.updatedAt)||0));
      if(domain==='parameters')rows.forEach(x=>{x.content=x.content.split(' / ')[0];x.definition=clone(EamArchiveRules.json.parameters[x.id]||null);x.valueType=x.definition?.type==='range'?'数值范围':x.definition?.type==='number'?'数值':'未定义';try{x.typed=EamArchiveRules.validate(x.definition,x.content);}catch(error){x.validationError=error.message;}});
      if(domain==='replacement')rows=rows.filter(x=>x.id!=='LIFE-001');
      if(scenario==='empty')rows=[];
      const ts=rows.map(r=>Date.parse(r.updatedAt)).filter(t=>!isNaN(t)).sort((a,b)=>b-a);
      const panelAsOf=ts.length?new Date(ts[0]).toLocaleString('sv-SE'):EAM_ARCHIVE_DEMO.asOf;
      const panelSource=[...new Set(rows.map(row=>row.source).filter(Boolean))].join('、')||'—';
      return {code:200,data:{items:rows,total:rows.length,columns:db.schemas[domain]||[['name','记录 / 名称'],['content','业务内容'],['evidence','关联与依据']],trend:domain==='trend'?clone(db.trendConfig[id]||null):null,lifetime:domain==='replacement'&&scenario!=='empty'?clone(db.lifetime[id]||null):null,source:panelSource,asOf:panelAsOf}};
    }
    const profile=db.profiles[id]||{};
    const states=clone(db.stateSources);const life=id==='EQ-001'?s.lifecycle:equipment.lifecycle;
    const values={lifecycle:db.stateLabels[life]||'未知',operating:id==='EQ-001'?EamDomain.operating(s):'未知',health:id==='EQ-001'?EamDomain.health(s):'未知',metrology:id==='EQ-001'?'不适用':'待确认',permission:id==='EQ-001'?(s.restrictions.some(x=>x.status==='active')?'限制使用':'可用'):'待确认'};
    if(projection){if(projection.health)values.health=projection.health;if(projection.restrictions.some(row=>row.status==='active'))values.permission='限制使用';}
    states.forEach(x=>{x.value=values[x.key];x.tone=['正常','在用','运行','可用'].includes(x.value)?'success':x.value==='不适用'?'neutral':'warning';if(id!=='EQ-001'){x.source='未接入';x.time='—';}if(projection&&['health','permission'].includes(x.key)){x.source='问题与隐患接口聚合';x.time=projection.records.map(row=>row.updatedAt).filter(value=>Number.isFinite(Date.parse(value))).sort((a,b)=>Date.parse(b)-Date.parse(a))[0]||'未记录';x.explanation='聚合来源业务状态；时间为最新来源记录时间，不代表实时信号';}if(scenario==='stale'&&x.key==='operating'){x.time='2026-10-08 08:00';x.source='信号超时';x.value='未知（信号超时）';x.tone='warning';x.explanation='采集信号已失效，不能认定为正常运行';}});
    const pending=scenario==='empty'?0:projection?projection.pending:id==='EQ-001'?s.reports.filter(x=>!['closed','withdrawn'].includes(x.status)).length:0;
    return {code:200,data:clone({equipment:{...profile,...equipment,owner:EamDomain.personName(equipment.ownerId)},states,fieldLabels:db.fieldLabels,pending,maintenanceCount:scenario==='empty'?0:pmProjection?pmProjection.taskCount:0,canRelease:['safety','metrology'].includes(s.role),canEdit:['engineer','supervisor'].includes(s.role)&&life!=='scrapped',canLifecycle:id==='EQ-001'&&['engineer','supervisor'].includes(s.role),asOf:EAM_ARCHIVE_DEMO.asOf,fileCount:scenario==='empty'?0:s.assetFiles.filter(f=>f.equipmentId===id&&f.status==='active'&&f.category==='document').length+(db.media[id]?.filter(f=>f.category==='document'&&f.status==='active').length||0),files:s.assetFiles.filter(f=>f.equipmentId===id),parts:s.parts.filter(x=>x.equipmentId===id)})};
  }
  return {request};
})();
