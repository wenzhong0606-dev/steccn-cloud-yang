package com.steccn.pms.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author W10
 * @desc 生产管理的常量接口
 */
public interface PmManagerConstant {


    /**
     * 小计
     */
    String SUBTOTAL = "小计";
    /**
     * 合计
     */
    String TOTAL = "合计";


    /**
     * 排序条件-编码
     */
    String PMS_ORDER_CODE = "code";

    /**
     * 排序条件-批次号
     */
    String PMS_ORDER_BATCH_NO = "batchNo";


    /**
     * 排序条件-箱号
     */
    String PMS_ORDER_CASE_NO = "caseNo";

    /**
     * 排序条件-物料名称
     */
    String PMS_MATERIAL_NAME = "materialName";

    /**
     * 排序条件- 物料编码
     */
    String PMS_MATERIAL_CODE = "materialCode";

    /**
     * 开口泡
     */
    String QMS_TASK_ITEM_SHUT_UP = "开口泡";
    String QMS_TASK_ITEM_SHUT_UP_FIELD = "shutUp";

    /**
     * 气线泡
     */
    String QMS_TASK_ITEM_START_TO_TALK = "气线泡";
    String QMS_TASK_ITEM_START_TO_TALK_FIELD = "startToTalk";

    /**
     * 夹杂结石
     */
    String QMS_TASK_ITEM_STONE = "夹杂结石";
    String QMS_TASK_ITEM_STONE_FIELD = "stone";

    /**
     * 冷裂线/发热变形
     */
    String QMS_TASK_ITEM_VARIANT = "冷裂线/发热变形";
    String QMS_TASK_ITEM_VARIANT_FIELD = "variant";

    /**
     * 辊伤(原片)
     */
    String QMS_TASK_ITEM_FLAW = "总疵点";
    String QMS_TASK_ITEM_FLAW_FIELD = "flaw";

    /**
     * 白班0
     */
    Integer FORE_SHIFT_0 = 0;

    /**
     * 夜班1
     */
    Integer NIGHT_SHIFT_1 = 1;

    /**
     * 白班
     */
    String FORE_SHIFT = "早班";

    /**
     * 夜班
     */
    String NIGHT_SHIFT = "晚班";

    /**
     * 甲班
     */
    String METHYL_CLASS = "车间倒班-甲班";

    /**
     * 甲班
     */
    String METHYL_CLASS_NAME = "甲班";

    /**
     * 乙班
     */
    String ETHYL_CLASS_NAME = "乙班";

    /**
     * 丙班
     */
    String PROPYLENE_CLASS_NAME = "丙班";


    /**
     * 乙班
     */
    String ETHYL_CLASS = "车间倒班-乙班";

    /**
     * 丙班
     */
    String PROPYLENE_CLASS = "车间倒班-丙班";


    /**
     * 测量单位
     */
    String MEASURE_UNIT = "measure_unit";

    /**
     * 单位转换
     */
    String MEASURE_UNIT_PIECE = "片";
    String MEASURE_UNIT_TON = "ton";
    String MEASURE_UNIT_SM = "m²";

    /**
     * 数字
     */
    String NUM_0 = "0";
    String NUM_1 = "1";
    String NUM_2 = "2";
    String NUM_3 = "3";
    String NUM_4 = "4";
    String NUM_5 = "5";
    String NUM_6 = "6";
    String NUM_7 = "7";
    String NUM_8 = "8";
    String NUM_9 = "9";
    String NUM_10 = "10";
    String NUM_11 = "11";
    String NUM_12 = "12";
    String NUM_13 = "13";
    String NUM_14 = "14";
    String NUM_15 = "15";
    String NUM_16 = "16";
    String NUM_17 = "17";
    String NUM_18 = "18";
    String NUM_19 = "19";
    String NUM_20 = "20";
    String NUM_21 = "21";
    String NUM_22 = "22";
    String NUM_23 = "23";
    String NUM_24 = "24";
    String NUM_25 = "25";
    String NUM_26 = "26";
    String NUM_27 = "27";
    String NUM_28 = "28";
    String NUM_29 = "29";
    String NUM_30 = "30";
    String NUM_31 = "31";
    String NUM_100 = "100";
    String NUM_1000 = "1000";

    String RECIPE_STR = "0000";


