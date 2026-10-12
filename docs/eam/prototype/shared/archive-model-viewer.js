/* 本地依赖按需加载；仅接收已检查的自包含模型，不启用AR或远程解码。 */
window.EamArchiveModelViewer=(()=>{
 let runtime;
 async function mount(host,blob,file){
  await EamArchiveMediaRules.inspect(blob,file.name);
  runtime ||= import('../vendor/model-viewer/model-viewer.min.js').catch(error=>{runtime=null;throw Error('本地3D查看器未能加载，请刷新重试');});
  await runtime;await customElements.whenDefined('model-viewer');
  const viewer=document.createElement('model-viewer'),url=URL.createObjectURL(blob);
  viewer.setAttribute('camera-controls','');viewer.setAttribute('touch-action','pan-y');viewer.setAttribute('interaction-prompt','none');viewer.setAttribute('alt',file.name+' · 可旋转和缩放的三维模型');viewer.setAttribute('shadow-intensity','0.6');viewer.setAttribute('environment-image','neutral');
  host.replaceChildren(viewer);
  let settled=false,timer;
  const ready=new Promise((resolve,reject)=>{
   const finish=error=>{if(settled)return;settled=true;clearTimeout(timer);error?reject(error):resolve();};
   viewer.addEventListener('load',()=>finish(),{once:true});viewer.addEventListener('error',()=>finish(Error('模型渲染失败：文件内容不完整或浏览器图形能力不可用')),{once:true});
   timer=setTimeout(()=>finish(Error('模型加载超时，请重试或使用较小模型')),45000);
  });
  viewer.src=url;
  return {viewer,ready,dispose:()=>{clearTimeout(timer);viewer.removeAttribute('src');viewer.remove();URL.revokeObjectURL(url);}};
 }
 function reset(viewer){viewer.cameraOrbit='0deg 75deg auto';viewer.cameraTarget='auto auto auto';viewer.fieldOfView='auto';viewer.jumpCameraToGoal();}
 return {mount,reset};
})();
