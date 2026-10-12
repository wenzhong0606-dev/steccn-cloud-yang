/* 模拟领域规则：无DOM、无后端请求，页面内容始终由独立HTML负责。 */
(function(root, factory) {
  const api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  else root.EamDomain = api;
})(typeof window !== 'undefined' ? window : globalThis, function() {
  const maintenance = typeof module==='object'&&module.exports ? require('./maintenance.js') : window.EamMaintenance;
  const people = [
    {id:'P01',name:'陈晓',roles:['operator']}, {id:'P02',name:'李敏',roles:['inspector']},
    {id:'P03',name:'王强',roles:['technician','supervisor']}, {id:'P04',name:'周平',roles:['supervisor']},
    {id:'P05',name:'赵工',roles:['engineer']}, {id:'P06',name:'孙工',roles:['metrology']},
    {id:'P07',name:'刘工',roles:['quality']}, {id:'P08',name:'吴工',roles:['safety']},
    {id:'P09',name:'许工',roles:['spare']}, {id:'P10',name:'郑经理',roles:['manager']},
    {id:'P11',name:'杨工',roles:['auditor']}, {id:'P12',name:'外包张工',roles:['contractor']},
    {id:'P13',name:'管理员',roles:['admin']}, {id:'P14',name:'林悦',roles:['inspector']}
  ];
  const positions=[{id:'JOB-I',name:'设备点检岗',members:['P02','P14']},{id:'JOB-O',name:'运行巡检岗',members:['P01']},{id:'JOB-EMPTY',name:'空缺备用岗',members:[]}];
  function planCandidates(p){const ids=new Set(p.personIds||[]);for(const id of p.positionIds||[]){const job=positions.find(x=>x.id===id);need(job,'岗位不存在');need(job.members.length,'岗位暂无在岗人员');job.members.forEach(x=>ids.add(x));}need(ids.size,'至少绑定一个人员或岗位');return [...ids].map(id=>{const person=people.find(x=>x.id===id);need(person&&person.roles.some(r=>['operator','inspector'].includes(r)),'候选人员缺少点检资格');return person;});}
  const clone = v => JSON.parse(JSON.stringify(v));
  const need = (condition, text) => {if (!condition) throw new Error(text);};
  const personName = id => people.find(p=>p.id===id)?.name || id || '尚未绑定';
  function initial() {
    const snapshot = {revision:'V1.2',method:'测温枪测轴承外壳；目视检查润滑与防护',upper:80,unit:'°C',items:['temperature','lubrication','guard']};
    return {...maintenance.initial(),schema:3,role:'inspector',person:'P02',scenario:'normal',version:1,lifecycle:'active',
      operating:'运行',restrictions:[],reports:[],orders:[],executions:[],acceptances:[],results:[],events:[],attachments:[],
      task:{id:'INS-20261010-001',status:'pending',type:'routine',assignee:'P02',window:'2026-10-10 08:00—20:00',snapshot:clone(snapshot)},
      draft:{temperature:'',lubrication:'',guard:'',exception:'',evidence:''},drafts:{},check:null,checks:[],activeCheckId:null,
      standard:{revision:'V1.2',upper:80,method:snapshot.method,status:'published',author:'P05',approvedBy:'P04'},standardDraft:null,
      plan:{revision:1,frequency:'shift',time:'08:00',route:'原料巡检线',assignee:'P02',status:'published',author:'P05',approvedBy:'P04'},planDraft:null,
      taskInstances:[],reportId:null,orderId:null,activeTaskId:null};
  }
  const currentReport = s => s.reports.find(r=>r.id===s.reportId) || s.reports.at(-1);
  const currentOrder = s => s.orders.find(o=>o.id===s.orderId) || s.orders.at(-1);
  const currentTask = s => s.taskInstances.find(t=>t.id===s.activeTaskId) || s.task;
  const currentCheck = s => s.checks.find(c=>c.id===s.activeCheckId) || s.checks.at(-1) || s.check;
  const checkForReport = (s,id) => s.checks.filter(c=>c.reportId===id&&c.status==='pending').at(-1)||s.checks.filter(c=>c.reportId===id).at(-1);
  const operating = s => s.scenario==='stale'?'未知':s.operating;
  const health = s => s.reports.some(r=>!['closed','withdrawn'].includes(r.status)&&r.confirmed)?'故障':s.reports.some(r=>!['closed','withdrawn'].includes(r.status))?'待确认':'正常';
  function command(state, actor, action, payload={}, now=new Date().toISOString()) {
    const s=clone(state), p=payload;
    need(!['forbidden','empty'].includes(s.scenario),'当前没有业务数据权限');
    need(s.scenario!=='failure','模拟服务失败：未保存，请恢复正常场景后重试');
    need(s.scenario!=='conflict','对象版本已变化：未保存，请恢复正常场景并重新读取');
    need(people.some(u=>u.id===actor.person&&u.roles.includes(actor.role)),'当前人员没有此角色权限');
    const permit = roles => need(roles.includes(actor.role),'当前角色无此操作权限');
    const event = (text,objectType,objectId,details={}) => s.events.unshift({id:'EV-'+(s.events.length+1),text,objectType,objectId,version:s.version+1,actorId:actor.person,actor:personName(actor.person),role:actor.role,time:now,receivedAt:now,details});
    const requireEquipment = () => need(s.lifecycle==='active','设备未在用，禁止创建或执行常规作业；联系主管处理');
    const repairable = () => need(['active','stopped'].includes(s.lifecycle),'当前生命周期不允许普通维修，请主管处理');
    const report = currentReport(s), order=currentOrder(s), task=currentTask(s);
    const newReport = (desc,source,sourceId,extra={}) => {
      const r={id:'AR-'+String(s.reports.length+1).padStart(3,'0'),equipmentId:'EQ-001',description:desc,source,sourceId,
        severity:p.severity||'medium',part:p.part||'轴承',partId:s.parts.find(x=>x.equipmentId==='EQ-001'&&x.name===(p.part||'轴承'))?.id||null,observedAt:p.observedAt||now,reporterId:actor.person,receivedAt:now,
        intent:p.intent||'check',status:'open',confirmed:false,evidence:p.evidence||'',...extra};
      s.reports.push(r);s.reportId=r.id;
      if(r.severity==='urgent')s.restrictions.push({id:'URG-'+r.id,reportId:r.id,scope:'设备使用',reason:'紧急风险，现场处置与安全验证待完成',status:'active'});
      event('上报异常：'+desc,'report',r.id,r);return r;
    };
    const closeReport = (r,reason) => {r.status='closed';r.closedBy=actor.person;r.closedAt=now;r.closureReason=reason;};
    const assigned = () => {permit(['technician','contractor']);need(order&&order.assignee===actor.person,'非指派执行人员；联系班组长重新派工');};
    const safety = () => {need(p.isolation&&p.guard,'请逐项确认隔离和防护安全措施');need(s.scenario!=='qualification','作业资格已失效，请班组长安排合格人员');};
    const activeExecution = () => s.executions.find(e=>e.id===order?.executionId);
    if (maintenance.apply(s,actor,action,p,{need,permit,event,now,task,order})) {
      // Maintenance command handled transactionally above.
    } else if (action==='draft'||action==='inspect') {
      permit(['operator','inspector']);requireEquipment();need(task.status==='pending','该任务已提交，不得重复覆盖');
      need(task.assignee===actor.person,'任务未指派给当前人员');
      if(action==='draft'){s.draft=clone(p);s.drafts[task.id]=clone(p);event('保存点检草稿','task',task.id,{record:clone(p)});}
      else {
        const items=task.snapshot.items;
        if(items.includes('temperature'))need(p.temperature!==undefined&&String(p.temperature).trim()!==''&&Number.isFinite(Number(p.temperature))&&Number(p.temperature)>=-50&&Number(p.temperature)<=300,'请填写有效温度（-50至300°C）');
        for(const key of items.filter(key=>key!=='temperature'))need(['normal','abnormal'].includes(p[key]),'本次必检项未填，请逐项记录');
        const abnormal=items.some(key=>key==='temperature'?Number(p.temperature)>task.snapshot.upper:p[key]==='abnormal');
        if(abnormal)need(p.exception?.trim()&&p.evidence?.trim(),'异常结果必须填写说明及证据引用');
        const result={id:'RES-'+(s.results.length+1),taskId:task.id,submissionRevision:1,snapshot:clone(task.snapshot),executedBy:actor.person,executedAt:now,receivedAt:now,
          items:items.map(key=>key==='temperature'?{code:key,value:Number(p.temperature),unit:'°C',conclusion:Number(p.temperature)>task.snapshot.upper?'abnormal':'normal'}:{code:key,value:p[key]}),exception:p.exception||'',evidence:p.evidence||''};
        s.results.push(result);task.status='complete';s.draft={};delete s.drafts[task.id];event('提交逐项点检结果','result',result.id,result);
        if(abnormal)newReport(p.exception,'inspection',result.id);
      }
    } else if(action==='report') {
      permit(['operator','inspector','technician','supervisor','engineer','safety','quality']);
      need(s.lifecycle!=='scrapped','已报废设备异常请由主管登记处置');
      need(p.description?.trim()&&p.observedAt&&p.severity,'请填写异常描述、发现时间与等级');
      newReport(p.description,'manual',null);
    } else if(action==='triageCheck') {
      permit(['supervisor','engineer']);need(report&&report.status==='open','没有待受理异常');
      need(!s.checks.some(c=>c.status==='pending'),'已有未完成检查，请主管先处置');
      report.status='checking';report.triageBy=actor.person;report.triageAt=now;
      s.check={id:'CHK-'+report.id+(s.checks.some(c=>c.reportId===report.id)?'-R'+(s.checks.filter(c=>c.reportId===report.id).length+1):''),reportId:report.id,status:'pending',assignee:p.assignee||'P02',method:p.method||'现场复测轴承温度、检查润滑与防护',snapshot:clone(s.standard)};
      s.checks.push(s.check);s.activeCheckId=s.check.id;
      event('受理并创建专项检查','check',s.check.id,s.check);
    } else if(action==='requestCheck') {
      permit(['operator','inspector','supervisor','engineer']);requireEquipment();
      need(!s.checks.some(c=>c.status==='pending'),'已有专项检查待处理，请先完成或由主管处置');
      need(p.reason?.trim()&&p.method?.trim(),'临时检查申请需原因、方法和判断要求');
      s.check={id:'CHK-TEMP-'+(s.events.length+1),reportId:null,status:'pending',assignee:'P02',method:p.method,reason:p.reason,snapshot:clone(s.standard)};
      s.checks.push(s.check);s.activeCheckId=s.check.id;
      event('创建临时检查申请（模拟已受理）','check',s.check.id,s.check);
    } else if(action==='checkResult') {
      const check=currentCheck(s);
      permit(['inspector','engineer']);need(check?.status==='pending','没有待执行专项检查');need(check.assignee===actor.person||actor.role==='engineer','非当前检查人员');
      need(['normal','defect','professional'].includes(p.conclusion)&&p.opinion?.trim()&&p.evidence?.trim(),'请填写检查结论、说明与证据');
      Object.assign(check,{status:'complete',conclusion:p.conclusion,opinion:p.opinion,evidence:p.evidence,executedBy:actor.person,executedAt:now});
      if(s.check?.id===check.id)s.check=check;
      let r=s.reports.find(r=>r.id===check.reportId);
      if(!r&&p.conclusion!=='normal')r=newReport(p.opinion,'check',check.id);
      if(r){if(p.conclusion==='normal')closeReport(r,p.opinion);else {r.status=p.conclusion==='defect'?'open':'professional';r.confirmed=p.conclusion==='defect';r.triageOpinion=p.opinion;}}
      if(r&&p.conclusion==='professional'&&!s.restrictions.some(x=>x.reportId===r.id&&x.status==='active'))s.restrictions.push({id:'REST-'+r.id,reportId:r.id,scope:'疑似异常相关用途',reason:p.opinion,status:'active'});
      event('专项检查结论：'+p.opinion,'check',check.id,check);
    } else if(action==='professional') {
      permit(['supervisor','engineer']);need(report&&report.status==='open','无待分流异常');need(p.opinion?.trim(),'专业分流需填写原因');
      report.status='professional';report.professionalReason=p.opinion;
      s.restrictions.push({id:'REST-'+report.id,reportId:report.id,scope:'疑似异常相关用途',reason:p.opinion,status:'active'});
      event('转专业评估，限制关联用途','report',report.id,{opinion:p.opinion});
    } else if(action==='professionalClose') {
      permit(['metrology','quality','safety']);need(report?.status==='professional','没有待处理专业异常');need(p.opinion?.trim()&&p.evidence?.trim(),'专业处置需结论与证据');
      closeReport(report,p.opinion);event('专业处置已记录，使用限制仍需独立放行','report',report.id,p);
    } else if(action==='dispatch') {
      permit(['supervisor','engineer']);repairable();need(report&&report.status==='open','该异常尚在检查/处置或已关闭');
      need(!s.orders.some(o=>o.reportId===report.id&&!['closed','cancelled'].includes(o.status)),'已有未关闭工单，禁止重复派工');
      need(['P03','P12'].includes(p.assignee)&&p.scope?.trim(),'请选择维修人员并填写工作范围');
      const o={id:'WO-'+String(s.orders.length+1).padStart(3,'0'),equipmentId:'EQ-001',reportId:report.id,assignee:p.assignee,scope:p.scope,priority:p.priority||'medium',status:'assigned',assignedBy:actor.person,assignedAt:now,executionId:null};
      report.status='processing';report.confirmed=true;report.triageBy=actor.person;s.orders.push(o);s.orderId=o.id;
      if(report.severity==='urgent'&&!s.restrictions.some(r=>r.reportId===report.id))s.restrictions.push({id:'URG-'+report.id,reportId:report.id,scope:'设备使用',reason:'紧急风险，现场安全处置待验证',status:'active'});
      event('确认缺陷并派工 '+o.id,'order',o.id,o);
    } else if(action==='start') {
      assigned();repairable();need(['assigned','returned'].includes(order.status),'工单不是待开工/返工状态');safety();
      const e={id:order.id+'-EX-'+(s.executions.filter(x=>x.orderId===order.id).length+1),orderId:order.id,revision:s.executions.filter(x=>x.orderId===order.id).length+1,executorId:actor.person,startedAt:now,safety:{isolation:true,guard:true,confirmedBy:actor.person,at:now},qualificationSnapshot:'演示资格有效',status:'active'};
      s.executions.push(e);order.executionId=e.id;order.status='active';event('安全逐项确认，开工','execution',e.id,e);
    } else if(action==='finish') {
      assigned();need(order.status==='active','工单不是执行中');need(p.repair?.trim()&&p.trial?.trim()&&p.diagnosis?.trim(),'诊断、维修措施及试运行结果分别必填');need(Number.isFinite(Number(p.labor))&&Number(p.labor)>=0,'工时须为非负秒数');
      const e=activeExecution();Object.assign(e,{diagnosis:p.diagnosis,repair:p.repair,trial:p.trial,labor:Number(p.labor),materialReference:p.materialReference||'',endedAt:now,status:'submitted'});order.status='awaiting';event('完工提交，冻结执行版本 '+e.revision,'execution',e.id,e);
    } else if(action==='pause'||action==='resume') {
      assigned();need(order.status===(action==='pause'?'active':'paused'),'当前状态不能暂停/恢复');
      if(action==='pause'){need(p.reason?.trim(),'暂停原因必填');order.pauseReason=p.reason;order.status='paused';}
      else {safety();order.status='active';}
      event(action==='pause'?'暂停：'+p.reason:'确认恢复条件，继续执行','order',order.id,p);
    } else if(action==='cancel') {
      permit(['supervisor','engineer']);need(order&&!['closed','cancelled'].includes(order.status),'无可取消工单');need(p.reason?.trim(),'取消原因必填');
      if(order.executionId)need(p.restored,'已开工工单取消须确认现场恢复');
      order.status='cancelled';order.cancelReason=p.reason;const linked=s.reports.find(r=>r.id===order.reportId);linked.status='open';
      if(activeExecution())activeExecution().status='cancelled';event('取消工单，异常退回主管待处置','order',order.id,p);
    } else if(action==='accept'||action==='reject') {
      permit(['supervisor','engineer']);need(order?.status==='awaiting','没有待验收工单');const e=activeExecution();need(e.executorId!==actor.person,'不能验收本人执行的工单，即使切换角色');
      if(action==='reject')need(p.opinion?.trim(),'验收退回原因必填');
      else {need(p.restored&&p.trialConfirmed,'请分别确认现场恢复与试运行');need(p.defectVerified,'必须验证关联缺陷处置');need(p.opinion?.trim(),'验收通过意见必填');}
      const a={id:'ACC-'+(s.acceptances.length+1),orderId:order.id,executionId:e.id,executionRevision:e.revision,reviewerId:actor.person,decision:action,opinion:p.opinion,restored:!!p.restored,trialConfirmed:!!p.trialConfirmed,defectVerified:!!p.defectVerified,reviewedAt:now};
      // 历史演示命令也必须遵守独立状态边界：技术验收不等于工单业务关闭，更不能替代异常/缺陷独立验证。
      s.acceptances.push(a);order.status=action==='reject'?'returned':'accepted';e.status=action==='reject'?'returned':'accepted';
      event(action==='reject'?'验收退回：'+p.opinion:'技术验收通过，工单待业务关闭；关联问题仍需独立验证','acceptance',a.id,a);
    } else if(action==='closeOrder') {
      // 旧单问题模型无法表达新模型的逐问题残留承接；仅允许已独立关闭来源异常的历史工单关闭。
      permit(['supervisor','engineer']);need(order?.status==='accepted','仅已技术验收的工单可业务关闭');
      const linked=s.reports.find(r=>r.id===order.reportId);
      need(linked?.status==='closed','关联异常未独立关闭；请通过新维护业务流程逐问题验证并完成业务关闭');
      need(p.reason?.trim(),'请填写业务关闭依据');
      order.status='closed';order.closedBy=actor.person;order.closedAt=now;order.closeReason=p.reason;
      event('历史工单业务关闭（来源异常已独立关闭）','order',order.id,{reason:p.reason});
    } else if(action==='releaseRestriction') {
      permit(['safety','metrology']);need(p.opinion?.trim(),'独立放行需证据/理由');const r=s.restrictions.find(r=>r.id===p.id&&r.status==='active');need(r,'无有效限制');need(!r.reportId||s.reports.find(x=>x.id===r.reportId)?.status==='closed','来源异常尚未处置闭环，不能放行');r.status='released';r.releasedBy=actor.person;r.releaseReason=p.opinion;event('专业使用限制解除','restriction',r.id,r);
    } else if(action==='saveStandard') {
      permit(['engineer']);need(!s.standardDraft||s.standardDraft.author===actor.person,'已有他人标准草稿，不能覆盖');need(Number.isFinite(Number(p.upper))&&Number(p.upper)>0&&Number(p.upper)<=300&&p.method?.trim(),'标准上限与方法不合法');
      s.standardDraft={revision:'V'+(parseFloat(s.standard.revision.slice(1))+0.1).toFixed(1),upper:Number(p.upper),method:p.method,status:'draft',author:actor.person};event('起草点检标准新版本','standard',s.standardDraft.revision,s.standardDraft);
    } else if(action==='publishStandard') {
      permit(['supervisor','quality']);need(s.standardDraft,'没有待发布标准');need(s.standardDraft.author!==actor.person,'不能审核本人起草标准');s.standardHistory.push(clone(s.standard));s.standard={...s.standardDraft,status:'published',approvedBy:actor.person,approvedAt:now};s.standardDraft=null;event('审核发布标准；旧任务快照保持不变','standard',s.standard.revision,s.standard);
    } else if(action==='savePlan') {
      permit(['engineer','supervisor']);need(!s.planDraft||s.planDraft.author===actor.person,'已有他人计划草稿，不能覆盖');need(['shift','daily','weekly'].includes(p.frequency)&&/^(?:[01]\d|2[0-3]):[0-5]\d$/.test(p.time)&&p.route?.trim(),'请填写路线、执行窗口和频次');
      const assignment=p.bindingType?{bindingType:p.bindingType,personIds:[...new Set(p.personIds||[])],positionIds:[...new Set(p.positionIds||[])],assignmentPolicy:p.assignmentPolicy}:{bindingType:'person',personIds:[p.assignee],positionIds:[],assignmentPolicy:'pool'};
      need(['person','position','mixed'].includes(assignment.bindingType)&&['pool','per_person'].includes(assignment.assignmentPolicy),'计划绑定或分配规则不合法');need(assignment.bindingType!=='person'||!assignment.positionIds.length,'人员模式不能绑定岗位');need(assignment.bindingType!=='position'||!assignment.personIds.length,'岗位模式不能绑定人员');const candidates=planCandidates(assignment);
      s.planDraft={revision:s.plan.revision+1,frequency:p.frequency,time:p.time,route:p.route,assignee:candidates[0].id,...assignment,status:'draft',author:actor.person};event('起草点检计划版本','plan',s.planDraft.revision,s.planDraft);
    } else if(action==='publishPlan') {
      permit(['supervisor','engineer']);need(s.planDraft,'没有计划草稿');need(s.planDraft.author!==actor.person,'不能批准本人起草计划');s.planHistory.push(clone(s.plan));s.plan={...s.planDraft,status:'published',approvedBy:actor.person};s.planDraft=null;event('审核启用计划','plan',s.plan.revision,s.plan);
    } else if(action==='reassignTask') {
      permit(['supervisor','engineer']);const target=[s.task,...s.taskInstances].find(t=>t.id===p.id);need(target?.status==='pending','仅待执行任务可改派');need((target.revision||1)===Number(p.revision),'任务版本冲突');need(p.reason?.trim(),'改派原因必填');const person=people.find(x=>x.id===p.assignee);need(person&&person.roles.some(r=>['operator','inspector'].includes(r)),'执行人员缺少资格');const pool=target.assignmentSnapshot?.resolvedPeople||[target.assignee];need(pool.includes(p.assignee),'执行人不在任务候选范围；请主管调整计划或人员资格');need(target.assignee!==p.assignee,'执行人没有变化');need(!s.taskInstances.some(t=>t.id!==target.id&&t.status==='pending'&&t.window===target.window&&t.itemGroup===target.itemGroup&&t.assignee===p.assignee),'此人员已有同窗口项目组任务');const previousAssignee=target.assignee,removedDraft=s.drafts[target.id]||null;delete s.drafts[target.id];target.assignee=p.assignee;target.revision=(target.revision||1)+1;target.reassignedBy=actor.person;target.reassignedAt=now;event('改派待执行点检任务','task',target.id,{previousAssignee,assignee:p.assignee,reason:p.reason,taskId:target.id,removedDraft});
    } else if(action==='generateTasks') {
      permit(['supervisor','engineer']);requireEquipment();need(s.plan.status==='published'&&s.standard.status==='published','计划与标准必须已发布');need(/^\d{4}-\d{2}-\d{2}$/.test(p.window),'请选择生成业务日期');
      const date=new Date(p.window+'T00:00:00Z');need(!Number.isNaN(date.getTime())&&date.toISOString().slice(0,10)===p.window,'无效业务日期');
      s.lastGenerateWindow=p.window;
      const groups=[];
      if(s.plan.frequency!=='weekly'||date.getUTCDay()===1){for(const time of s.plan.frequency==='shift'?[s.plan.time,'20:00']:[s.plan.time])groups.push({code:'routine',frequency:s.plan.frequency,time,items:['temperature','lubrication']});}
      if(date.getUTCDay()===1)groups.push({code:'guard-weekly',frequency:'weekly',time:s.plan.time,items:['guard']});
      const assignment=s.plan.bindingType?s.plan:{bindingType:'person',personIds:[s.plan.assignee],positionIds:[],assignmentPolicy:'pool'};const candidates=planCandidates(assignment);let executors=candidates;
      if(assignment.assignmentPolicy==='pool'){const chosen=p.assignee||(candidates.length===1?candidates[0].id:null);need(chosen&&candidates.some(c=>c.id===chosen),'请从候选池指定具体执行人');executors=candidates.filter(c=>c.id===chosen);}
      for(const group of groups)for(const executor of executors){const key=s.plan.revision+':EQ-001:'+p.window+':'+group.code+':'+group.time+(assignment.assignmentPolicy==='per_person'?':'+executor.id:'');const existing=s.taskInstances.find(t=>t.key===key);need(!existing||existing.assignee===executor.id,'该窗口已有个人责任任务，请通过任务改派处理，不能换人重生成');
        if(!s.taskInstances.some(t=>t.key===key)){const t={id:'INS-'+(s.taskInstances.length+2),key,revision:1,status:'pending',type:'routine',assignee:executor.id,assignmentSnapshot:clone({bindingType:assignment.bindingType,personIds:assignment.personIds,positionIds:assignment.positionIds,assignmentPolicy:assignment.assignmentPolicy,resolvedPeople:candidates.map(c=>c.id)}),window:p.window+' '+group.time,route:s.plan.route,frequency:group.frequency,itemGroup:group.code,snapshot:{...clone(s.standard),unit:'°C',items:group.items},planRevision:s.plan.revision};s.taskInstances.push(t);event('按到期项目组生成任务（去重）','task',t.id,t);}}
    } else if(action==='lifecycle') {
      maintenance.apply(s,actor,'equipmentLifecycle',{...p,id:'EQ-001'},{need,permit,event,now,task,order});
    } else throw new Error('未知模拟操作');
    if(action==='report')delete s.reportDrafts[actor.person];
    s.version+=1;Object.keys(state).forEach(k=>delete state[k]);Object.assign(state,s);return state;
  }
  return {initial,command,positions,planCandidates,people,personName,currentReport,currentOrder,currentTask,currentCheck,checkForReport,operating,health};
});
