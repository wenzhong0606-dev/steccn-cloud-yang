/* 组件就绪后执行 */
Eam.registerInit(async function(){
(()=>{if(!document.getElementById('list-rows'))return;const s=Eam.s;EamList([{status:s.plan.status,person:s.plan.author,values:['ROUTE-DEMO-001',s.plan.route,'原料二车间 → 配料线 → EQ-001','计划 V'+s.plan.revision,EamProduct.label(s.plan.status)],page:'plans'}],{primary:'维护路线配置',page:'plans',allowed:Eam.can(['engineer','supervisor'])});})();

});
