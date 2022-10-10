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

    String QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME = "material_name";

    String QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME = "arrival_time";

    String QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME = "supplier_name";

    String QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME = "material_workShop_name";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER = "product_order";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME = "product_workshop_name";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME = "product_line_name";

    String QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH = "product_batch";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME = "defect_start_time";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE = "original_code";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_ORDER = "deliver_order";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_TIME = "deliver_time";

    String QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME = "customer_name";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_NUM = "deliver_num";

    String QT_CHECK_TASK_SORT_FIELD_DEFECT_RESULT = "result";

    String QT_CHECK_TASK_SORT_FIELD_CREATE_NAME = "create_name";

    String QT_CHECK_TASK_SORT_FIELD_CREATE_TIME = "create_time";



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
}
