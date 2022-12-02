package com.steccn.wms.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @desc 仓储管理的常量接口
 */
public interface WmsManagerConstant {


    /**
     * 检验值
     */
    @Getter
    @AllArgsConstructor
    enum CheckScope{
        WAREHOUSE(0,"仓库"),
        SHELVES(1,"货架"),
        MATERIALCLASSIFICATION(2,"物料分类"),
        MATERIAL(3,"物料");

        private Integer code;
        private String name;

    }

}
