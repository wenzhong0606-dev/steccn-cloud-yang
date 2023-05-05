package com.steccn.qms.constant;

import com.google.common.collect.Sets;
import com.steccn.qms.dto.QtImproveRecordDetailDTO;
import com.steccn.qms.dto.QtImproveRecordDisposeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yequan
 * @desc 质量管理的常量接口
 */
public interface QtManagerConstant {


    String QT_CHECK_ITEM_SORT_FIELD_NAME = "name";

    String QT_CHECK_ITEM_SORT_FIELD_STATUS = "status";

    String QT_CHECK_ITEM_SORT_FIELD_CREATE_TIME = "create_time";

    String QT_CHECK_ITEM_SORT_FIELD_UPDATE_TIME = "update_time";

    String QT_CHECK_ITEM_SORT_FIELD_CODE = "code";

    String QT_CHECK_TASK_SORT_FIELD_CODE = "code";

    String QT_CHECK_TASK_SORT_FIELD_STATUS = "status";

    String QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME = "materialName";

    String QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME = "arrivalTime";

    String QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME = "supplierName";

    String QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME = "materialWorkshopName";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER = "productOrder";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME = "productWorkshopName";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME = "productLineName";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH = "productBatch";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME = "defectStartTime";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE = "originalTaskCode";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_ORDER = "deliverOrder";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_TIME = "deliverTime";

    String QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME = "customerName";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_NUM = "deliverNum";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_RESULT = "result";

    String QT_CHECK_TASK_SORT_FIELD_CREATE_NAME = "createName";

    String QT_CHECK_TASK_SORT_FIELD_CREATE_TIME = "createTime";

    String QT_CHECK_TASK_SORT_FIELD_SPOT_TIME = "spotTime";

    String QT_CHECK_TASK_FIELD_CHECK_RESULT = "checkResult";

    String QT_CHECK_TASK_FIELD_CHECK_DEFECT = "defectId";

    String QT_CHECK_TASK_FIELD_CHECK_DEFECTTEM = "defect";
    String QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME = "finishedTime";

    String QT_CHECK_TASK_COPY__SORT_FIELD = "samplingTime";

    String QT_CHECK_TASK_SORT_FIELD_CLASS_ID = "classId";

    String QT_CHART_EXCEL_MODEL_URL = "http://61.177.139.244/images/";

    String QT_CHART_EXCEL_MODEL_NAME_JPCP = "JPCP.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_PLCJ = "PLCJ.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_SJGQXLBFB = "SJGQXLBFB.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_SJGQXX = "SJGQXX.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_JPQXX = "JPQXX.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_SJGQXLBQS = "SJGQXLBQS.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_SJGQXXQS = "SJGQXXQS.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_JQQXXQS = "JPQXXQS.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_SJGXJHGLQS = "SJGXJHGLQS.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_LLJ = "LLJ.xlsx";

    //String QT_CHART_EXCEL_MODEL_NAME_JPXJHGL = "JPXJHGL.xlsx";

    String QT_CHART_SPC_NAME = "SPC.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_JPSH = "JPSH.xlsx";

    String QT_CHART_EXCEL_MODEL_NAME_JPHY = "JPHY.xlsx";

    String QT_CHART_EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    String QT_CHART_EXCEL_ENCODE = "UTF-8";

    String QT_CHART_JPCP_NAME = "基片成品趋势";

    String QT_CHART_JPSH_NAME = "基片损耗分布";

    String QT_CHART_SPC_REPORT_NAME = "报表卡片名称";

    String QT_CHART_DEEP_PROCESS_NAME = "深加工巡检合格率趋势";

    String QT_CHART_SUB_PROCESS_NAME = "基片巡检合格率";

    String QT_CHART_ACCESSORY_PROCESS_NAME = "配料抽检合格率";

    String QT_CHART_DEFECTIVE_CATEGORY_NAME = "深加工缺陷类别分布";

    String QT_CHART_DEFECTIVE_ITEM_NAME = "深加工缺陷项分布";

    String QT_CHART_SUBSTRATE_ITEM_NAME = "基片缺陷项分布";

    String QT_CHART_DEFECTIVE_CATEGORY_LINE_NAME = "深加工缺陷类别趋势";

    String QT_CHART_DEEP_ITEM_LINE_NAME = "深加工缺陷项趋势";

    String QT_CHART_SUBSTRATE_ITEM_LINE_NAME = "基片缺陷项趋势";

    String QT_CHART_SUB_JPCJ = "基片化验抽检合格率趋势";

