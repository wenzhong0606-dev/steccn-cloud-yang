package com.steccn.ems.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface EpManagerConstant {
    /**
     * 关联图表
     */
    @Getter
    @AllArgsConstructor
    enum EpEnergyTypes{
        ENERGYPARTITION(0,"能耗分区对比"),
        ENERGYHISTORY(1,"能耗历史对比"),
        CONSUMPTIONPARTITION(2,"单耗分区对比"),
        CONSUMPTIONSHIFT(3,"单耗班组对比"),
        CONSUMPTIONHISTORY(4,"单耗历史对比"),
        PEAKGUPINGPARTITION(5,"峰谷平分区对比"),
        PEAKGUPINGHISTORY(6,"峰谷平历史对比"),
        ENERGYFLOW(7,"能源流向");

        private Integer code;
        private String name;

        public static EpEnergyTypes getById(Integer id){
            for (EpEnergyTypes value : EpEnergyTypes.values()) {
                if (value.getCode().equals(id)){
                    return value;
                }
            }
            return null;
        }

    }
}
