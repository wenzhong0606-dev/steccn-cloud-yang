/* 组件就绪后执行 */
Eam.registerInit(async function(){
(()=>{if(!document.getElementById('list-rows'))return;const s=Eam.s;EamList([...s.standardHistory.map(v=>({...v,status:'archived'})),s.standard,...(s.standardDraft?[s.standardDraft]:[])].map(v=>({status:v.status,person:v.author,values:['混合机标准 / '+v.revision,'EQ-001',v.method,EamDomain.personName(v.author),EamProduct.label(v.status)],page:'standards',query:v===s.standardDraft?{mode:'draft'}:{revision:v.revision}})),{primary:'起草新标准版本',page:'standards',query:{mode:'draft'},mine:true,allowed:Eam.can(['engineer'])});})();

});