    String QT_CHART_ACCEPTABILITY_LLJ = "来料检合格率";

    String QT_CHART_DEFECT_JPFB = "DEFECTJPFB.xlsx";

    String QT_CHART_DEFECT_JPFB_NAME = "基片缺陷分布";

    String QT_CHART_EXCEL_MODEL_NAME_JPXJHGLQS = "JPXJHGLQS.xlsx"; //基片巡检合格率

    /**
     * 基片二车间ID
     */
    Integer QT_JP_2_ID = 76;

    //质量管控模块样本概况字段常量
    String SAMPLENUM = "sampleNum"; //样本编号
    String LICENSEPLATE = "licensePlate"; //车牌号
    String ANALYST = "analyst"; //分析人
    String ANALYSTNAME = "analystName"; //分析人名
    String SAMPLINGTIME = "samplingTime"; //取样时间
    String SAMPLINGTIMEMIX = "samplingTimeMix"; //配料抽检取样时间
    String ANALYSTIME = "analystTime"; //分析时间
    String INGREDIENTNUM = "ingredientNum"; //配料付数

    /**
     * 机构类型编码
     */
    String OFFICE_WORKSHOP_TYPE = "8";
    String OFFICE_PRODUCE_LINE_TYPE = "9";

    /**
     * 良次
     */
    String QT_TASK_GOOD = "良";
    String QT_TASK_BAD = "次";

    /**
     * SPC 任务详情的表头
     */
    String SPC_HEADER_FIELD_ORDER = "检验任务单号";
    String SPC_HEADER_FIELD_FINISHED_TIME = "检验完成时间";
    String SPC_HEADER_FIELD_SPOT_TIME = "抽检时间";
    String SPC_HEADER_FIELD_STATION = "工位";
    String SPC_HEADER_FIELD_MATERIAL_NAME = "物料名称";
    String SPC_HEADER_FIELD_PRODUCT_LINE = "生产产线";
    String SPC_HEADER_FIELD_WORKSHOP = "物料使用车间";
    String SPC_HEADER_FILELD_SUPPLIER = "供应商";
    String SPC_HEADER_FIELD_ORDER_VALUE = "code";
    String SPC_HEADER_FIELD_FINISHED_TIME_VALUE = "finishedTime";
    String SPC_HEADER_FIELD_SPOT_TIME_VALUE = "spotTime";
    String SPC_HEADER_FIELD_FINISHED_CHECK_RESULT_VALUE = "checkResult";
    String SPC_HEADER_FIELD_MATERIAL_NAME_VALUE = "materialName";
    String SPC_HEADER_FIELD_PRODUCT_LINE_VALUE = "productLine";
    String SPC_HEADER_FIELD_WORKSHOP_VALUE = "workshop";
    String SPC_HEADER_FIELD_STATION_VALUE = "station";
    String SPC_HEADER_FILELD_SUPPLIER_VALUE = "supplier";
    String SPC_REPORT_NAME = "SPC名称:";
    String SPC_REPORT_MATERIAL_NAME = "物料名称:";
    String SPC_REPORT_CHECK_ITEM_NAME = "检验项名称:";


    String SPC_REPORT_SAMPLING_TIME = "取样日期";
    String SPC_REPORT_SAMPLING_TIME_VALUE = "samplingTime";

    /**
     * 首检质量日报表 常量
     */
    String DAY_REPORT_FIELD_RESULT = "检验结果";
    String DAY_REPORT_FIELD_RESULT_VALUE = "result";
    String DAY_REPORT_FIELD_COPY = "样本序号";
    String DAY_REPORT_FIELD_COPY_VALUE = "copy";
    String DAY_REPORT_FIELD_NOTE = "备注";
    String DAY_REPORT_FIELD_NOTE_VALUE = "note";
    String DAY_REPORT_FIELD_ORDER = "班次";
    String DAY_REPORT_FIELD_ORDER_VALUE = "className";
    String DAY_REPORT_FIELD_APPARENT_CONDITION = "外观状态";

    /**
     * 车间名称
     */
    String DEEP_OFFICE_NAME = "深加工";
    String DEEP_OFFICE_NAME_ONE = "深加工一车间";
    String DEEP_OFFICE_NAME_TWO = "深加工二车间";
    String DEEP_OFFICE_NAME_THREE = "深加工三车间";

    String SUB_OFFICE_NAME = "基片";
    String SUB_OFFICE_NAME_ONE = "基片一车间";
    String SUB_OFFICE_NAME_TWO = "基片二车间";

