const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('./组件测试/node_modules/jsdom');
const root=path.resolve(__dirname,'../../docs/eam/prototype');
const wait=async predicate=>{for(let i=0;i<500;i++){if(predicate())return;await new Promise(r=>setTimeout(r,10));}throw Error('页面加载超时');};
async function check(equipment='EQ-001'){
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM(fs.readFileSync(root+'/pages/device.html','utf8'),{url:'http://localhost/pages/device.html?equipment='+equipment,runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc}),w=dom.window,d=w.document;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:String};w.matchMedia=()=>({matches:false,addEventListener(){}});w.ResizeObserver=class{observe(){}disconnect(){}};
 w.URL.createObjectURL=()=> 'blob:local';w.URL.revokeObjectURL=()=>{};w.HTMLElement.prototype.scrollIntoView=function(){};w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>({ok:true,text:async()=>fs.readFileSync(path.join(root,new URL(src,w.document.baseURI).pathname),'utf8')});
 try{
  for(const node of [...d.querySelectorAll('script[src]')]){const script=d.createElement('script');script.textContent=fs.readFileSync(path.resolve(root,'pages',node.getAttribute('src').split('?')[0]),'utf8');d.body.append(script);}
  if(equipment!=='EQ-001')w.Eam.s.equipmentRecords.push({id:equipment,name:'其他设备',code:'OTHER',lifecycle:'active'});
  await w.Eam.bootstrap(d);assert.deepEqual(errors,[]);assert.equal(d.getElementById('archive-page-error').hidden,true);
  if(equipment!=='EQ-001'){assert.equal(d.getElementById('archive-open-count').textContent,'0');return;}
  const summary=(await w.EamArchiveApi.request('GET','/equipment/EQ-001/archive')).data;
  assert.equal(Number(d.getElementById('archive-file-count').textContent),d.getElementById('device-documents').children.length);
  const switchTab=async key=>{d.querySelector('[data-tab="'+key+'"]').click();await wait(()=>d.getElementById('archive-loading').hidden);return d.querySelector('[data-record-domain="'+key+'"]');};
  d.querySelector('[data-tab-target="related"]').click();await wait(()=>d.getElementById('archive-loading').hidden);
  let host=d.querySelector('[data-record-domain="related"]');assert.match(host.textContent,new RegExp('共\\s*'+summary.pending+'\\s*条'));assert.equal(host.querySelector('[data-archive-view="pending"]').getAttribute('aria-pressed'),'true');
  host=await switchTab('maintenance');assert.match(host.querySelector('[data-archive-view="tasks"]').textContent,new RegExp('（'+summary.maintenanceCount+'）'));
  host.querySelector('[data-archive-view="plans"]').click();assert.equal(host.querySelectorAll('tbody tr:not([data-table-state])').length,3);assert.match(host.textContent,/混合机月度润滑保养/);
  host=await switchTab('failure');assert(!host.querySelector('thead').textContent.includes('停机'));assert(host.textContent.includes('DF-1001'));assert(!host.textContent.includes('FL-1001'));
  host.querySelector('[data-archive-view="failures"]').click();assert(host.querySelector('thead').textContent.includes('停机'));assert(!host.textContent.includes('DF-1001'));assert(host.textContent.includes('FL-1001'));
  host=await switchTab('repair');assert.match(host.querySelector('tbody').textContent,/历史样本/);host.querySelector('button[aria-label^="查看"]').click();assert.match(d.getElementById('archive-record-professional').textContent,/对象与责任/);assert.match(d.getElementById('archive-record-professional').textContent,/执行与验收/);assert.match(d.getElementById('archive-record-professional').textContent,/后续与限制/);d.getElementById('archive-record-close').click();
  w.Eam.s.task.status='complete';w.Eam.s.results.push({id:'RESULT-TEST',taskId:w.Eam.s.task.id,executedBy:'P02',executedAt:'2026-10-12',receivedAt:'2026-10-12',items:[],submissionRevision:1});
  host=await switchTab('work');assert.equal(host.querySelectorAll('tbody tr:not([data-table-state])').length,0,'已完成点检不得留在待执行清单');
  assert.deepEqual(errors,[]);console.log('PASS 档案统计跳转、Bootstrap分视图、专业详情、完成任务隔离 '+equipment);
 }finally{w.close();}
}
(async()=>{await check();await check('EQ-002');})().catch(e=>{console.error(e);process.exitCode=1;});
