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
    /**
     * 动态时间
     */
    String DYNAMIC_TIME_TODAY = "今天";

    String DYNAMIC_TIME_TOMORROW = "昨天";

    String DYNAMIC_TIME_THIS_WEEK = "本周";

    String DYNAMIC_TIME_LAST_WEEK = "上周";

    String DYNAMIC_TIME_THIS_MONTH = "本月";

    String DYNAMIC_TIME_LAST_MONTH = "上月";

    String DYNAMIC_TIME_THIS_QUARTER = "本季度";

    String DYNAMIC_TIME_LAST_QUARTER = "上季度";

    String DYNAMIC_TIME_THIS_YEAR = "今年";

    String DYNAMIC_TIME_LAST_YEAR = "去年";

    String DYNAMIC_TIME_PAST_SEVEN_DAYS = "过去7天";

    String DYNAMIC_TIME_PAST_FOURTEEN_DAYS = "过去14天";

    String DYNAMIC_TIME_PAST_THIRTY_DAYS = "过去30天";

    String DYNAMIC_TIME_PAST_NINETY_DAYS = "过去90天";

    String DYNAMIC_TIME_PAST_ONE_HUNDRED_EIGHTY_DAYS = "过去180天";

    String DYNAMIC_TIME_LAST_THREE_HUNDRED_SIXTY_FIVE_DAYS = "过去365天";

    String QT_CHART_EXCEL_MODEL_URL = "http://61.177.139.244/images/";

    String QT_CHART_EXCEL_MODEL_NAME = "JPCP.xlsx";

    String QT_CHART_EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    String QT_CHART_EXCEL_ENCODE = "UTF-8";

    String QT_CHART_JPCP_NAME = "基片成品趋势";

    /**
     * beforeHour 代表早上8点
     * afterHour  代表晚上20点
     */
    final int beforeHour = 8;
    final int afterHour = 20;
    final long hourSpace = 2;

    /**
     * 班次 0早班 1晚班
     */
    final int morningWork = 0;
    final int nightWork = 1;

    /**
     * 日环比,周环比标志
     */
    Boolean DAY_FLAG = false;
    Boolean WEEK_FLAG = false;

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
    enum QtSPCGranularityEnum{
        //粒度 小时 天 周 月 季度 年
        HOUR(0,"小时"),
        DAY(1,"天"),
        WEEK(2,"周"),
        MONTH(3,"月"),
        QUARTER(4,"季度"),
        YEAR(5,"年");

        private Integer code;
        private String name;
    }

    /**
     * 粒度
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
}