    /**
     * 正则表达式表示为非负浮点数
     */
    String REGULAR_NON_FLOATING_POINT_NUMBER = "([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)";
    String REGULAR_NON_NUMBER = "\\d+";

    /**
     * 计算结果为"0"
     */
    String THE_CALCULATION_RESULTS = "0";

    /**
     * beforeHour 代表早上8点
     * afterHour  代表晚上20点
     */
    final int beforeHour = 8;
    final int afterHour = 20;
    final long hourSpace = 2;

    /**
     * 小时粒度下  限制开始时间和结束时间的差值为15天
     */
    Integer GRANULARITY_HOUR_DAY_DIFF = 15;


    /**
     * 日环比,周环比标志
     */
    Boolean DAY_FLAG = false;
    Boolean WEEK_FLAG = false;

    /**
     * 限制只取一条
     */
    String STR_LIMIT_1 = " limit 1 ";

    /**
     * 客诉排序字段-编码
     */
    String QT_CUSTOMER_COMPLAIN_CODE = "code";


    /**
     * 客诉排序字段-标题
     */
    String QT_CUSTOMER_COMPLAIN_title = "title";

    /**
     * 客诉排序字段-投诉客户
     */
    String QT_CUSTOMER_COMPLAIN_CUSTOMER = "complainCustomer";

    /**
     * 客诉排序字段-投诉日期
     */
    String QT_CUSTOMER_COMPLAIN_DATE = "complainDate";

    /**
     * 客诉排序字段-投诉接收人
     */
    String QT_CUSTOMER_COMPLAIN_RECEIVER = "complainReceiver";

    /**
     * 客诉排序字段-投诉类型
     */
    String QT_CUSTOMER_COMPLAIN_TYPE = "complainType";

    /**
     * 客诉排序字段-处理人
     */
    String QT_CUSTOMER_COMPLAIN_DEAL_MAN = "dealMan";

    /**
     * 客诉排序字段-状态
     */
    String QT_CUSTOMER_COMPLAIN_STATUS = "status";

    /**
     * 客诉排序字段-处理方式
     */
    String QT_CUSTOMER_COMPLAIN_DEAL_WAY = "dealWay";

    /**
     * 不合格品处理排序-检验单号
     */
    String QT_DEFECT_PRODUCT_TASK_CODE = "taskCode";

    /**
     * 不合格品处理排序-检验类型
     */
    String QT_DEFECT_PRODUCT_TASK_TYPE = "taskType";


    /**
     * 不合格品处理排序-检验类型
     */
    String QT_DEFECT_PRODUCT_MATERIAL_NAME = "materialName";

    /**
     * 不合格品处理排序-检验完成人员
     */
    String QT_DEFECT_PRODUCT_TASK_FINISH_MAN = "taskFinishMan";

    /**
     * 不合格品处理排序-检验完成人员
     */
    String QT_DEFECT_PRODUCT_TASK_FINISH_TIME = "taskFinishTime";

    /**
     * 不合格品处理排序-处理人
     */
    String QT_DEFECT_PRODUCT_DEAL_MAN = "dealMan";

    /**
     * 不合格品处理排序-状态
     */
    String QT_DEFECT_PRODUCT_STATUS = "status";

    /**
     * 不合格品处理排序-处理方式
     */
    String QT_DEFECT_PRODUCT_DEAL_WAY = "dealWay";

    /**
     * 整改排序字段-整改单号
     */
    String QT_IMPROVE_RECORD_ORDER = "improveOrder";

    /**
     * 整改排序字段-描述
     */
    String QT_IMPROVE_RECORD_DESCRIPTION = "description";

    /**
     * 整改排序字段-关联客诉标题
     */
    String QT_IMPROVE_RECORD_CUSTOMER_COMPLAIN_TITLE = "customerComplainTitle";


    /**
     * 整改排序字段-关联不合格品处理标题
     */
    String QT_IMPROVE_RECORD_DEFECT_PRODUCT_TITLE = "defectProductTitle";


    /**
     * 整改排序字段-创建人
     */
    String QT_IMPROVE_RECORD_CREATE_NAME = "createName";

    /**
     * 整改排序字段-创建时间
     */
    String QT_IMPROVE_RECORD_CREATE_TIME = "createTime";


    /**
     * 整改排序字段-整改启动审批人
     */
    String QT_IMPROVE_RECORD_START_APPROVER_NAME = "startApproverName";

    /**
     * 整改排序字段 - 状态
     */
    String QT_IMPROVE_RECORD_STATUS = "status";

