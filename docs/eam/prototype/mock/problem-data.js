/* 发现与问题独立样本；接口读取时深拷贝。 */
(function(root){const data={
 equipment:[{id:'EQ-001',name:'1#混合机',code:'TC2_YL1_PL_01',location:'原料二车间 / 配料线'}],
 people:[{id:'P05',name:'赵工'},{id:'P04',name:'周平'},{id:'P03',name:'王强'},{id:'P02',name:'李敏'}],
 discoveries:[
 {id:'AB-1001',equipmentId:'EQ-001',description:'主轴振动增大，间歇出现异响',source:'点检发现',part:'传动主轴',priority:'high',status:'submitted',ownerId:'P05',reporter:'李敏',occurredAt:'2026-10-10T08:15',evidence:'测点V-01：4.8 mm/s；现场记录IN-1001',links:[],version:1},
 {id:'AB-1002',equipmentId:'EQ-001',description:'减速箱密封处油迹',source:'现场发现',part:'减速箱',priority:'medium',status:'triaged',ownerId:'P05',reporter:'陈晓',occurredAt:'2026-10-09T14:20',evidence:'现场观察：滴漏约2滴/分钟',links:[{kind:'defects',id:'DF-1001'}],version:2},
 {id:'AB-1003',equipmentId:'EQ-001',description:'负载升高后驱动跳闸，混合功能中断',source:'运行告警',part:'驱动电机',priority:'high',status:'triaged',ownerId:'P05',reporter:'运行监测',occurredAt:'2026-10-10T09:00',evidence:'AL-882：电流过载事件',links:[{kind:'failures',id:'FL-1001'}],version:2},
 {id:'AB-1004',equipmentId:'EQ-001',description:'防护罩固定螺栓松动，已停止接近危险部位',source:'现场发现',part:'联轴器防护罩',priority:'high',status:'submitted',ownerId:'P04',reporter:'王强',occurredAt:'2026-10-10T10:10',evidence:'紧急措施：警戒并通知班组长；隐患协同页待后续批次',links:[],version:1}],
 defects:[
 {id:'DF-1001',equipmentId:'EQ-001',description:'减速箱输出轴密封渗油',part:'减速箱',priority:'medium',status:'open',ownerId:'P05',dueAt:'2026-10-12',occurredAt:'2026-10-09T14:20',links:[{kind:'discoveries',id:'AB-1002'}],evidence:'已确认密封偏离，尚未完成功能失效判定',version:1},
 {id:'DF-1002',equipmentId:'EQ-001',description:'润滑管路接头松动',part:'润滑系统',priority:'medium',status:'pending_verification',ownerId:'P05',dueAt:'2026-10-10',occurredAt:'2026-10-08T09:20',executorId:'P03',evidence:'已紧固接头；压力保持30分钟无下降',links:[],version:2},
 {id:'DF-1003',equipmentId:'EQ-001',description:'主轴轴承振动趋势升高',part:'传动主轴',priority:'high',status:'in_progress',ownerId:'P05',dueAt:'2026-10-09',occurredAt:'2026-10-07T08:30',evidence:'已安排复测，暂未判定原因',links:[],version:1},
 {id:'DF-1004',equipmentId:'EQ-001',description:'观察窗密封老化',part:'罐体',priority:'low',status:'closed',ownerId:'P05',dueAt:'2026-10-08',occurredAt:'2026-10-06T13:00',evidence:'更换密封后气密复测合格',verifiedBy:'P04',links:[],version:3}],
 failures:[
 {id:'FL-1001',equipmentId:'EQ-001',description:'驱动过载跳闸导致混合功能中断',part:'驱动电机',priority:'high',status:'confirmed',ownerId:'P05',dueAt:'2026-10-10',occurredAt:'2026-10-10T09:00',failureMode:'驱动过载保护动作',impact:'混合功能中断 / 产线等待',evidence:'保护事件与现场核实一致',links:[{kind:'discoveries',id:'AB-1003'}],version:1},
 {id:'FL-1002',equipmentId:'EQ-001',description:'上料阀卡涩导致进料功能失效',part:'上料阀',priority:'medium',status:'restored',ownerId:'P05',dueAt:'2026-10-07',occurredAt:'2026-10-07T11:00',restoredAt:'2026-10-07T11:45',failureMode:'阀门卡涩',impact:'进料功能中断',executorId:'P03',evidence:'清理异物后连续动作10次正常，待独立复核',links:[],version:2}], audit:[],requests:[]};
 if(typeof module==='object'&&module.exports)module.exports=data;else root.EAM_PROBLEM_JSON=data;
})(typeof window==='undefined'?globalThis:window);
