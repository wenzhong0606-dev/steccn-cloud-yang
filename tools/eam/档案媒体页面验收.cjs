const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('./组件测试/node_modules/jsdom');
const root=path.resolve(__dirname,'../../docs/eam/prototype');
const wait=async predicate=>{for(let i=0;i<600;i++){if(predicate())return;await new Promise(r=>setTimeout(r,10));}throw Error('媒体页面状态超时');};
(async()=>{
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM(fs.readFileSync(root+'/pages/device.html','utf8'),{url:'http://localhost/pages/device.html?equipment=EQ-001&tab=media',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc}),w=dom.window,d=w.document;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:String};w.matchMedia=()=>({matches:false,addEventListener(){}});w.ResizeObserver=class{observe(){}disconnect(){}};
 let urlId=0;w.URL.createObjectURL=()=> 'blob:test-'+(++urlId);w.URL.revokeObjectURL=()=>{};w.HTMLElement.prototype.scrollIntoView=function(){};w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>({ok:true,text:async()=>fs.readFileSync(path.join(root,new URL(src,w.document.baseURI).pathname),'utf8')});
 try{
  for(const node of [...d.querySelectorAll('script[src]')]){const script=d.createElement('script');script.textContent=fs.readFileSync(path.resolve(root,'pages',node.getAttribute('src').split('?')[0]),'utf8');d.body.append(script);}
  w.Eam.s.role='engineer';w.Eam.s.person='P05';
  const file=(id,category,extra={})=>({id,equipmentId:'EQ-001',category,status:'active',revision:'V1',name:id+'.'+(category==='image'?'png':'glb'),localContent:true,...extra});
  w.Eam.s.assetFiles.push(file('PRIMARY','image',{isPrimary:true}),file('SECOND','image'),file('MODEL-META','model',{localContent:false}),file('MODEL-BAD','model'),file('OTHER','model',{equipmentId:'EQ-002'}));
  w.EamFiles.get=async f=>{if(f.id==='MODEL-BAD')return new Blob(['bad GLB']);if(f.id==='MODEL-META')throw Error('本机没有文件内容');return new Blob(['image']);};
  await w.Eam.bootstrap(d);assert.deepEqual(errors,[]);assert.equal(d.getElementById('archive-page-error').hidden,true);
  assert.equal(d.getElementById('model-object'),null,'固定设备CSS假模型必须移除');
  assert(d.getElementById('media-image-stage').querySelector('img'),'缺少主图片查看区');assert.match(d.getElementById('gallery-count').textContent,/2/);
  assert.equal(d.querySelectorAll('#device-gallery button').length,2);assert.equal(d.querySelectorAll('#media-sample-gallery button').length,2,'实拍图片不能隐藏示意分组');
  const primaryUrl=d.getElementById('archive-photo').src;
  d.querySelector('#device-gallery button[data-file-id="SECOND"]').click();await wait(()=>d.getElementById('media-image-stage').querySelector('img')?.alt==='SECOND.png');assert.equal(d.getElementById('archive-photo').src,primaryUrl,'浏览相册不能覆盖指定主图');
  d.getElementById('media-image-primary').click();await wait(()=>w.Eam.s.assetFiles.find(f=>f.id==='SECOND').isPrimary);await w.EamArchiveGallery.refresh();assert.notEqual(d.getElementById('archive-photo').src,primaryUrl);
  d.getElementById('media-models-tab').click();assert.equal(d.getElementById('media-images-panel').hidden,true);assert.equal(d.getElementById('media-models-panel').hidden,false);assert.equal(d.querySelectorAll('#media-model-list button').length,2,'模型清单必须按设备隔离');
  d.querySelector('#media-model-list button[data-file-id="MODEL-META"]').click();assert.match(d.getElementById('media-model-status').textContent,/仅有元数据/);assert.equal(d.getElementById('media-model-load').disabled,true);
  d.querySelector('#media-model-list button[data-file-id="MODEL-BAD"]').click();d.getElementById('media-model-load').click();await wait(()=>d.getElementById('media-model-status').textContent.includes('GLB'));assert.equal(d.getElementById('media-model-reset').disabled,true);
  w.Eam.s.assetFiles.push(file('MODEL-GOOD','model'));await w.EamArchiveGallery.refresh();
  w.EamFiles.get=async()=>new Blob([JSON.stringify({asset:{version:'2.0'},scenes:[{nodes:[0]}],nodes:[{}]})]);
  w.Eam.s.assetFiles.find(f=>f.id==='MODEL-GOOD').name='self.gltf';await w.EamArchiveGallery.refresh();
  let finishMount;w.EamArchiveModelViewer.mount=host=>new Promise(resolve=>{finishMount=()=>{const viewer=d.createElement('model-viewer');host.replaceChildren(viewer);resolve({viewer,ready:Promise.resolve(),dispose:()=>viewer.remove()});};});
  d.querySelector('#media-model-list button[data-file-id="MODEL-GOOD"]').click();d.getElementById('media-model-load').click();await wait(()=>finishMount);
  d.querySelector('#media-model-list button[data-file-id="MODEL-META"]').click();finishMount();await new Promise(r=>setTimeout(r,20));
  assert.match(d.getElementById('media-model-stage').textContent,/仅有元数据/,'迟到的模型加载不得覆盖新选择');
  w.Eam.s.scenario='failure';await w.EamArchiveGallery.refresh();
  assert.equal(d.getElementById('media-image-next').disabled,true,'目录读取失败后不得通过下一张恢复旧资源');
  assert.equal(d.getElementById('archive-photo').hidden,true,'失败后不得保留未核验旧主图');
  assert(!d.getElementById('device-documents').textContent.includes('MODEL-'));assert.deepEqual(errors,[]);
  console.log('PASS 媒体分组、指定主图回读、设备隔离、模型元数据/损坏状态及文档边界');
 }finally{w.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