    String UROPATAGIA = "基片";
    String DEEP_PROCESSING = "深加工";


    /**
     * 下片工序code
     */
    String XPGX = "XPGX";

    /**
     * mrp 类型
     */
    String MRP_TYPE = "pms_mrp_type";

    /**
     * mrp库存
     */
    String MRP_INVENTORY = "mrp_inventory";

    /**
     * mrp 安全库存
     */
    String MRP_SAFETY_INVENTORY = "mrp_safety_inventory";

    /**
     * mrp 计划订单
     */
    String MRP_PLANNED_ORDER = "mrp_planned_order";

    /**
     * 采购申请
     */
    String MRP_PURCHASE_REQUEST = "mrp_purchase_request";

    /**
     * 采购订单
     */
    String MRP_PURCHASE_ORDER = "mrp_purchase_order";

    /**
     * 销售订单
     */
    String MRP_SALES_ORDER = "mrp_sales_order";

    /**
     * 独立需求
     */
    String MRP_INDEPENDENT_DEMAND = "mrp_independent_demand";

    /**
     * 生产需求
     */
    String MRP_MANUFACTURE_REQUIRMENT = "mrp_manufacture_requirment";

    /**
     * 生产订单
     */
    String MRP_PRODUCE_ORDER = "mrp_produce_order";

    /**
     * 班组
     */
    String SYS_TEAM = "sys_team";

    /**
     * 领料类型
     */
    String MATERIAL_REQUISITION = "material_requisition";

    /**
     * 生产领料
     */
    String PRODUCE_PICK = "produce_pick";
    /**
     * 班次
     */
    String SYS_SHIFT = "sys_shift";

    String FILE_EXTENSION_XLSX = ".xlsx";

    /**
     * 工位code
     */
    String STATE_13 = "13";

    /**
     * 产线code
     */
    String STATE_9 = "9";

    /**
     * 车间code
     */
    String STATE_8 = "8";

    /**
     * 单位T
     */
    String UNIT_T = "T";

    /**
     * 单位类型
     */
    String ENERGY_CONSUME = "energy_consume";

    /**
     * 领料单状态
     */
    String MATERIAL_REQUISITION_STATUS = "material_requisition_bill_status";
    /**
     * 已创建
     */
    String MRBS_CREATED = "mrbs_created";

    /**
     * 审批中
     */
    String MRBS_APPROVAL = "mrbs_approval";

    /**
     * 审批通过
     */
    String MRBS_APPROVAL_PASS = "mrbs_approval_pass";

    /**
     * 审批未通过
     */
    String MRBS_APPROVAL_FAIL = "mrbs_approval_fail";

    /**
     * 已删除
     */
    String MRBS_DELETE = "mrbs_delete";

    /**
     * 已完成
     */
    String MRBS_FINISH = "mrbs_finish";

    /**
     * 类型编码-工序类型
     */
    String PROCESS_TYPE = "process_type";

    /**
     * 计划数量
     */
    String PLAN_NUM = "计划数量";

    /**
     * 完工数量
     */
    String FINISH_NUM = "完工数量";

    /**
     * 入库数量
     */
    String RECEIPT_NUM = "入库数量";

    /**
     * 系统版本
     */
    String sysVersion = "2023-01-0000";

    /**
     * 配方超级管理员权限
     */
    String RECIPE_ROLE_SYSTEM = "recipeSystem";

    /**
     * 配方管理员权限
     */
    String RECIPE_ROLE_ADMIN = "recipeAdmin";

    /**
     * 配方管理查看
     */
    String RECIPE_PERMISSION_VIEW = "pms:recipe:view";

    String LABORATORY_DIRECTOR = "化验室副主任";

    String PEILIAO_DUANZHANG = "二线配料副段长";
    /**
     * 副主任id
     */
    Integer DIRECTOR_ID = 151;

    /**
     * 配料副段长id
     */
    Integer DIRECTOR_ID_LINE2 = 414284;

    /**
     * 配料班长
     */
    Integer CLASS_LEADER_PEILIAO = 414286;

    /**
     * 配电工段副段长
     */
    Integer DIRECTOR_ID_PEIDIANFU = 160;

    /**
     * 电器维修工段副段长
     */
    Integer DIRECTOR_ID_DIANQI = 177;

