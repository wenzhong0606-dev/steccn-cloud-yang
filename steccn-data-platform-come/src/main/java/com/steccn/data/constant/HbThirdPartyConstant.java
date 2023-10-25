package com.steccn.data.constant;

import com.google.common.collect.Lists;
import com.steccn.oa.constant.OaManagerConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @desc 湖北博华采样数据对接
 * @author yequan
 */
public interface HbThirdPartyConstant {

    /**
     * 请求头
     */
    String STR_CLIENT_ID = "client-id";

    public static final List<String> SIGNAL_CODE_LIST = Lists.newArrayList("TX_DEEP01_AE.Alert", "TX_DEEP01_LH_AE.Alert", "TX_DEEP02_AE.Alert", "TX_DEEP02_LH_AE.Alert" , "TX_DEEP03_AE.Alert" , "TX_DEEP03_LH_AE.Alert" , "TX_DEEP04_AE.Alert" , "TX_DEEP04_LH_AE.Alert" , "TX_DEEP05_AE.Alert" , "TX_DEEP05_LH_AE.Alert" , "TX_DEEP06_AE.Alert" , "TX_DEEP06_LH_AE.Alert" , "TX_DEEP07_AE.Alert" , "TX_DEEP07_LH_AE.Alert" , "TX_DEEP08_AE.Alert" , "TX_DEEP08_LH_AE.Alert");

    @Getter
    @AllArgsConstructor
    enum SIGNAL_CODE_MAPPING {
        //编码 名称
        A("TX_DEEP01_AE.Alert","深加工1线炉前断线"),
        B("TX_DEEP01_LH_AE.Alert","深加工1线炉后断线"),
        C("TX_DEEP02_AE.Alert", "深加工2线炉前断线"),
        D("TX_DEEP02_LH_AE.Alert","深加工2线炉后断线"),
        E("TX_DEEP03_AE.Alert","深加工3线炉前断线"),
        F("TX_DEEP03_LH_AE.Alert","深加工3线炉后断线"),
        G("TX_DEEP04_AE.Alert","深加工4线炉前断线"),
        H("TX_DEEP04_LH_AE.Alert","深加工4线炉后断线"),
        I("TX_DEEP05_AE.Alert","深加工5线炉前断线"),
        J("TX_DEEP05_LH_AE.Alert","深加工5线炉后断线"),
        K("TX_DEEP06_AE.Alert","深加工6线炉前断线"),
        L("TX_DEEP06_LH_AE.Alert","深加工6线炉后断线"),
        M("TX_DEEP07_AE.Alert","深加工7线炉前断线"),
        N("TX_DEEP07_LH_AE.Alert","深加工7线炉后断线"),
        O("TX_DEEP08_AE.Alert","深加工8线炉前断线"),
        P("TX_DEEP08_LH_AE.Alert","深加工8线炉后断线");

        private String code;
        private String name;

        public static String getNameByCode(String code) {
            for (HbThirdPartyConstant.SIGNAL_CODE_MAPPING value : HbThirdPartyConstant.SIGNAL_CODE_MAPPING.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }

    }

    /**
     * 值班主任
     */
    String SHIFT_SUPERVISOR_1 = "79";
    String SHIFT_SUPERVISOR_2 = "414255";
    String SHIFT_SUPERVISOR_3 = "414256";
    String SHIFT_SUPERVISOR_4 = "414259";
    String SHIFT_SUPERVISOR_5 = "414260";
}
