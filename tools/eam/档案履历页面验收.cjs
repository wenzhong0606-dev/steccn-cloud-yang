const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('./组件测试/node_modules/jsdom');
const root=path.resolve(__dirname,'../../docs/eam/prototype');
const wait=async predicate=>{for(let i=0;i<600;i++){if(predicate())return;await new Promise(r=>setTimeout(r,10));}throw Error('履历页面状态超时');};
(async()=>{
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM(fs.readFileSync(root+'/pages/device.html','utf8'),{url:'http://localhost/pages/device.html?equipment=EQ-001&tab=history',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc}),w=dom.window,d=w.document;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:String};w.matchMedia=()=>({matches:false,addEventListener(){}});w.ResizeObserver=class{observe(){}disconnect(){}};
 w.URL.createObjectURL=()=> 'blob:test';w.URL.revokeObjectURL=()=>{};w.HTMLElement.prototype.scrollIntoView=function(){};w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>({ok:true,text:async()=>fs.readFileSync(path.join(root,new URL(src,w.document.baseURI).pathname),'utf8')});
 try{
  for(const node of [...d.querySelectorAll('script[src]')]){const script=d.createElement('script');script.textContent=fs.readFileSync(path.resolve(root,'pages',node.getAttribute('src').split('?')[0]),'utf8');d.body.append(script);}
  w.Eam.s.role='engineer';w.Eam.s.person='P05';await w.Eam.bootstrap(d);assert.deepEqual(errors,[]);
  const enter=async domain=>{d.querySelector('[data-tab="'+domain+'"]').click();await wait(()=>d.getElementById('archive-loading').hidden);return d.querySelector('[data-record-domain="'+domain+'"]');};
  let host=await enter('history');assert(host.querySelector('ol[aria-label]'));assert.equal(host.querySelector('table'),null,'时间线与清单不能重复堆叠');
  const history=await w.EamArchiveApi.request('GET','/equipment/EQ-001/domains/history');assert(history.data.items.every(w.EamArchiveTraceRules.business));assert(history.data.items.some(x=>x.id==='EVT-001'));
  await w.EamArchiveApi.request('POST','/equipment/EQ-001/domains/lifecycle',{next:'stopped',reason:'履历回归停用'});const updated=(await w.EamArchiveApi.request('GET','/equipment/EQ-001/domains/history')).data.items;assert(updated.some(x=>x.evidence.includes('履历回归停用')));const audit=(await w.EamArchiveApi.request('GET','/equipment/EQ-001/domains/audit')).data.items;assert(w.EamArchiveTraceRules.filter(audit,{field:'lifecycle'}).some(x=>x.evidence.includes('履历回归停用')));
  [...host.querySelectorAll('button')].find(b=>b.textContent==='清单').click();assert(host.querySelector('table'));assert.equal(host.querySelector('ol'),null);
  host.querySelector('[data-trace-filter="query"]').value='首次投用';[...host.querySelectorAll('button')].find(b=>b.textContent==='应用筛选').click();assert.equal(host.querySelectorAll('tbody>tr').length,1);
  await enter('related');host=await enter('history');assert.equal(host.querySelector('[data-trace-filter="query"]').value,'首次投用','页签切换恢复筛选');
  host=await enter('audit');host.querySelector('[data-trace-filter="field"]').value='润滑周期';[...host.querySelectorAll('button')].find(b=>b.textContent==='应用筛选').click();assert.equal(host.querySelectorAll('tbody>tr').length,1);
  [...host.querySelectorAll('button')].find(b=>b.textContent==='详情').click();assert(d.getElementById('archive-record-dialog').open);assert(d.querySelector('table[aria-label="字段差异"]'));assert(d.getElementById('archive-record-professional').textContent.includes('45天'));assert(d.getElementById('archive-record-professional').textContent.includes('30天'));assert(d.getElementById('archive-record-edit').hidden);d.getElementById('archive-record-dialog').close();
  host=await enter('related');assert(host.textContent.includes('未结事项'));const related=await w.EamArchiveApi.request('GET','/equipment/EQ-001/domains/related');assert.equal(host.querySelectorAll('tbody>tr').length,Math.min(8,related.data.items.filter(x=>x.isPending===true).length));
  [...host.querySelectorAll('button')].find(b=>b.textContent==='全部关联').click();assert(host.textContent.includes('历史样本'));
  w.Eam.s.equipmentRecords.push({...w.Eam.s.equipmentRecords[0],id:'EQ-TRACE-TEST'});const other=await w.EamArchiveApi.request('GET','/equipment/EQ-TRACE-TEST/domains/history');assert(!other.data.items.some(x=>x.id==='EVT-001'));
  assert.deepEqual(errors,[]);console.log('PASS 生命周期互切与数据分工、筛选恢复、审计字段差异只读、未结关联、设备隔离');
 }finally{w.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
