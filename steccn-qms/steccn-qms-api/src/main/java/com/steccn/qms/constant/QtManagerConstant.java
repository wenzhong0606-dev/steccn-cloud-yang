package com.steccn.qms.constant;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
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

    String QT_CHECK_TASK_FIELD_CHECK_RESULT = "checkResult";

    String QT_CHECK_TASK_FIELD_CHECK_DEFECT = "defectId";

    String QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME ="finishedTime";

    String QT_CHECK_TASK_COPY__SORT_FIELD="samplingTime";

    String QT_CHECK_TASK_SORT_FIELD_CLASS_ID="classId";

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

    String QT_CHART_SPC_REPORT_NAME="报表卡片名称";

    String QT_CHART_DEEP_PROCESS_NAME = "深加工巡检合格率趋势";

    String QT_CHART_SUB_PROCESS_NAME ="基片巡检合格率";

    String QT_CHART_ACCESSORY_PROCESS_NAME ="配料抽检合格率";

    String QT_CHART_DEFECTIVE_CATEGORY_NAME ="深加工缺陷类别分布";

    String QT_CHART_DEFECTIVE_ITEM_NAME ="深加工缺陷项分布";

    String QT_CHART_SUBSTRATE_ITEM_NAME ="基片缺陷项分布";

    String QT_CHART_DEFECTIVE_CATEGORY_LINE_NAME = "深加工缺陷类别趋势";

    String QT_CHART_DEEP_ITEM_LINE_NAME = "深加工缺陷项趋势";

    String QT_CHART_SUBSTRATE_ITEM_LINE_NAME = "基片缺陷项趋势";

    String QT_CHART_SUB_JPCJ ="基片化验抽检合格率趋势";

    String QT_CHART_ACCEPTABILITY_LLJ ="来料检合格率";

    String QT_CHART_DEFECT_JPFB = "DEFECTJPFB.xlsx";

    String QT_CHART_DEFECT_JPFB_NAME = "基片缺陷分布";

    String QT_CHART_EXCEL_MODEL_NAME_JPXJHGLQS= "JPXJHGLQS.xlsx"; //基片巡检合格率

    //质量管控模块样本概况字段常量
    String SAMPLENUM = "sampleNum"; //样本编号
    String LICENSEPLATE = "licensePlate"; //车牌号
    String ANALYST = "analyst"; //分析人
    String ANALYSTNAME = "analystName"; //分析人名
    String SAMPLINGTIME = "samplingTime"; //取样时间
    String ANALYSTIME = "analystTime"; //分析时间
    String INGREDIENTNUM = "ingredientNum"; //配料付数


    /**
     * 机构类型为车间的编码
     */
    String OFFICE_WORKSHOP_TYPE ="8";
    String OFFICE_PRODUCE_LINE_TYPE ="9";


    /**
     * 车间名称
     */
    String DEEP_OFFICE_NAME = "深加工";
    String DEEP_OFFICE_NAME_ONE = "深加工一车间";
    String DEEP_OFFICE_NAME_TWO = "深加工二车间";
    String DEEP_OFFICE_NAME_THREE = "深加工三车间";

    String SUB_OFFICE_NAME="基片";
    String SUB_OFFICE_NAME_ONE = "基片一车间";
    String SUB_OFFICE_NAME_TWO = "基片二车间";

    /**
     * 产线名称
     */
    String DEEP_OFFICE_NAME_ONE_PRODUCT_LINE_ONE = "深加工一车间一产线";
    String DEEP_OFFICE_NAME_ONE_PRODUCT_LINE_TWO = "深加工一车间二产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_ONE = "深加工二车间一产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_TWO = "深加工二车间二产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_THREE = "深加工二车间三产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_FOUR = "深加工二车间四产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_FIVE = "深加工二车间五产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_SIX = "深加工二车间六产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_SEVEN = "深加工二车间七产线";
    String DEEP_OFFICE_NAME_TWO_PRODUCT_LINE_EIGHT = "深加工二车间八产线";
    String DEEP_OFFICE_NAME_THREE_PRODUCT_LINE_ONE = "深加工三车间一产线";

    String SUB_OFFICE_NAME_ONE_PRODUCT_LINE_ONE = "基片一车间一产线";
    String SUB_OFFICE_NAME_ONE_PRODUCT_LINE_TWO = "基片一车间二产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_ONE = "基片二车间一产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_TWO = "基片二车间二产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_THREE = "基片二车间三产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_FOUR = "基片二车间四产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_FIVE = "基片二车间五产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_SIX = "基片二车间六产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_SEVEN = "基片二车间七产线";
    String SUB_OFFICE_NAME_TWO_PRODUCT_LINE_EIGHT = "基片二车间八产线";


    /**
     * 计算结果为"0"
     */
    String THE_CALCULATION_RESULTS = "0";

    /**
     * 班次
     */
    Integer DAY_SHIFT = 0;
    Integer NIGHT_SHIFT =1;


    /**
     * beforeHour 代表早上8点
     * afterHour  代表晚上20点
     */
    final int beforeHour = 8;
    final int afterHour = 20;
    final long hourSpace = 2;


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
     * 周枚举
     */
    @Getter
    @AllArgsConstructor
    enum WEEKDAY{
        //周枚举 MON星期一 TUE星期二 WED星期三 THU星期四 FRI星期五 STA星期六 SUN星期日
        MON(1, "星期一"),
        TUE(2,"星期二"),
        WED(3,"星期三"),
        THU(4,"星期四"),
        FRI(5,"星期五"),
        STA(6,"星期六"),
        SUN(7,"星期日");

        private Integer day;
        private String dayName;
    }

        /**
         * 车间枚举
         */
        @Getter
        @AllArgsConstructor
        enum WORKSHOP {
            JPCJ1("STEC001001", "基片一车间"),
            JPCJ2("STEC001002", "基片二车间"),
            SJGCJ1("STEC002", "深加工一车间"),
            SJGCJ2("STEC003", "深加工二车间"),
            SJGCJ3("STEC004", "深加工三车间");

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
    enum QtCheckTaskTypeEnum{
        //任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
        INCOMING(1, "来料检"),
        FIRST(2,"首检"),
        ONLINE(3,"在线检"),
        PATROL(4,"巡检"),
        DELIVER(5,"出货检"),
        CHEMICAL(6,"化验抽检"),
        MIXED(7,"配料抽检");

        private Integer code;
        private String name;
    }

    /**
     * 检验类型
     */
    @Getter
    @AllArgsConstructor
    enum QtCheckTaskStatusEnum{
        //任务类型 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
        WAITED(0,"待检验"),
        CHECKING(1,"检验中"),
        FINISHED(2,"已检验"),
        CANCELED(3,"已取消"),
        TEMPORARY(4,"暂存");

        private Integer code;
        private String name;
    }

    /**
     * 物料状态:0停用,1启用
     */
    @Getter
    @AllArgsConstructor
    enum QtMaterialStatusEnum{
        DISABLE(0,"停用"),
        ENABLE(1,"启用");

        private Integer code;
        private String name;
    }

    /**
     * 粒度
     */
    @Getter
    @AllArgsConstructor
    enum QtGranularityEnum{
        //粒度 小时 天 周 月 季度 年
        HOUR(0,"小时","H"),
        DAY(1,"天","D"),
        WEEK(2,"周","W"),
        MONTH(3,"月","M"),
        QUARTER(4,"季度","Q"),
        YEAR(5,"年","Y");

        private Integer code;
        private String name;
        private String type;

        public static QtGranularityEnum getByName(String name){
            for (QtGranularityEnum value : QtGranularityEnum.values()) {
                if(value.getName().equals(name)){
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
    enum QtChatTargetValue{
        //基片成品率 基片缺陷总数 深加工缺陷总数 来料检合格率 基片巡检合格率 深加工巡检合格率
        JPCP(1,"基片成品率"),
        JPQX(2,"基片缺陷总数"),
        SJGQX(3,"深加工缺陷总数"),
        LLJHG(4,"来料检合格率"),
        JPXJHG(5,"基片巡检合格率"),
        SJGXJHG(6,"深加工巡检合格率");

        private Integer code;
        private String name;
    }

    /**
     * 对比类型
     */
    @Getter
    @AllArgsConstructor
    enum QtChartCompareEnum{
        //班次 产线 车间 缺陷类别
        ALL(-1,"不对比"),
        WORK(0,"班次"),
        PRODUCTLINE(1,"产线"),
        WORKSHOP(2,"车间"),
        DEFECTCATEGORY(3,"缺陷类别"),
        SAMPLINGTIME(4,"取样日期"),
        MATERIALWORKSHOP(5,"物料使用车间"),
        SUPPLIER(6,"供应商"),
        MATERIAL(7,"物料名称");

        private Integer code;
        private String name;

        public static String getNameByCode(Integer code){
            for (QtChartCompareEnum value : QtChartCompareEnum.values()) {
                if(value.getCode().equals(code)){
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
    enum QtChartType{
        HISTOGRAM(0,"柱状图"),
        PIECHART(1,"饼状图");

        private Integer code;
        private String name;
    }


    /**
     * 白班夜班
     */
    @Getter
    @AllArgsConstructor
    enum QtClassesEnum {
        DAY(0, "早班"),
        NIGHT(1, "晚班");
        private Integer code;
        private String name;

        /**
         * 根据code筛选白班夜班
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
        enum SAMPLEINFOLIST{
            SAMPLENUM("sampleNum","样本编号"),
            LICENSEPLATE("licensePlate","车牌号"),
            ANALYST("analyst","分析人"),
            ANALYSTNAME("analystName","分析人名"),
            SAMPLINGTIME("samplingTime","取样时间"),
            ANALYSTIME("analystTime","分析时间"),
            INGREDIENTNUM("ingredientNum","配料付数");

            private String code;
            private String name;
        }

    /**
     * 计量单位
     */
    @Getter
    @AllArgsConstructor
    enum QtChartUnit{
        PERCENT(1566986152769929216L,"百分比","%");

        private Long code;
        private String name;
        private String unitType;
    }

    /**
     * 统计图下载枚举
     */
    @Getter
    @AllArgsConstructor
    enum QtChatDownloadEnum{
        //基片成品率 基片缺陷总数 深加工缺陷总数 来料检合格率 基片巡检合格率 深加工巡检合格率
        SJGQXLB(0,"深加工缺陷类别分布下载"),
        SJGQX(1,"深加工缺陷分布下载"),
        JBQXX(2,"基片缺陷项分布下载"),
        SJGQXLBQS(3,"深加工缺陷类别趋势下载"),
        SJGQXQS(4,"深加工缺陷项趋势下载"),
        JBQXXQS(5,"基片缺陷项趋势下载");

        private final Integer code;
        private final String name;

        public static QtChatDownloadEnum getByCode(Integer code){
            for (QtChatDownloadEnum value : QtChatDownloadEnum.values()) {
                if(value.getCode().equals(code)){
                    return value;
                }
            }
            return null;
        }
    }


}