    /**
     * 机修段长
     */
    Integer DIRECTOR_ID_JIXIU = 172;

    /**
     * 机修段长
     */
    Integer DIRECTOR_ID_JIXIU_FU = 178;

    /**
     * 原料车间id
     */
    Integer DEPARTMENT_YUANLIAO_ID = 37;

    /**
     * 原料二车间，配料工段
     */
    Integer DEPARTMENT_YUANLIAO_2_PEILIAO_ID = 166;

    /**
     * 原料二车间，高压电力工段
     */
    Integer DEPARTMENT_GAOYADIANLI = 140;

    /**
     * 电工工段
     */
    Integer DEPARTMENT_DIANGONG = 144;

    /**
     * 机修工段
     */
    Integer DEPARTMENT_JIXIUGONGDUAN = 143;

    /**
     * 流程字段-申请人岗位
     */
    String WORKFLOW_SQRGW = "sqrgw";

    /**
     * 流程字段-申请人
     */
    String WORKFLOW_SQR = "sqr";
    /**
     * 流程字段-申请部门
     */
    String WORKFLOW_SQBM = "sqbm";
    /**
     * 流程字段-申请分部
     */
    String WORKFLOW_SQFB = "sqfb";

    /**
     * 流程字段-申请日期
     */
    String WORKFLOW_SQRQ = "sqrq";
    /**
     * 流程字段-申请编号
     */
    String WORKFLOW_SQBH = "sqbh";

    /**
     * 流程字段-类型
     */
    String WORKFLOW_LX = "lx";

    /**
     * 流程字段-总数量
     */
    String WORKFLOW_ZSL = "zsl";

    /**
     * 流程字段-领料单编码
     */
    String WORKFLOW_LLDBM = "lldbm";


    /**
     * 流程字段-附件
     */
    String WORKFLOW_FJ = "fj";

    /**
     * 详情表名
     */
    String WORKFLOW_REQ_TABLE_NAME = "formtable_main_103_dt1";

    /**
     * 详情表名
     */
    String WORKFLOW_REQ_TABLE_NAME2 = "formtable_main_103_dt2";

    /**
     * 物料编码
     */
    String WORKFLOW_WLBM = "wlbm";

    /**
     * 物料名称
     */
    String WORKFLOW_WLMC = "wlmc";

    /**
     * 规格型号
     */
    String WORKFLOW_GGXH = "ggxh";

    /**
     * 需求时间
     */
    String WORKFLOW_XQSJ = "xqsj";

    /**
     * 需求数量
     */
    String WORKFLOW_XQSL = "xqsl";

    /**
     * 备注
     */
    String WORKFLOW_BZ = "bz";

    /**
     * 领用人
     */
    String WORKFLOW_LYR = "lyr";

    String WORKFLOW_LYYF = "lyyf";

    /**
     * 领用岗位
     */
    String WORKFLOW_LYGW = "lygw";

    /**
     * 成功
     */
    String WORKFLOW_SUCCESS = "SUCCESS";

    /**
     * 配料工id
     */
    String BATCHER_ID = "191";

    /**
     * 电工1
     */
    String DIAN_GONG1 = "173";

    /**
     * 电工2
     */
    String DIAN_GONG2 = "118";

    /**
     * 机修工1
     */
    String JXIU_GONG1 = "175";

    /**
     * 机修工2
     */
    String JXIU_GONG2 = "119";
    /**
     * 甘特图id前缀
     */
    String GANTT_PREFIX = "gstcid-";

    /**
     * 入库出库凭证状态
     */
    @Getter
    @AllArgsConstructor
    enum InVouchEnum {
        CREATE(0, "已创建"), FINISH(1, "已完成"), CHARGE(2, "已冲销");

        private final Integer code;

        private final String name;
    }


    /**
     * 箱状态0:已完工 1:待入库 2：已入库
     */
    @Getter
    @AllArgsConstructor
    enum CaseStateEnum {
        FINISH(0, "已完工"), PREPARE(1, "待入库"), ALREADY(2, "已入库");

        private final Integer code;

        private final String name;
    }

