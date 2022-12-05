package com.steccn.ems.constant;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

public interface EpManagerConstant {
    //排序字段
    String  EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME = "name";
    String  EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE = "energyTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE="standardTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE ="productTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS  ="productThickName" ;
    String  EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD = "unitValue";
    String  EP_ENERGY_TYPE_SORT_FIELD_INDATE  = "validity";


    /**
     * 产品单耗标准的产品类型
     */
    @Getter
    @AllArgsConstructor
    enum EpProductTypes{
        SUBSTRATE(0L,"基片"),
        DEEP_PROCESSING(1L,"深加工");
        private Long id;
        private String name;
        //根据code查找值
        public static EpProductTypes getProductType(Long id) {
            for (EpProductTypes value : EpProductTypes.values()) {
                if (value.getId().equals(id)){
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 关联图表
     */
    @Getter
    @AllArgsConstructor
    enum EpEnergyTypes {
        ENERGY_PARTITION(0, "能耗分区对比"),
        ENERGY_HISTORY(1, "能耗历史对比"),
        CONSUMPTION_PARTITION(2, "单耗分区对比"),
        CONSUMPTION_SHIFT(3, "单耗历史对比"),
        ENERGY_FLOW(4, "能源流向"),
        CONSUMPTION_HISTORY(5, "单耗班组对比"),
        PEAK_PARTITION(6, "峰谷平分区对比"),
        PEAK_HISTORY(7, "峰谷平历史对比");

        private Integer code;
        private String name;
        //根据code查找值
        public static EpEnergyTypes getByCode(Integer code) {
            for (EpEnergyTypes value : EpEnergyTypes.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        //获取code的集合  新建或编辑时必须在这里面选
        public static Set<Integer> codeSet() {
            Set<Integer> set = Sets.newHashSet();
            for (EpEnergyTypes value : EpEnergyTypes.values()) {
                set.add(value.getCode());
            }
            return set;
        }
    }

    /**
     * 能耗分区
     */
    @Getter
    @AllArgsConstructor
    enum EnergyPartitioningTree {
        POWER_CONSUMPTION_PARTITION(1,"电耗分区"),
        WATER_CONSUMPTION_PARTITION(2,"水耗分区"),
        GAS_CONSUMPTION_PARTITION(3,"气耗分区"),
        FUEL_CONSUMPTION_PARTITION(4,"油耗分区"),
        GAS_FUEL_CONSUMPTION_PARTITION(5,"气耗/油耗分区"),
        ENERGY_PARTITION(6,"能耗分区");


        private Integer energyType;
        private String name;

        //根据energyType查找能耗分区
        public static EnergyPartitioningTree getByEnergyType(Integer energyType) {
            for (EnergyPartitioningTree value : EnergyPartitioningTree.values()) {
                if (value.energyType.equals(energyType)) {
                    return value;
                }
            }
            return null;
        }
    }
}
