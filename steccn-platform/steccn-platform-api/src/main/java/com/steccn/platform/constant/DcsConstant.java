package com.steccn.platform.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 青州中联DCS常量
 *
 * @author AndrewLuo
 * @date 2025/08/28
 */
public interface DcsConstant {
    /**
     * 青州DCS画面
     */
    @Getter
    @AllArgsConstructor
    enum  DCS1{
        SYSTEM1(1, "系统总貌","d21didc91a2oav9cn72g"),
        PSA1(2, "PSA实验装置","d0g36kk91a2oav9cn0m0"),
        REFRIGERATION(3, "制冷系统","cr2npu491a2t9h1r83tg"),
        SYSTEM2(4, "系统总貌","cp1d57491a2t9h1r82kg"),
        WATER(5, "循环水系统","cp0mjbk91a2t9h1r82h0"),
        VACUUM(6, "真空系统","cos7j6k91a2t9h1r82d0"),
        VOLTAGE(7, "变压吸附","coinu5491a2t9h1r81r0"),
        COMPRESS(8, "压缩系统","cog8k3c91a2t9h1r81eg"),
        ADSORB(9, "吸附精制系统","coe9g8s91a2t9h1r8090"),
        CHEMICAL_WATER(10, "化水系统","coe7kks91a2t9h1r801g"),
        ELECTRIC(11, "电气系统","cocsrvc91a2t9h1r7vkg"),
        LIQUID(12, "液化精馏系统","coc96ck91a2t9h1r7vig"),
        BOILER(13, "锅炉工艺","clvti3pmn3dmns1fb49g"),
        ELECTROLYSIS(14,"电催化装置","d2q0sg491a2oav9cn7gg"),
        ELECTROLYSIS2(15,"电催化装置","d6t4j4491a2oav9cnmkg");;


        private Integer id;
        private String name;
        private String pageId;

        public static DCS1 getById(Integer id) {
            for (DCS1 dcs : DCS1.values()) {
                if (dcs.getId().equals(id)) {
                    return dcs;
                }
            }
            return null; // 或者抛异常，根据需要
        }
    }
}
