/* 页面显示及文件选择规则；不生成业务页面。 */
(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.EamProduct=api;})(typeof window==='undefined'?globalThis:window,function(){
 const labels={pending:'待执行',complete:'已完成',cancelled:'已取消',open:'待受理',checking:'检查中',processing:'维修中',professional:'专业处置中',closed:'已关闭',withdrawn:'已撤回',assigned:'待开工',active:'执行中',paused:'已暂停',awaiting:'待验收',returned:'待返工',draft:'草稿',published:'已发布',disabled:'已停用',archived:'历史版本',removed:'已移除',normal:'正常',abnormal:'异常',medium:'一般',high:'重要',urgent:'紧急风险',shift:'每班',daily:'每日',weekly:'每周',image:'设备图片',document:'技术文档',model:'3D模型'};
 const label=x=>labels[x]||x||'未维护';
 const time=x=>{if(!x)return '未记录';const d=new Date(x);return Number.isNaN(d.getTime())?x:d.toLocaleString('zh-CN',{timeZone:'Asia/Shanghai',hour12:false});};
 const fileLimits={image:10,document:25,model:100};
 function fileError(p){if(!p.name||/[\\/]/.test(p.name))return '文件名不可包含目录路径';const ext=p.name.split('.').at(-1).toLowerCase(),allowed={image:['png','jpg','jpeg','webp'],document:['pdf','txt','doc','docx','xls','xlsx'],model:['glb','gltf']};if(!allowed[p.category]?.includes(ext))return '文件格式与分类不匹配';if(!Number.isFinite(Number(p.size))||Number(p.size)<=0||Number(p.size)>fileLimits[p.category]*1024*1024)return '文件大小须大于0且不超过'+fileLimits[p.category]+'MB';if(p.category==='image'&&p.type&&!p.type.startsWith('image/'))return '图片文件类型不匹配';return '';}
 const storageTarget=(equipment,category,id,name)=>'eam/equipment/'+equipment+'/'+category+'/'+id+'.'+name.split('.').at(-1).toLowerCase();
 function selectVersion(current,draft,history,params){if(params.revision!==undefined&&params.revision!==null){return {record:[current,...history].find(v=>String(v.revision)===String(params.revision))||null,readonly:true};}return {record:draft||current,readonly:false};}
 const filePermission=(role,action,file)=>file.status==='active'&&(action==='view'||(action==='download'?['engineer','supervisor','quality','safety','metrology','auditor','manager'].includes(role):['engineer','supervisor'].includes(role)));
 const eventEquipment=e=>e.objectType==='equipment'?e.objectId:(e.details.equipmentId||e.details.record?.equipmentId||e.details.before?.equipmentId||e.details.after?.equipmentId||'EQ-001');
 return {label,time,fileLimits,fileError,storageTarget,selectVersion,filePermission,eventEquipment};
});
