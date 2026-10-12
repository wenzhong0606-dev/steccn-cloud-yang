/* 统一图表与原始读数口径；无效读数保留但不得连成有效曲线。 */
(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.EamArchiveTrendRules=api;})(typeof window==='undefined'?globalThis:window,function(){
 function time(value){if(typeof value!=='string')return NaN;const s=value.trim();return Date.parse(/^\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}(:\d{2})?$/.test(s)?s.replace(' ','T')+(s.length===16?':00':'')+'+08:00':s);}
 function select(rows,config,state){
  let from=-Infinity,to=Infinity;
  if(state.range==='custom'){from=time(state.from);to=time(state.to);if(!Number.isFinite(from)||!Number.isFinite(to)||from>to)throw Error('起始与结束时间必须有效，且起始时间不得晚于结束时间');}
  if(['day','week'].includes(state.range)){to=time(state.asOf);if(!Number.isFinite(to))throw Error('来源未提供有效快照时间，无法确定相对时间范围');from=to-(state.range==='day'?24:168)*3600000;}
  const raw=rows.filter(row=>(!row.pointId||row.pointId===config.pointId)&&(state.range==='all'||(time(row.eventAt)>=from&&time(row.eventAt)<=to))).slice().sort((a,b)=>(Number.isFinite(time(a.eventAt))?time(a.eventAt):Infinity)-(Number.isFinite(time(b.eventAt))?time(b.eventAt):Infinity));
  const valid=[],segments=[];let segment=[];
  for(const row of raw){if(row.quality===config.quality&&typeof row[config.metric]==='number'&&Number.isFinite(row[config.metric])&&Number.isFinite(time(row.eventAt))){valid.push(row);segment.push(row);}else if(segment.length){segments.push(segment);segment=[];}}
  if(segment.length)segments.push(segment);
  return {raw,valid,segments,exceeded:valid.filter(row=>(Number.isFinite(config.upper)&&row[config.metric]>config.upper)||(Number.isFinite(config.lower)&&row[config.metric]<config.lower))};
 }
 const quality=value=>({GOOD:'合格',BAD:'无效',UNCERTAIN:'不确定',STALE:'失效',UNKNOWN:'未知'})[value]||'未知（'+(value||'未提供')+'）';
 return {time,select,quality};
});
