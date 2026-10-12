/* 组件就绪后执行 */
Eam.registerInit(async function(){
(()=>{
 if(!document.getElementById('route-name'))return;const s=Eam.s,context=EamProduct.selectVersion(s.plan,s.planDraft,s.planHistory,Object.fromEntries(new URLSearchParams(location.search))),p=context.record||s.plan;
 Eam.text('plan-info','当前计划 V'+s.plan.revision+' / '+({shift:'每班',daily:'每日',weekly:'每周'}[s.plan.frequency])+' / '+s.plan.time+' / '+s.plan.route+' / '+(s.plan.status==='published'?'已发布':'已停用'));
 document.getElementById('route-name').value=p.route;document.getElementById('plan-frequency').value=p.frequency;document.getElementById('plan-time').value=p.time;
 document.getElementById('generate-date').value=s.lastGenerateWindow||'2026-10-11';
 Eam.text('plan-draft',s.planDraft?'待批准计划V'+s.planDraft.revision+' / 起草人 '+EamDomain.personName(s.planDraft.author):'暂无计划草稿');
 document.getElementById('publish-plan').disabled=!s.planDraft;
 for(const t of s.taskInstances)Eam.row(document.getElementById('generated-tasks'),t.id,t.window+' / '+({pending:'待执行',complete:'已完成',cancelled:'已取消'}[t.status]||t.status)+' / 执行人 '+EamDomain.personName(t.assignee)+' / 标准 '+t.snapshot.revision,'inspection',{task:t.id});
 document.getElementById('save-plan').onclick=()=>{if(Eam.run('savePlan',{route:Eam.value('route-name'),frequency:Eam.value('plan-frequency'),time:Eam.value('plan-time'),...binding()}))location.reload();};
 document.getElementById('publish-plan').onclick=()=>{if(Eam.run('publishPlan',{}))location.reload();};
 document.getElementById('generate-tasks').onclick=()=>{if(Eam.run('generateTasks',{window:Eam.value('generate-date'),assignee:Eam.value('task-assignee')}))location.reload();};

 function binding(){const type=Eam.value('plan-binding');return {bindingType:type,personIds:type==='position'?[]:[...document.querySelectorAll('#plan-people input:checked')].map(el=>el.value),positionIds:type==='person'?[]:[...document.querySelectorAll('#plan-positions input:checked')].map(el=>el.value),assignmentPolicy:Eam.value('plan-policy')};}
 const initial=p.bindingType?p:{bindingType:'person',personIds:[p.assignee],positionIds:[],assignmentPolicy:'pool'};for(const [container,items,selected]of [['plan-people',EamDomain.people.filter(x=>x.roles.some(r=>['operator','inspector'].includes(r))),initial.personIds],['plan-positions',EamDomain.positions,initial.positionIds]])for(const item of items){const label=document.createElement('label');label.className='choice';const input=document.createElement('input');input.type='checkbox';input.value=item.id;input.checked=selected.includes(item.id);input.id=container+'-'+item.id;label.append(input,document.createTextNode(item.name+' / '+item.id));document.getElementById(container).append(label);}
 document.getElementById('plan-binding').value=initial.bindingType;document.getElementById('plan-policy').value=initial.assignmentPolicy;
 function candidates(){const b=binding();document.getElementById('plan-people-box').hidden=b.bindingType==='position';document.getElementById('plan-positions-box').hidden=b.bindingType==='person';try{Eam.text('plan-candidates','当前候选人员（去重）：'+EamDomain.planCandidates(b).map(x=>x.name).join('、'));}catch(e){Eam.text('plan-candidates',e.message);}}
 for(const id of ['plan-binding','plan-policy','plan-people','plan-positions'])document.getElementById(id).addEventListener('change',candidates);candidates();
 const published=s.plan.bindingType?s.plan:{bindingType:'person',personIds:[s.plan.assignee],positionIds:[],assignmentPolicy:'pool'};try{for(const person of EamDomain.planCandidates(published))document.getElementById('task-assignee').add(new Option(person.name+' / '+person.id,person.id));}catch(e){Eam.text('generation-rule',e.message);}
 document.getElementById('task-assignee').disabled=published.assignmentPolicy==='per_person';Eam.text('generation-rule',published.assignmentPolicy==='per_person'?'依据已发布计划为每位候选人分别生成个人任务':'依据已发布计划从候选池指定一位主责任人；草稿配置不会影响任务');
 const author=s.planDraft?.author, isDraftOwner=author===s.person;
 document.getElementById('copy-plan').disabled=!!s.planDraft||!Eam.can(['engineer','supervisor']);
 document.getElementById('discard-plan').disabled=!isDraftOwner;
 document.getElementById('disable-plan').disabled=s.plan.status!=='published'||!Eam.can(['engineer','supervisor','quality']);
 document.getElementById('copy-plan').onclick=()=>{if(Eam.run('copyPlan'))location.reload();};
 for(const action of ['discard','disable'])document.getElementById(action+'-plan').onclick=()=>Eam.confirm(action==='discard'?'删除本人草稿':'停用当前发布版本',reason=>{if(!Eam.run(action+'Plan',{reason}))return false;location.reload();return true;});
 for(const version of [...s.planHistory,s.plan])Eam.row(document.getElementById('plan-history'),'版本 '+version.revision,(version.status==='published'?'已发布':version.status==='disabled'?'已停用':'历史发布')+' / 起草 '+EamDomain.personName(version.author)+' / 审核 '+EamDomain.personName(version.approvedBy)+(version.disableReason?' / 停用原因 '+version.disableReason:'')+' / '+(version.route+' / '+version.frequency+' / '+version.time));
 document.getElementById('save-plan').disabled=!Eam.can(['engineer','supervisor'])||!!(author&&!isDraftOwner);
 document.getElementById('publish-plan').disabled=!s.planDraft||isDraftOwner||!Eam.can(['engineer','supervisor']);

 document.getElementById('generate-tasks').disabled=!Eam.can(['engineer','supervisor']);

 const params=Object.fromEntries(new URLSearchParams(location.search));const selected=EamProduct.selectVersion(s.plan,s.planDraft,s.planHistory,params);
 if(selected.readonly){Eam.text('plan-info',selected.record?'版本 '+selected.record.revision+' / '+EamProduct.label(selected.record.status)+' / 只读':'未找到此版本，请返回列表');Eam.text('plan-draft','当前查看发布记录；草稿编辑请从列表草稿入口进入');
 if(document.getElementById('route-name'))document.getElementById('route-name').value=selected.record?.route??'';
 if(document.getElementById('plan-frequency'))document.getElementById('plan-frequency').value=selected.record?.frequency??'';
 if(document.getElementById('plan-time'))document.getElementById('plan-time').value=selected.record?.time??'';
 if(document.getElementById('plan-person'))document.getElementById('plan-person').value=selected.record?.assignee??'';
 document.querySelectorAll('main input,main select,main textarea,main button:not([data-page])').forEach(el=>el.disabled=true);}
})();

});
