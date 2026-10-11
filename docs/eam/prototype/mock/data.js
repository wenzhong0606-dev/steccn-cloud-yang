window.EAM_DEMO = {
 equipment: {id:'EQ-001',code:'TC2_YL1_PL_01',name:'1# 混合机',model:'MX-2400',location:'原料二车间 / 配料线',owner:'原料运行班组',criticality:'关键设备',serial:'MX20240318',commissioned:'2024-03-18'},
 roles:[
 ['operator','操作人员','陈晓','当班设备','我的点检','我的报修','使用限制'],
 ['inspector','点检人员','李敏','今日路线','待执行任务','逾期任务','待同步草稿'],
 ['technician','维修人员','王强','我的工单','执行中','待完工','缺料工单'],
 ['supervisor','班组长','周平','班组设备','待派工','待验收','漏检处理'],
 ['engineer','设备工程师','赵工','关键设备','重复故障','策略待评','生成异常'],
 ['metrology','计量人员','孙工','测量设备','待校准','待确认','失准评估'],
 ['quality','质量人员','刘工','影响评估','待处置','纠正措施','效果验证'],
 ['safety','安全/环境人员','吴工','风险设备','作业阻断','待恢复','检验到期'],
 ['spare','备件人员','许工','待领料','待退料','替代待批','同步异常'],
 ['manager','管理层','郑经理','设备可用性','重大风险','停机影响','维护成本'],
 ['auditor','审核人员','杨工','审核对象','发现待改','待验证','证据缺失'],
 ['contractor','外部维保','外包张工','指派任务','待交付','退回作业','授权期限'],
 ['admin','系统管理员','管理员','集成通道','消费异常','同步失败','配置变更']
 ]
};

