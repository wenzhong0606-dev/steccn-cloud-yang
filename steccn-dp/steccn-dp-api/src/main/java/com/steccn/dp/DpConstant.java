package com.steccn.dp;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.steccn.dp.DpConstant.BusiMonitorCategoryEnum.*;

/**
 * dp常量
 *
 * @author AndrewLuo
 * @date 2023/12/21
 */
public interface DpConstant {
    String DP_WEIGHT_GUISHA_1_TAGID = "Raw_YL.CS.ZHM.fact_weigh1_FLOAT";
    String DP_WEIGHT_GUISHA_2_TAGID = "Raw_YL.CS.ZHM.fact_weigh2_FLOAT";
    String DP_WEIGHT_BAIYUNSHI_TAGID = "Raw_YL.CS.ZHM.fact_weigh3_FLOAT";
    String DP_WEIGHT_CHUNJIAN_TAGID = "Raw_YL.CS.ZHM.fact_weigh4_FLOAT";
    String DP_WEIGHT_SHIHUI_TAGID = "Raw_YL.CS.ZHM.fact_weigh5_FLOAT";
    String DP_WEIGHT_FUHE_TAGID = "Raw_YL.CS.ZHM.fact_weigh6_FLOAT";
    String DP_WEIGHT_LVFEN_TAGID = "Raw_YL.CS.ZHM.fact_weigh7_FLOAT";
    String DP_WEIGHT_XIAOLIAO_TAGID = "Raw_YL.CS.ZHM.fact_weigh8_FLOAT";
    String DP_WEIGHT_GLASS_1_TAGID = "Raw_YL.CS.ZHM.fact_weigh9_FLOAT";
    String DP_WEIGHT_GLASS_2_TAGID = "Raw_YL.CS.ZHM.fact_weigh10_FLOAT";

    @Getter
    @AllArgsConstructor
    enum BusiMonitorCategoryEnum {
        RAW_MATERIAL(0, "原料配料"),
        SUBSTRATE(1, "基片生产"),
        DEEP_PROCESS(2, "深加工生产"),
        SALE(3, "销售"),
        WAREHOUSE(4, "仓储"),
        QUALITY(5, "质量"),
        ENERGY(6, "能源"),
        DEVICE(7, "设备");
        private Integer code;
        private String name;

        public static BusiMonitorCategoryEnum getByCode(Integer code) {
            for (BusiMonitorCategoryEnum value : BusiMonitorCategoryEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    enum BusiMonitorAlarmEnum {
        GS_WATER("硅砂含水率告警", RAW_MATERIAL),
        PHL_WATER("配合料含水率告警", RAW_MATERIAL),
        JP_RH_RATE("融化率告警", SUBSTRATE),
        JP_PRODUCT_PASS_RATE("产量合格率告警", SUBSTRATE),
        JP_IN_STORAGE_RATE("入库合格率告警", SUBSTRATE),
        DEEP_PRODUCT_PASS_RATE("产量合格率告警", DEEP_PROCESS),
        DEEP_IN_STORAGE_PASS_RATE("入库合格率告警", DEEP_PROCESS),
        DEEP_REPORT_WORK_FINISH_NUM("报工完成量告警", DEEP_PROCESS),
        DELIVERY_OVERDUE("发货逾期告警", SALE),
        SAFETY_STOCK("安全库存告警", WAREHOUSE),
        SPC_CONTROL("SPC控制告警", QUALITY),
        UNIT_CONSUME_ALARM("单耗告警", QUALITY),
        ENERGY_ALARM("能耗告警", QUALITY),
        SPOT_CHECK_RATE("点检完成率告警", DEVICE),
        ;
        private String name;
        private BusiMonitorCategoryEnum category;

        public static BusiMonitorAlarmEnum getByCode(String code) {
            for (BusiMonitorAlarmEnum value : BusiMonitorAlarmEnum.values()) {
                if (value.toString().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        public static List<BusiMonitorAlarmEnum> getByCategory(BusiMonitorCategoryEnum categoryEnum) {
            List<BusiMonitorAlarmEnum> resList = Lists.newArrayList();
            for (BusiMonitorAlarmEnum value : BusiMonitorAlarmEnum.values()) {
                if (value.getCategory().equals(categoryEnum)) {
                    resList.add(value);
                }
            }
            return resList;
        }
    }


}
