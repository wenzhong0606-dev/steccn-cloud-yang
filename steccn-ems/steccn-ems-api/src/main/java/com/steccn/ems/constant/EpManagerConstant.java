package com.steccn.ems.constant;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

public interface EpManagerConstant {
    /**
     * 关联图表
     */
    @Getter
    @AllArgsConstructor
    enum EpEnergyTypes {
        ENERGYPARTITION(0, "能耗分区对比"),
        ENERGYHISTORY(1, "能耗历史对比"),
        CONSUMPTIONPARTITION(2, "单耗分区对比"),
        CONSUMPTIONSHIFT(3, "单耗班组对比"),
        ENERGYFLOW(4, "能源流向"),
        CONSUMPTIONHISTORY(5, "单耗历史对比"),
        PEAKGUPINGPARTITION(6, "峰谷平分区对比"),
        PEAKGUPINGHISTORY(7, "峰谷平历史对比");

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

}