// 台账及设备树扩充样本，独立编号避免与用户新增记录冲突。
window.EAM_EQUIPMENT_SAMPLES={
  "equipment": [
    {
      "id": "DEMO-EQ-101",
      "code": "EAM_DEMO_101",
      "name": "2#混合机",
      "model": "MX-2400",
      "location": "原料二车间/配料线",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "MX-2400-101",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-102",
      "code": "EAM_DEMO_102",
      "name": "皮带输送机",
      "model": "B800",
      "location": "原料二车间/输送线",
      "ownerId": "P04",
      "manufacturer": "示例动力设备有限公司",
      "serial": "B800-102",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-103",
      "code": "EAM_DEMO_103",
      "name": "引风机",
      "model": "Y5-47",
      "location": "余热发电车间/锅炉系统",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "Y5-47-103",
      "commissionedAt": "2024-06-18",
      "criticality": "critical",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-104",
      "code": "EAM_DEMO_104",
      "name": "循环水泵",
      "model": "IS150-125",
      "location": "余热发电车间/循环水系统",
      "ownerId": "P04",
      "manufacturer": "示例动力设备有限公司",
      "serial": "IS150-125-104",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "stopped",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-105",
      "code": "EAM_DEMO_105",
      "name": "汽轮发电机组",
      "model": "QF-6",
      "location": "余热发电车间/发电系统",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "QF-6-105",
      "commissionedAt": "2024-06-18",
      "criticality": "critical",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-106",
      "code": "EAM_DEMO_106",
      "name": "CO₂吸收塔",
      "model": "AT-120",
      "location": "碳捕集车间/吸收系统",
      "ownerId": "P04",
      "manufacturer": "示例动力设备有限公司",
      "serial": "AT-120-106",
      "commissionedAt": "2024-06-18",
      "criticality": "critical",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-107",
      "code": "EAM_DEMO_107",
      "name": "再生塔",
      "model": "RT-80",
      "location": "碳捕集车间/再生系统",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "RT-80-107",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-108",
      "code": "EAM_DEMO_108",
      "name": "CO₂压缩机",
      "model": "CC-300",
      "location": "碳捕集车间/压缩系统",
      "ownerId": "P04",
      "manufacturer": "示例动力设备有限公司",
      "serial": "CC-300-108",
      "commissionedAt": "2024-06-18",
      "criticality": "critical",
      "lifecycle": "pending_scrap",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-109",
      "code": "EAM_DEMO_109",
      "name": "储液罐",
      "model": "TK-50",
      "location": "碳捕集车间/溶剂系统",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "TK-50-109",
      "commissionedAt": "",
      "criticality": "normal",
      "lifecycle": "draft",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-110",
      "code": "EAM_DEMO_110",
      "name": "空气压缩机",
      "model": "AC-75",
      "location": "公用工程/压缩空气站",
      "ownerId": "P04",
      "manufacturer": "示例动力设备有限公司",
      "serial": "AC-75-110",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    },
    {
      "id": "DEMO-EQ-111",
      "code": "EAM_DEMO_111",
      "name": "冷却塔",
      "model": "CT-600",
      "location": "公用工程/循环水站",
      "ownerId": "P05",
      "manufacturer": "示例工业装备制造有限公司",
      "serial": "CT-600-111",
      "commissionedAt": "2024-06-18",
      "criticality": "normal",
      "lifecycle": "active",
      "revision": 1,
      "createdBy": "P05"
    }
  ],
  "parts": [
    {
      "id": "DEMO-PART-101-0",
      "equipmentId": "DEMO-EQ-101",
      "code": "P-101-0",
      "name": "驱动组件",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-101-1",
      "equipmentId": "DEMO-EQ-101",
      "code": "P-101-1",
      "name": "主轴承",
      "parentId": "DEMO-PART-101-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-102-0",
      "equipmentId": "DEMO-EQ-102",
      "code": "P-102-0",
      "name": "输送机构",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-102-1",
      "equipmentId": "DEMO-EQ-102",
      "code": "P-102-1",
      "name": "托辊组件",
      "parentId": "DEMO-PART-102-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-103-0",
      "equipmentId": "DEMO-EQ-103",
      "code": "P-103-0",
      "name": "风机本体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-103-1",
      "equipmentId": "DEMO-EQ-103",
      "code": "P-103-1",
      "name": "叶轮",
      "parentId": "DEMO-PART-103-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-104-0",
      "equipmentId": "DEMO-EQ-104",
      "code": "P-104-0",
      "name": "泵本体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-104-1",
      "equipmentId": "DEMO-EQ-104",
      "code": "P-104-1",
      "name": "机械密封",
      "parentId": "DEMO-PART-104-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-105-0",
      "equipmentId": "DEMO-EQ-105",
      "code": "P-105-0",
      "name": "汽轮机",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-105-1",
      "equipmentId": "DEMO-EQ-105",
      "code": "P-105-1",
      "name": "转子",
      "parentId": "DEMO-PART-105-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-106-0",
      "equipmentId": "DEMO-EQ-106",
      "code": "P-106-0",
      "name": "塔体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-106-1",
      "equipmentId": "DEMO-EQ-106",
      "code": "P-106-1",
      "name": "填料层",
      "parentId": "DEMO-PART-106-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-107-0",
      "equipmentId": "DEMO-EQ-107",
      "code": "P-107-0",
      "name": "塔体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-107-1",
      "equipmentId": "DEMO-EQ-107",
      "code": "P-107-1",
      "name": "再沸器",
      "parentId": "DEMO-PART-107-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-108-0",
      "equipmentId": "DEMO-EQ-108",
      "code": "P-108-0",
      "name": "压缩机本体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-108-1",
      "equipmentId": "DEMO-EQ-108",
      "code": "P-108-1",
      "name": "气缸组件",
      "parentId": "DEMO-PART-108-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-109-0",
      "equipmentId": "DEMO-EQ-109",
      "code": "P-109-0",
      "name": "罐体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-109-1",
      "equipmentId": "DEMO-EQ-109",
      "code": "P-109-1",
      "name": "液位计",
      "parentId": "DEMO-PART-109-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-110-0",
      "equipmentId": "DEMO-EQ-110",
      "code": "P-110-0",
      "name": "压缩机本体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-110-1",
      "equipmentId": "DEMO-EQ-110",
      "code": "P-110-1",
      "name": "油气分离器",
      "parentId": "DEMO-PART-110-0",
      "revision": 1
    },
    {
      "id": "DEMO-PART-111-0",
      "equipmentId": "DEMO-EQ-111",
      "code": "P-111-0",
      "name": "塔体",
      "parentId": null,
      "revision": 1
    },
    {
      "id": "DEMO-PART-111-1",
      "equipmentId": "DEMO-EQ-111",
      "code": "P-111-1",
      "name": "布水器",
      "parentId": "DEMO-PART-111-0",
      "revision": 1
    }
  ]
};