    /**
     * 整改管理员角色编码
     */
    String IMPROVE_ADMINISTRATOR = "improveAdministrator";


    /**
     * 客诉管理员角色编码
     */
    String COMPLAINT_MANAGER = " complaintManager";



    /**
     * 不合格品管理员角色编码
     */
    String DEFECT_PRODUCT_MANAGER = " defectProductManager";


    Set<Integer> states = Sets.newHashSet(2,3,4,5,6,7,8);

    /**
     * oa不合格品处理审批发起人
     */
    String WORKFLOW_SPFQR = "spfqr";

    /**
     * 不合格品处理单号
     */
    String WORKFLOW_BHGPCLDH = "bhgpcldh";


    /**
     * 客诉处理单号
     */
    String WORKFLOW_KSDH = "ksdh";

    /**
     * 整改单号
     */
    String WORKFLOW_ZGDH = "zgdh";

    /**
     * 整改标题
     */
    String WORKFLOW_ZGBT = "zgbt";

    /**
     * 客诉标题
     */
    String WORKFLOW_KSBT ="ksbt";

    /**
     *  描述
     */
    String WORKFLOW_MS = "ms";

    /**
     * 投诉客户
     */
    String WORKFLOW_TSKH ="tskh";

    /**
     * 投诉日期
     */
    String WORKFLOW_TSRQ = "tsrq";

    /**
     * 投诉接收人
     */
    String WORKFLOW_TSJSR = "tsjsr";

    /**
     * 投诉创建人
     */
    String WORKFLOW_TSCJR = "tscjr";

    /**
     * 投诉创建时间
     */
    String WORKFLOW_TSCJSJ = "tscjsj";

    /**
     * 投诉类型
     */
    String WORKFLOW_TSLX =  "tslx";

    /**
     * 抄送
     */
    String WORKFLOW_CS = "cs";

    /**
     * 箱号
     */
    String WORKFLOW_XH = "xh";

    /**
     * 生产批次号
     */
    String WORKFLOW_SCPCH = "scpch";

    /**
     * 发货数量
     */
    String WORKFLOW_FHSL = "fhsl";

    /**
     * 投诉数量
     */
    String WORKFLOW_TSSL = "tssl";

    /**
     * 上线数量
     */
    String WORKFLOW_SXSL = "sxsl";

    /**
     * 不良率
     */
    String WORKFLOW_BLL = "bll";


    /**
     * 抽检数量
     */
    String WORKFLOW_CJSL =  "cjsl";

    /**
     * 抽检率
     */
    String WORKFLOW_CJL = "cjl";

    /**
     * 勘察确认日期
     */
    String WORKFLOW_KCQRRQ = "kcqrrq";

    /**
     * 不良判断依据
     */
    String WORKFLOW_BLPDYJ = "blpdyj";

    /**
     * 勘察确认详细结果
     */
    String WORKFLOW_KCQRXXJG = "kcqrxxjg";


    /**
     * 勘察确认初步结论
     */
    String WORKFLOW_KCQRCBJ ="kcqrcbj";

    String WORKFLOW_GLZG = "glzg";

    String WORKFLOW_GLZG2 = "glzg2";


    String WORKFLOW_GLZG_MOBILE = "glzgmobile";

    /**
     * 不合格品处理标题
     */
    String WORKFLOW_BHGPCLBT = "bhgpclbt";

    /**
     * 问题描述
     */
    String WORKFLOW_WTMS = "wtms";

    /**
     * 创建人
     */
    String WORKFLOW_CJR = "cjr";

    /**
     * 创建时间
     */
    String WORKFLOW_CJSJ = "cjsj";

    /**
     * 检验单号
     */
    String WORKFLOW_JYDH = "jydh";

    /**
     * 检验类型
     */
    String WORKFLOW_JYLX = "jylx";

    /**
     * 检验完成人员
     */
    String WORKFLOW_JYWCRY = "jywcry";

    /**
     * 检验完成时间
     */
    String WORKFLOW_JYWCSJ = "jywcsj";

    /**
     * 物料名称
     */
    String WORKFLOW_WLMC = "wlmc";

    /**
     * 物料编码
     */
    String WORKFLOW_WLBM = "wlbm";

    /**
     * 生产订单号
     */
    String WORKFLOW_SCDDH = "scddh";

    /**
     * 供应商
     */
    String WORKFLOW_GYS = "gys";

    /**
     * 样本数量
     */
    String WORKFLOW_YBSL = "ybsl";

    /**
     * 合格数量
     */
    String WORKFLOW_HGSL = "hgsl";

