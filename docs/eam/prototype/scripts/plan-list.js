/* 组件就绪后执行 */
Eam.registerInit(async function(){
(()=>{if(!document.getElementById('list-rows'))return;const s=Eam.s;EamList([...s.planHistory.map(v=>({...v,status:'archived'})),s.plan,...(s.planDraft?[s.planDraft]:[])].map(v=>({status:v.status,person:v.assignee,values:['巡检计划 / V'+v.revision,v.route,EamProduct.label(v.frequency)+' / '+v.time,v.bindingType?((v.positionIds||[]).map(id=>EamDomain.positions.find(p=>p.id===id)?.name||id).concat((v.personIds||[]).map(EamDomain.personName)).join('、')+' / '+(v.assignmentPolicy==='per_person'?'逐人执行':'候选池派工')):EamDomain.personName(v.assignee),EamProduct.label(v.status)],page:'plans',query:v===s.planDraft?{mode:'draft'}:{revision:v.revision}})),{primary:'起草新计划版本',page:'plans',query:{mode:'draft'},mine:true,allowed:Eam.can(['engineer','supervisor'])});})();

});
