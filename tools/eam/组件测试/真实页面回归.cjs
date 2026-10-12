/* 使用页面实际JSON/API脚本，不用宽容业务代理；DOM环境替身只覆盖浏览器API。 */
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('jsdom');
const root=path.resolve(__dirname,'../../../docs/eam/prototype');
async function check(file,missingPhoto=false){
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>{if(!/Not implemented: navigation/.test(e.message))errors.push(e.message);});
 const html=fs.readFileSync(path.join(root,'pages',file),'utf8');
 const dom=new JSDOM(html,{url:'http://localhost/pages/'+file+(missingPhoto?'?equipment=DEMO-NO-PHOTO':''),runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc});const w=dom.window;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:s=>String(s).replace(/[^\w-]/g,'\\$&')};
 w.matchMedia=()=>({matches:false,addEventListener(){},removeEventListener(){}});
 w.ResizeObserver=class{observe(){}disconnect(){}};
 w.URL.createObjectURL=()=> 'blob:local';w.URL.revokeObjectURL=()=>{};
 w.HTMLElement.prototype.scrollIntoView=function(){};
 w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>{const url=new URL(src,w.document.baseURI),filePath=path.join(root,url.pathname.replace(/^\//,''));return {ok:true,text:async()=>fs.readFileSync(filePath,'utf8')};};
 try{
  for(const el of [...w.document.querySelectorAll('script[src]')]){
   const p=path.resolve(root,'pages',el.getAttribute('src').split('?')[0]);
   if(missingPhoto&&p.endsWith('state.js')){const state=w.EamDomain.initial();state.equipmentRecords.push({id:'DEMO-NO-PHOTO',code:'QF-6-105',name:'汽轮发电机组',model:'QF-6',serial:'QF-6-105',ownerId:'P05',location:'余热发电车间',lifecycle:'active'});w.sessionStorage.setItem('eam-demo-v3',JSON.stringify(state));}
   const script=w.document.createElement('script');script.textContent=fs.readFileSync(p,'utf8');w.document.body.append(script);
  }
  const res=await w.Eam.bootstrap(w.document);assert.equal(res.ok,true);
  assert.equal(w.document.querySelectorAll('[data-component-loaded="failed"]').length,0);
  assert.equal(w.document.querySelectorAll('main h1').length,1);
  assert.deepEqual(errors,[]);
  const ids=[...w.document.querySelectorAll('[id]')].map(e=>e.id);assert.equal(new Set(ids).size,ids.length,'重复ID');
  if(w.document.body.dataset.layout==='list'){
   for(const panel of w.document.querySelectorAll('section[data-component="list-panel"]')){
    if(panel.querySelector('tbody tr:not([data-table-state])'))assert.match(panel.querySelector('.list-panel-footer')?.textContent||'',/共\s*\d+\s*条/,'有业务数据但未渲染分页');
   }
  }
  if(file==='equipment.html'){
   const original=w.document.querySelector('main'),second=original.cloneNode(true);second.id='secondary-main';second.dataset.instance='secondary';w.document.body.append(second);w.Eam.prefixIds(second,'secondary');
   w.initEquipment(second);const find=id=>w.Eam.find(second,id),originalRows=w.Eam.find(original,'equipment-list').children.length;
   find('search').value='不存在的第二实例设备';find('search').dispatchEvent(new w.Event('input',{bubbles:true}));
   assert.equal(find('equipment-list').querySelectorAll('tr:not([data-table-state])').length,0);
   assert.equal(w.Eam.find(original,'equipment-list').children.length,originalRows,'第二实例筛选改变第一实例');
   console.log('PASS 设备台账真实业务双实例隔离');second.remove();
  }
  if(missingPhoto){assert.equal(w.document.getElementById('archive-photo').hidden,true);assert.equal(w.document.getElementById('archive-photo-empty').hidden,false);assert.equal(w.document.getElementById('archive-photo').parentElement.className,'archive-photo-slot');assert.match(w.document.getElementById('device-model').textContent,/QF-6/);assert.match(w.document.getElementById('archive-status-strip').textContent,/未知/);console.log('PASS 无图片设备档案保持媒体占位和独立状态');}
  else console.log('PASS '+file+' · 实际脚本/片段初始化');
 }finally{w.close();}
}
(async()=>{let failed=0;const files=fs.readdirSync(path.join(root,'pages')).filter(f=>f.endsWith('.html')&&!f.startsWith('_'));
 for(const file of files)try{await check(file);}catch(e){failed++;console.error('FAIL '+file+' '+e.message);}
 try{await check('device.html',true);}catch(e){failed++;console.error('FAIL 无图片设备档案 '+e.message);}
 console.log('真实页面回归：'+(files.length-failed)+'通过 / '+failed+'失败');process.exitCode=failed?1:0;
})();