    /**
     * 不合格数量
     */
    String WORKFLOW_BHGSL = "bhgsl";

    /**
     * 处理人
     */
    String WORKFLOW_CLR = "clr";

    /**
     * 处理方式
     */
    String WORKFLOW_CLFS = "clfs";

    /**
     * 处理数量
     */
    String WORKFLOW_CLSL = "clsl";

    /**
     * 备注
     */
    String WORKFLOW_BZ = "bz";

    /**
     * 审批人
     */
    String WORKFLOW_SPR = "spr";

    /**
     * 抄送人
     */
    String WORKFLOW_CSR = "csr";

    /**
     * 整改启动审批人
     */
    String WORKFLOW_ZGQDSPR = "zgqdspr";

    /**
     * D1处理人
     */
    String WORKFLOW_D1CLR = "d1clr";

    /**
     * D2处理人
     */
    String WORKFLOW_D2CLR = "d2clr";

    /**
     * d2 结论
     */
    String WORKFLOW_J2 = "j2";

    /**
     * D3处理人
     */
    String WORKFLOW_D3CLR = "d3clr";

    /**
     * d3结论
     */
    String WORKFLOW_J3 = "j3";

    /**
     * D4处理人
     */
    String WORKFLOW_D4CLR = "d4clr";

    /**
     * d4 结论
     */
    String WORKFLOW_J4 = "j4";

    /**
     * d5处理人
     */
    String WORKFLOW_D5CLR = "d5clr";
    /**
     * d5结论
     */
    String WORKFLOW_J5 = "j5";

    /**
     * D6处理人
     */
    String WORKFLOW_D6CLR ="d6clr";


    /**
     * d6结论
     */
    String WORKFLOW_J6 = "j6";

    /**
     * D7处理人
     */
    String WORKFLOW_D7CLR ="d7clr";


    /**
     * d6结论
     */
    String WORKFLOW_J7 = "j7";


    /**
     * D8 审批人
     */
    String WORKFLOW_D8SPR ="d8spr";

    /**
     * 关联不合格品处理
     */
    String WORKFLOW_GLBHGPCL = "glbhgpcl";

    /**
     * 关联不合格品处理-移动
     */
    String WORKFLOW_GLBHGPCL_MOBILE = "glbhgpclmobile";


    /**
     * 关联客诉
     */
    String WORKFLOW_GLKS  ="glks";

    /**
     * 关联客诉-移动
     */
    String WORKFLOW_GLKS_MOBILE  ="glksmobile";



    /**
     * oa不合格品明细表
     */
    String WORKFLOW_TABLE_DEFECT_PRODUCT = "formtable_main_373_dt1";

    /**
     * oa客诉明细表1
     */
    String WORKFLOW_TABLE_CUSTOMER_COMPLAIN_DETAIL1 = "formtable_main_372_dt1";

    /**
     * oa客诉明细表2
     */
    String WORKFLOW_TABLE_CUSTOMER_COMPLAIN_DETAIL2 = "formtable_main_372_dt2";
    /**
     * 数字
     */
    Integer NUM_0 = 0;
    Integer NUM_1 = 1;
    Integer NUM_2 = 2;
    Integer NUM_3 = 3;
    Integer NUM_4 = 4;
    Integer NUM_5 = 5;
    Integer NUM_6 = 6;
    Integer NUM_7 = 7;
    Integer NUM_8 = 8;
    Integer NUM_9 = 9;
    Integer NUM_10 = 10;

    Integer NUM_11 = 11;

    String WHIPPTREE = "-";


    /**
     * 周枚举
     */
    @Getter
    @AllArgsConstructor
    enum WEEKDAY {
        //周枚举 MON星期一 TUE星期二 WED星期三 THU星期四 FRI星期五 STA星期六 SUN星期日
        MON(1, "星期一"), TUE(2, "星期二"), WED(3, "星期三"), THU(4, "星期四"), FRI(5, "星期五"), STA(6, "星期六"), SUN(7, "星期日");

        private Integer day;
        private String dayName;
    }

    /**
     * 车间枚举
     */
    @Getter
    @AllArgsConstructor
    enum WORKSHOP {
        JPCJ1("STEC001001", "基片一车间"), JPCJ2("STEC001002", "基片二车间"), SJGCJ1("STEC002", "深加工一车间"), SJGCJ2("STEC003", "深加工二车间"), SJGCJ3("STEC004", "深加工三车间");

        private String code;
        private String workShopName;
    }


