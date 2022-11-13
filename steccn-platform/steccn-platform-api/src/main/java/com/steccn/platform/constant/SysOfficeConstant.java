package com.steccn.platform.constant;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author yequan
 */
@ApiModel("组织机构常量")
public interface SysOfficeConstant {

    /**
     * 仓库货架的类型
     */
    String WAREHOUSE_TYPE = "12";
    String SHELF_TYPE = "5";

    /**
     * 根节点
     */
    Integer ROOT_LEVEL = 1;

    /**
     * 产线类型
     */
    String PRODUCT_LINE="9";

    /**
     * 车间类型
     */
    String WORKSHOP = "8";
}
