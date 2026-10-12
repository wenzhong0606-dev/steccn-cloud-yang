/* 设备媒体目录和本机模型检查；不修改资源或业务状态。 */
(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.EamArchiveMediaRules=api;})(typeof window==='undefined'?globalThis:window,function(){
 function catalog(items,equipment){
  const scoped=items.filter(f=>f.equipmentId===equipment&&f.status==='active');
  const images=scoped.filter(f=>f.category==='image'&&!f.isSample).sort((a,b)=>Number(!!b.isPrimary)-Number(!!a.isPrimary));
  return {images,samples:scoped.filter(f=>f.category==='image'&&f.isSample),models:scoped.filter(f=>f.category==='model'),documents:scoped.filter(f=>f.category==='document'),primary:images.find(f=>f.isPrimary)||null};
 }
 async function inspect(blob,name){
  const ext=name.split('.').at(-1).toLowerCase();let json,format;
  if(ext==='glb'){
   const bytes=await blob.arrayBuffer(),view=new DataView(bytes);
   if(bytes.byteLength<20||view.getUint32(0,true)!==0x46546c67||view.getUint32(4,true)!==2||view.getUint32(8,true)!==bytes.byteLength)throw Error('GLB文件头或长度损坏，请重新关联有效模型');
   const length=view.getUint32(12,true);if(view.getUint32(16,true)!==0x4e4f534a||length>bytes.byteLength-20)throw Error('GLB缺少有效模型描述');
   try{json=JSON.parse(new TextDecoder().decode(new Uint8Array(bytes,20,length)));}catch{throw Error('GLB模型描述损坏');}format='GLB';
  }else if(ext==='gltf'){
   try{json=JSON.parse(await blob.text());}catch{throw Error('glTF模型描述损坏');}format='glTF';
  }else throw Error('查看器仅支持自包含GLB / glTF');
  if(json.asset?.version!=='2.0'||!json.scenes?.length||!json.nodes?.length)throw Error('glTF 2.0场景或节点缺失');
  if([...(json.buffers||[]),...(json.images||[])].some(r=>r.uri&&!r.uri.startsWith('data:')))throw Error('模型依赖外部资源，请导出纹理与几何内嵌的GLB后重新关联');
  if(format==='glTF'&&(json.buffers||[]).some(r=>!r.uri))throw Error('glTF缺少内嵌几何数据，请使用自包含文件');
  if([...(json.extensionsUsed||[]),...(json.extensionsRequired||[])].some(e=>['KHR_draco_mesh_compression','KHR_texture_basisu','EXT_meshopt_compression'].includes(e)))throw Error('当前本地查看器未配置该压缩解码器，请导出未压缩自包含模型');
  return {format,nodes:json.nodes.length};
 }
 return {catalog,inspect};
});