    /**
     * 检验项类型
     */
    final int check_type = 2;
    /**
     * 报表卡片名称长度限制
     */
    final int nameLengthUpLimit = 20;
    final int nameLengthDownLimit = 1;
    final int noteLengthUpLimit = 100;
    final int noteLengthDownLimit = 1;


    /**
     * @desc 检验项 | 缺陷项类别枚举
     */
    @Getter
    @AllArgsConstructor
    enum QtItemCategoryEnum {
        /**
         * 检验项
         */
        CHECK(0, "检验项"),
        /**
         * 缺陷项
         */
        DEFECTIVE(1, "缺陷项");

        private Integer type;
        private String name;

        /**
         * 所有类别
         *
         * @return 一个set集合【底层是hashmap组成，检索效率为o(1)】
         */
        public static Set<Integer> categoryAll() {
            Set<Integer> categorySet = Sets.newHashSet();
            for (QtItemCategoryEnum ele : QtItemCategoryEnum.values()) {
                categorySet.add(ele.getType());
            }
            return categorySet;
        }

        /**
         * 获取类别-可能会返回空
         *
         * @param type 类别id
         * @return 类别元素
         */
        public static QtItemCategoryEnum getEleByType(Integer type) {
            for (QtItemCategoryEnum ele : QtItemCategoryEnum.values()) {
                if (ele.type.equals(type)) {
                    return ele;
                }
            }
            return null;
        }
    }

    /**
     * 检验类型
     */
    @Getter
    @AllArgsConstructor
    enum QtCheckTaskTypeEnum {
        //任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
        INCOMING(1, "来料检"), FIRST(2, "首检"), ONLINE(3, "在线检"), PATROL(4, "巡检"), DELIVER(5, "出货检"), CHEMICAL(6, "化验抽检"), MIXED(7, "配料抽检");

        private Integer code;
        private String name;

        public static QtCheckTaskTypeEnum code(Integer code) {
            for (QtCheckTaskTypeEnum ele : QtCheckTaskTypeEnum.values()) {
                if (ele.getCode().equals(code)) {
                    return ele;
                }
            }
            return null;
        }

    }

    /**
     * 检验类型
     */
    @Getter
    @AllArgsConstructor
    enum QtCheckTaskStatusEnum {
        //任务类型 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
        WAITED(0, "待检验"), CHECKING(1, "检验中"), FINISHED(2, "已检验"), CANCELED(3, "已取消"), TEMPORARY(4, "暂存");

        private Integer code;
        private String name;
    }

    /**
     * 物料状态:0停用,1启用
     */
    @Getter
    @AllArgsConstructor
    enum QtMaterialStatusEnum {
        DISABLE(0, "停用"), ENABLE(1, "启用");

        private Integer code;
        private String name;
    }

    /**
     * 粒度
     */
    @Getter
    @AllArgsConstructor
    enum QtGranularityEnum {
        //粒度 小时 天 周 月 季度 年
        HOUR(0, "小时", "H"), DAY(1, "天", "D"), WEEK(2, "周", "W"), MONTH(3, "月", "M"), QUARTER(4, "季度", "Q"), YEAR(5, "年", "Y"), NONE(6, "无粒度", "N"), CLASS(7, "班次", "C");

        private Integer code;
        private String name;
        private String type;