    /**
     * 状态:0 ：已创建  1：已下单  2：执行中 3：暂停中  4：已暂停 5：完成中 6：已完成 7：已关闭 ）
     */
    @Getter
    @AllArgsConstructor
    enum OrderStateEnum {
        ZERO(0, "已创建"), ONE(1, "已下单"), TWO(2, "执行中"), THREE(3, "暂停中"), FOUR(4, "已暂停"), FIVE(5, "完成中"), SIX(6, "已完成"), SEVEN(7, "已关闭");

        private final Integer code;

        private final String name;
    }

    /**
     * 0：出库单 1：领用单
     */

    @Getter
    @AllArgsConstructor
    enum OrderEnum {
        ZERO(0, "出库单"), ONE(1, "领用单");

        private final Integer code;

        private final String name;
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
     * 领料单工作流选项
     */
    @Getter
    @AllArgsConstructor
    enum PmRequisitionWorkFlow {
        PRODUCT(0, "生产领料"), DEPARTMENT(1, "部门领料"), OTHER(2, "其他领料"), FIX(3, "维修领料"), UPKEEP(4, "保养领料"),LABOR(5, "劳保用品领用"),SLABOR(6, "特殊劳保领用");

        private Integer code;
        private String name;

        public static Integer getCodeByName(String name) {
            for (PmRequisitionWorkFlow value : PmRequisitionWorkFlow.values()) {
                if (value.getName().equals(name)) {
                    return value.getCode();
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    enum BadnessType {
        RAW_CLASS("原片不良"), EDGING("磨边不良"), PUNCHING("打孔不良"), SILK_SCREEN("丝印不良"), TEMPERING("钢化不良");

        private String name;

    }


    /**
     * 甲班：A；乙班：B；丙班C
     */
    @Getter
    @AllArgsConstructor
    enum classesType {
        A(1, "早班"),

        B(2, "晚班");

        private Integer code;
        private String name;

        public static Integer getCodeByName(String name) {
            for (classesType value : classesType.values()) {
                if (value.getName().equals(name)) {
                    return value.getCode();
                }
            }
            return null;
        }
    }

    /**
     * 甲班：A；乙班：B；丙班C
     */
    @Getter
    @AllArgsConstructor
    enum classGroupType {

        A("A", "甲班"),

        B("B", "乙班"),

        C("C", "丙班");

        private String code;
        private String name;


        public static String getCodeByName(String name) {
            for (classGroupType value : classGroupType.values()) {
                if (value.getName().equals(name)) {
                    return value.getCode();
                }
            }
            return null;
        }
    }

    /**
     * 深加工产线
     */
    @Getter
    @AllArgsConstructor
    enum DeepProcessingLine {
        ONE("深加工一产线", "1"),
        TWO("深加工二产线", "2"),
        THREE("深加工三产线", "3"),
        FOUR("深加工四产线", "4"),
        FIVE("深加工五产线", "5"),
        SIX("深加工六产线", "6"),
        SEVEN("深加工七产线", "7"),
        EIGHT("深加工八产线", "8");
        private final String name;
        private final String num;

        public static String getNameByNum(String num) {
            for (DeepProcessingLine value : DeepProcessingLine.values()) {
                if (value.getNum().equals(num)) {
                    return value.getName();
                }
            }
            return null;
        }
    }

    enum ChineseNumber {
        ONE("一", 1), TWO("二", 2), THREE("三", 3), FOUR("四", 4), FIVE("五", 5), SIX("六", 6), SEVEN("七", 7), EIGHT("八", 8), NINE("九", 9), TEN("十", 10);

        private final String chinese;
        private final int arabic;

        ChineseNumber(String chinese, int arabic) {
            this.chinese = chinese;
            this.arabic = arabic;
        }

        public int getArabic() {
            return arabic;
        }

        public static int getArabicByChinese(String chinese) {
            for (ChineseNumber number : values()) {
                if (number.chinese.equals(chinese)) {
                    return number.getArabic();
                }
            }
            return 0;
        }
    }

    /**
     * 粒度
     */
    @Getter
    @AllArgsConstructor
    enum QtGranularityEnum {
        //粒度 小时 天 周 月 季度 年
        HOUR(0, "小时", "H"),
        DAY(1, "天", "D"),
        WEEK(2, "周", "W"),
        MONTH(3, "月", "M"),
        QUARTER(4, "季度", "Q"),
        YEAR(5, "年", "Y"),
        NONE(6, "无粒度", "N"),
        CLASS(7, "班次", "C");

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

    }
}
