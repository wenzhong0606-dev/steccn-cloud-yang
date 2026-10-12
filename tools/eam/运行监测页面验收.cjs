const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {JSDOM,VirtualConsole}=require('./组件测试/node_modules/jsdom');
const root=path.resolve(__dirname,'../../docs/eam/prototype');
const wait=async predicate=>{for(let i=0;i<600;i++){if(predicate())return;await new Promise(r=>setTimeout(r,10));}throw Error('运行监测页面状态超时');};
(async()=>{
 const errors=[],vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM(fs.readFileSync(root+'/pages/device.html','utf8'),{url:'http://localhost/pages/device.html?equipment=EQ-001&tab=trend',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc}),w=dom.window,d=w.document;
 w.__EamNoAuto=true;w.structuredClone=structuredClone;w.CSS={escape:String};w.matchMedia=()=>({matches:false,addEventListener(){}});w.ResizeObserver=class{constructor(callback){this.callback=callback;}observe(host){host.__resizeTest=this.callback;}disconnect(){}};
 let urlId=0;w.URL.createObjectURL=()=> 'blob:test-'+(++urlId);w.URL.revokeObjectURL=()=>{};w.HTMLElement.prototype.scrollIntoView=function(){};w.HTMLDialogElement.prototype.showModal=function(){this.open=true;};w.HTMLDialogElement.prototype.close=function(){this.open=false;};
 w.fetch=async src=>({ok:true,text:async()=>fs.readFileSync(path.join(root,new URL(src,w.document.baseURI).pathname),'utf8')});
 try{
  for(const node of [...d.querySelectorAll('script[src]')]){const script=d.createElement('script');script.textContent=fs.readFileSync(path.resolve(root,'pages',node.getAttribute('src').split('?')[0]),'utf8');d.body.append(script);}
  w.Eam.s.role='engineer';w.Eam.s.person='P05';
  await w.Eam.bootstrap(d);assert.deepEqual(errors,[]);
  d.querySelector('[data-tab="trend"]').click();await wait(()=>d.getElementById('archive-loading').hidden);
  let host=d.querySelector('[data-record-domain="trend"]');
  assert(host.querySelector('svg'),'应默认展示主图');assert.equal(host.querySelector('table'),null,'主图与原始表格不得同时堆叠');
  Object.defineProperty(host,'clientWidth',{value:300,configurable:true});host.querySelector('[data-trend-view="chart"]').click();
  assert.equal(Number(host.querySelector('svg').getAttribute('viewBox').split(' ')[2]),300,'窄屏必须重排坐标，不得缩小720px画布');
  assert(Number(host.querySelector('circle[role="button"]').getAttribute('r'))>=24,'采样点需要触屏命中区域');
  const redraw=host.querySelector('[data-trend-view="chart"]');w.EamArchiveTrendView.state(host,'loading');Object.defineProperty(host,'clientWidth',{value:301,configurable:true});host.__resizeTest();assert.equal(host.querySelector('svg'),null,'改变宽度不得覆盖加载提示');w.EamArchiveTrendView.state(host,'error','读取失败');host.__resizeTest();assert.match(host.textContent,/读取失败/);redraw.click();
  assert.equal(host.querySelector('select[aria-label="测点选择"]').options.length,2);
  const point=host.querySelector('select[aria-label="测点选择"]');point.value='CTR-HOURS-01';point.dispatchEvent(new w.Event('change'));
  assert.match(host.textContent,/单位 h/);assert(!host.textContent.includes('75 °C'));
  host.querySelector('[data-trend-view="raw"]').click();assert.equal(host.querySelector('svg'),null);assert.equal(host.querySelectorAll('tbody tr').length,3);assert.match(host.querySelector('thead').textContent,/h/);
  const range=host.querySelector('select[aria-label="时间范围"]');range.value='custom';range.dispatchEvent(new w.Event('change'));
  host.querySelector('[aria-label="起始时间"]').value='2026-10-10T07:15';host.querySelector('[aria-label="结束时间"]').value='2026-10-10T07:45';host.querySelector('[data-trend-apply]').click();assert.equal(host.querySelectorAll('tbody tr').length,1);
  w.Eam.s.scenario='stale';d.getElementById('archive-refresh').click();await wait(()=>d.getElementById('archive-refresh').disabled===false&&d.getElementById('archive-loading').hidden);assert.match(host.textContent,/信号失效/);
  w.Eam.s.scenario='failure';d.getElementById('archive-refresh').click();await wait(()=>d.getElementById('archive-refresh').disabled===false);assert.equal(host.querySelector('svg'),null);
  assert.deepEqual(errors,[]);console.log('PASS 运行监测测点单位、图表/原始数据、范围筛选、失效信号与错误状态');
 }finally{w.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
