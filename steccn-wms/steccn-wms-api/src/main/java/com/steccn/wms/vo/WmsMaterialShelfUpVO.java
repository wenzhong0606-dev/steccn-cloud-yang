package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.naming.ldap.PagedResultsControl;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/20
 */
@Data
@ApiModel("获取物料的上架策略的仓库货架")
public class WmsMaterialShelfUpVO {

    private Long materialId;

    private Integer warehouseId;

    private String warehouseName;

    private Integer shelfId;

    private String shelfName;
}