        public static QtGranularityEnum getByName(String name) {
            for (QtGranularityEnum value : QtGranularityEnum.values()) {
                if (value.getName().equals(name)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * 根据code去查询时间粒度
         *
         * @param code code
         * @return 时间粒度
         */
        public static QtGranularityEnum getByCode(Integer code) {
            for (QtGranularityEnum value : QtGranularityEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 图表类型
     */
    @Getter
    @AllArgsConstructor
    enum QtChatTargetValue {
        //基片成品率 基片缺陷总数 深加工缺陷总数 来料检合格率 基片巡检合格率 深加工巡检合格率
        JPCP(1, "基片成品率"), JPQX(2, "基片缺陷总数"), SJGQX(3, "深加工缺陷总数"), LLJHG(4, "来料检合格率"), JPXJHG(5, "基片巡检合格率"), SJGXJHG(6, "深加工巡检合格率");

        private Integer code;
        private String name;

        public static String getNameByCode(Integer code) {
            for (QtChatTargetValue value : QtChatTargetValue.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }

    /**
     * 对比类型
     */
    @Getter
    @AllArgsConstructor
    enum QtChartCompareEnum {
        //班次 产线 车间 缺陷类别
        ALL(-1, "不对比"), WORK(0, "班次"), PRODUCTLINE(1, "产线"), WORKSHOP(2, "车间"), DEFECTCATEGORY(3, "缺陷类别"), SAMPLINGTIME(4, "取样日期"), MATERIALWORKSHOP(5, "物料使用车间"), SUPPLIER(6, "供应商"), MATERIAL(7, "物料名称");

        private Integer code;
        private String name;

        public static String getNameByCode(Integer code) {
            for (QtChartCompareEnum value : QtChartCompareEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }

    /**
     * 柱状图或饼状图常量
     */
    @Getter
    @AllArgsConstructor
    enum QtChartType {
        HISTOGRAM(0, "柱状图"), PIECHART(1, "饼状图");

        private Integer code;
        private String name;
    }


    /**
     * 白班夜班
     */
    @Getter
    @AllArgsConstructor
    enum QtClassesEnum {
        DAY(0, "早班"), NIGHT(1, "晚班");
        private Integer code;
        private String name;

        /**
         * 根据code筛选白班夜班
         *
         * @param code
         * @return
         */
        public static QtClassesEnum code(Integer code) {
            for (QtClassesEnum ele : QtClassesEnum.values()) {
                if (ele.getCode().equals(code)) {
                    return ele;
                }
            }
            return null;
        }
    }

    /**
     * 样本概览枚举
     */
    @Getter
    @AllArgsConstructor
    enum SAMPLEINFOLIST {
        SAMPLENUM("sampleNum", "样本编号"), LICENSEPLATE("licensePlate", "车牌号"), ANALYST("analyst", "分析人"), ANALYSTNAME("analystName", "分析人名"), SAMPLINGTIME("samplingTime", "取样时间"), ANALYSTIME("analystTime", "分析时间"), INGREDIENTNUM("ingredientNum", "配料付数");

        private String code;
        private String name;
    }

    /**
     * 数据来源 0人工 1采集
     */
    @Getter
    @AllArgsConstructor
    enum QtDefectDataSource {
        MAN(0, "人工检验"), COLLECT(1, "检验设备");
        private Integer code;
        private String name;

        public static QtDefectDataSource code(Integer code) {
            for (QtDefectDataSource ele : QtDefectDataSource.values()) {
                if (ele.getCode().equals(code)) {
                    return ele;
                }
            }
            return null;
        }
    }

    /**
     * 计量单位
     */
    @Getter
    @AllArgsConstructor
    enum QtChartUnit {
        PERCENT(1566986152769929216L, "百分比", "%");

        private Long code;
        private String name;
        private String unitType;
    }

    /**
     * 统计图下载枚举
     */
    @Getter
    @AllArgsConstructor
    enum QtChatDownloadEnum {
        //基片成品率 基片缺陷总数 深加工缺陷总数 来料检合格率 基片巡检合格率 深加工巡检合格率
        SJGQXLB(0, "深加工缺陷类别分布下载"), SJGQX(1, "深加工缺陷分布下载"), JBQXX(2, "基片缺陷项分布下载"), SJGQXLBQS(3, "深加工缺陷类别趋势下载"), SJGQXQS(4, "深加工缺陷项趋势下载"), JBQXXQS(5, "基片缺陷项趋势下载");

        private final Integer code;
        private final String name;

        public static QtChatDownloadEnum getByCode(Integer code) {
            for (QtChatDownloadEnum value : QtChatDownloadEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 检验值
     */
    @Getter
    @AllArgsConstructor
    enum QtSpcFilterConditions {
        UPLIMIT(0, "上限"), DOWNLIMIT(1, "下限"), STANDARDVALUE(2, "标准值"), TESTVALUE(3, "检验值");

        private Integer code;
        private String name;

    }


    @Getter
    @AllArgsConstructor
    enum QtBusinessType {
        CUSTOMER_COMPLAIN(0, "客诉"), UNACCEPTED_PRODUCT(1, "不合格品"), IMPROVE_START(2, "整改启动"),IMPROVE_CLOSEOUT(3,"整改结案");

        private Integer code;
        private String name;
    }


    @Getter
    @AllArgsConstructor
    enum userType {
        APPROVE_MAN(0, "审批人"), CARBON_COPY_MAN(1, "抄送人");

        private Integer code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    enum status {
        PENDING(0, "待处理"), APPROVE(1, "待审批"), CLOSED(2, "已关闭");

        private Integer code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    enum progress {
        CREATE(0, "创建"), DISPOSE(1, "处理"), APPROVAL_RETURN(2, "审批退回"), APPROVE(3, "审批通过"), IMPROVE(4, "关联整改");

        private Integer code;
        private String name;
    }

    /**
     * 整改进度标题
     */
    @Getter
    @AllArgsConstructor
    enum improveProgress {
        CREATE(0, "创建"),
        UNAPPROVED_START(1, "未批准启动"),
        REDACT(2, "编辑"),
        APPROVE_START(3, "批准启动"),
        D1_CREATE(4, "D1:创建团队"),
        D2_CLEAR_PROBLEM(5, "D2:问题明确"),
        RETURN_D2(6, "退回D2"),
        D3_COUNTERMEASURE(7, "D3:临时对策"),
        D4_REASON_ANALYSIS(8, "D4:原因分析"),
        D5_PERMANENT_CORRECTIVE_ACTION_PLAN(9, "D5:永久纠正措施"),
        D6_CORRECTIVE_ACTION_VERIFICATION(10, "D6:纠正措施验证"),
        D7_PREVENT_RECURRENCE(11, "D7:预防再发生"),
        D8_APPROVAL_END_PASS(12, "D8:审批结案通过"),
        RETURN_D3(13, "退回D3"),
        RETURN_D4(14, "退回D4"),
        RETURN_D5(15, "退回D5"),
        RETURN_D6(16, "退回D6"),
        RETURN_D7(17, "退回D7"),
        RETURN_D8(18, "退回D8"),
        RETURN_D1(19, "退回D1"),
        SUSPEND(20, "整改中断");


        private Integer code;
        private String name;
    }

    /**
     * 待批准启动 0、未批准启动 1、D1处理中 2、D2处理中 3、D3处理中 4、D4处理中 5、D5处理中 6、D6处理中 7、D7处理中 8、D8处理中 9、已关闭 10、已中断 11
     */
    @Getter
    @AllArgsConstructor
    enum ImproveProgressStatus {

        D1_CREATE(2, "D1:创建团队"),
        D2_CLEAR_PROBLEM(3, "D2:问题明确"),
        D3_COUNTERMEASURE(4, "D3:临时对策"),
        D4_REASON_ANALYSIS(5, "D4:原因分析"),
        D5_PERMANENT_CORRECTIVE_ACTION_PLAN(6, "D5:永久纠正措施"),
        D6_CORRECTIVE_ACTION_VERIFICATION(7, "D6:纠正措施验证"),
        D7_PREVENT_RECURRENCE(8, "D7:预防再发生"),
        D8_APPROVAL_END_PASS(12, "D8:审批结案通过");
        private Integer code;
        private String name;

        private static HashMap<Integer, ImproveProgressStatus> map;

        static {
            HashMap<Integer, ImproveProgressStatus> hashMap = new HashMap<>(ImproveProgressStatus.values().length);
            for (ImproveProgressStatus value : ImproveProgressStatus.values()) {
                hashMap.put(value.code,value);
            }
            map = hashMap;
        }
        public static ImproveProgressStatus getEnum(Integer code){
            return map.get(code);
        }
    }

    @Getter
    @AllArgsConstructor
    enum DD {
        D2("D2:问题明确", "getD2Processor", "getD2ProcessorName"),
        D3("D3:临时对策", "getD3Processor", "getD3ProcessorName"),
        D4("D4:原因分析", "getD4Processor", "getD4ProcessorName"),
        D5("D5:永久纠正措施", "getD5Processor", "getD5ProcessorName"),
        D6("D6:纠正措施验证", "getD6Processor", "getD6ProcessorName"),
        D7("D7:预防再发生", "getD7Processor", "getD7ProcessorName"),
        D8("D8:审批结案通过", "getD8Processor", "getD8ProcessorName");


        private final String name;
        private final String methodName1;
        private final String methodName2;

        public static void execute(QtImproveRecordDetailDTO detail, QtImproveRecordDisposeDTO dto) {
            for (DD value : DD.values()) {
                try {
                    if (value.name.equals(detail.getApproverStep())) {
                        Method method1 = QtImproveRecordDisposeDTO.class.getMethod(value.getMethodName1(), null);
                        Object invoke1 = method1.invoke(dto);
                        detail.setApproverId((Long) invoke1);
                        Method method2 = QtImproveRecordDisposeDTO.class.getMethod(value.getMethodName2(), null);
                        Object invoke2 = method2.invoke(dto);
                        detail.setApproverName((String) invoke2);
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 样本概览枚举
     */
    @Getter
    @AllArgsConstructor
    enum LossReportType {
        CLASS(0, "班次"), DAY(1, "日"), MONTH(2, "月");

        private Integer code;
        private String name;
    }


}
