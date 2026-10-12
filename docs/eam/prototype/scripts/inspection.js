/* 组件就绪后执行 */
Eam.registerInit(async function(){
(()=>{
 if(!document.getElementById('temperature'))return;const s=Eam.s,params=new URLSearchParams(location.search);
 if(params.has('task')){s.activeTaskId=params.get('task');Eam.save();}
 if(params.has('check')){s.activeCheckId=params.get('check');Eam.save();}
 const t=EamDomain.currentTask(s);Eam.text('task-title',t.id+' · 1#混合机');Eam.text('task-status',({complete:'已完成',cancelled:'已取消',pending:'待执行'}[t.status]));
 Eam.text('task-meta','执行人 '+EamDomain.personName(t.assignee)+' / '+t.window+' / 标准 '+t.snapshot.revision+' / 来源 '+(t.planRevision?'计划V'+t.planRevision:'初始计划V1'));
 Eam.text('task-method',t.snapshot.method+'；温度上限 '+t.snapshot.upper+'°C。润滑与防护分别记录。');
 for(const task of [s.task,...s.taskInstances]){const b=document.createElement('button');b.textContent=task.id+' / '+task.status;b.onclick=()=>Eam.go('inspection',{task:task.id,tab:'routine'});document.getElementById('task-selector').append(b);}
 for(const key of ['temperature','lubrication','guard'])document.getElementById(key).closest('.field').hidden=!t.snapshot.items.includes(key);
 for(const [key,id]of [['temperature','temperature'],['lubrication','lubrication'],['guard','guard'],['exception','exception'],['evidence','evidence-reference']])document.getElementById(id).value=(s.drafts[t.id]||{})[key]||'';
 const result=s.results.find(r=>r.taskId===t.id);if(result){Eam.text('result-summary','原结果 '+result.id+' / '+EamDomain.personName(result.executedBy)+' / '+result.executedAt+' / 版本 '+result.submissionRevision);for(const item of result.items)document.getElementById(item.code).value=item.value;document.getElementById('exception').value=result.exception;document.getElementById('evidence-reference').value=result.evidence;document.querySelectorAll('[data-panel="routine"] input,[data-panel="routine"] select,[data-panel="routine"] textarea').forEach(el=>el.disabled=true);}
 const fields=()=>({temperature:Eam.value('temperature'),lubrication:Eam.value('lubrication'),guard:Eam.value('guard'),exception:Eam.value('exception'),evidence:Eam.value('evidence-reference')});
 document.getElementById('save-draft').disabled=t.status!=='pending';document.getElementById('submit-inspection').disabled=t.status!=='pending';
 document.getElementById('save-draft').onclick=()=>{if(Eam.run('draft',fields()))location.reload();};
 document.getElementById('submit-inspection').onclick=()=>{const button=document.getElementById('submit-inspection');button.disabled=true;if(Eam.run('inspect',fields())){const r=s.reports.find(r=>r.sourceId===s.results.at(-1).id);Eam.go(r?'abnormal-events':'history',r?{id:'LEG-'+r.id}:{source:s.events[0].id});}else button.disabled=false;};
 const c=EamDomain.currentCheck(s);Eam.text('check-info',c?c.id+' / '+({pending:'待执行',complete:'已完成',cancelled:'已取消'}[c.status])+(c.cancelReason?' / 原因 '+c.cancelReason:'')+' / '+EamDomain.personName(c.assignee)+' / '+c.method:'暂无专项检查；可由异常受理创建，或申请临时检查。');
 for(const check of s.checks){const b=document.createElement('button');b.textContent=check.id+' / '+({pending:'待执行',complete:'已完成',cancelled:'已取消'}[check.status]);b.onclick=()=>Eam.go('inspection',{check:check.id,tab:'special'});document.getElementById('check-selector').append(b);}
 if(c&&c.status!=='pending'){for(const [id,key]of [['check-conclusion','conclusion'],['check-opinion','opinion'],['check-evidence','evidence']]){document.getElementById(id).value=c[key]||'';document.getElementById(id).disabled=true;}}
 document.getElementById('submit-check').disabled=!c||c.status!=='pending';
 document.getElementById('submit-check').onclick=()=>{if(Eam.run('checkResult',{conclusion:Eam.value('check-conclusion'),opinion:Eam.value('check-opinion'),evidence:Eam.value('check-evidence')}))Eam.go(c.reportId?'defect':'history',c.reportId?{report:c.reportId}:{});};
 document.getElementById('request-check').onclick=()=>{if(Eam.run('requestCheck',{reason:Eam.value('check-reason'),method:Eam.value('check-method')}))Eam.go('inspection',{tab:'special'});};
 if(t.status!=='pending')document.querySelectorAll('[data-panel="routine"] input,[data-panel="routine"] select,[data-panel="routine"] textarea').forEach(el=>el.disabled=true);
 if(params.get('tab')==='special')document.querySelector('[data-tab="special"]').click();

 document.getElementById('discard-inspection-draft').disabled=t.status!=='pending'||!s.drafts[t.id]||t.assignee!==s.person;
 document.getElementById('cancel-task').disabled=t.status!=='pending'||!Eam.can(['engineer','supervisor']);
 document.getElementById('cancel-check').disabled=c?.status!=='pending'||!Eam.can(['engineer','supervisor']);
 for(const [id,action,payload]of [['discard-inspection-draft','discardInspectionDraft',{}],['cancel-task','cancelTask',{}],['cancel-check','cancelCheck',{id:c?.id}]])document.getElementById(id).onclick=()=>Eam.confirm(document.getElementById(id).textContent,reason=>{if(!Eam.run(action,{...payload,reason}))return false;location.reload();return true;});

 const assignedHere=t.assignee===s.person&&Eam.can(['operator','inspector']);for(const id of ['save-draft','submit-inspection'])document.getElementById(id).disabled ||= !assignedHere;document.getElementById('submit-check').disabled ||= !(Eam.can(['engineer'])||(s.person===c?.assignee&&Eam.can(['inspector'])));document.getElementById('request-check').disabled=!Eam.can(['operator','inspector','engineer','supervisor']);
})();

});
