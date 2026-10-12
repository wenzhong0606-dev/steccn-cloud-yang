/* 组件就绪后执行 */
Eam.registerInit(async function(){
/* 跨页常用控件；各业务页逻辑位于../scripts/同名.js，禁止拼接整页。 */
(()=>{
  Eam.attach();
  const label={active:'在用',stopped:'停用',pending_scrap:'待报废',scrapped:'已报废',pending:'待验收',draft:'草稿'};
  Eam.text('lifecycle-state',label[Eam.s.lifecycle]);
  Eam.text('operating-state',EamDomain.operating(Eam.s));
  Eam.text('health-state',EamDomain.health(Eam.s));
  Eam.text('permission-state',Eam.s.restrictions.some(r=>r.status==='active')?'限制使用':'可用（模拟）');
  Eam.text('metrology-state','不适用（演示设备）');
  Eam.text('actor-name',EamDomain.personName(Eam.s.person));
  Eam.text('object-version',Eam.s.version);
})();

});
