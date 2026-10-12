const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('./组件测试/node_modules/jsdom');
const root=path.resolve(__dirname,'../../docs/eam/prototype');
(async()=>{
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM(fs.readFileSync(root+'/pages/abnormal-events.html','utf8'),{url:'http://localhost/pages/abnormal-events.html?id=AB-1004&equipment=EQ-001&returnEquipment=EQ-001&returnTab=related',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc}),w=dom.window,d=w.document;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:String};w.matchMedia=()=>({matches:false,addEventListener(){}});w.ResizeObserver=class{observe(){}disconnect(){}};w.HTMLElement.prototype.scrollIntoView=function(){};w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>({ok:true,text:async()=>fs.readFileSync(path.join(root,new URL(src,w.document.baseURI).pathname),'utf8')});
 try{for(const node of [...d.querySelectorAll('script[src]')]){const script=d.createElement('script');script.textContent=fs.readFileSync(path.resolve(root,'pages',node.getAttribute('src').split('?')[0]),'utf8');d.body.append(script);}w.Eam.s.role='engineer';w.Eam.s.person='P05';await w.Eam.bootstrap(d);
  for(let i=0;i<600&&!d.getElementById('detail-dialog').open;i++)await new Promise(r=>setTimeout(r,10));assert(d.getElementById('detail-dialog').open);let destination;w.Eam.go=(page,query)=>{destination={page,...query};};d.getElementById('detail-equipment').click();assert.deepEqual(JSON.parse(JSON.stringify(destination)),{page:'device',equipment:'EQ-001',tab:'related'});assert.deepEqual(errors,[]);console.log('PASS 实际异常详情按钮返回原设备关联页签');
 }finally{w.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
