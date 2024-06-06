package com.steccn.wms.vo;


import com.steccn.pms.vo.PassRateDateVo;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiOperation("二维码获取标识信息")
public class WmsCodeInfoVO {


    private String materialCode;

    private String materialName;

    private String warehouseCode;

    private String warehouseName;

    private BigDecimal totalQuantity;


    private BigDecimal quantity;

    /**
     * 物料id
     */
    private Long materialId;


    /**
     * 数量单位
     */
    private Long unitId;


    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架Id
     */
    private Integer shelfId;

    private String shelfName;


    /**
     * 批次号
     */
    private String batchNo;

    private String caseNo;

}
