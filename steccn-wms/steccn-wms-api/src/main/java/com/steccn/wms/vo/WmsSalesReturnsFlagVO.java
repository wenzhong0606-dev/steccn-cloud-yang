package com.steccn.wms.vo;

import com.steccn.wms.constant.WmsManagerConstant;
import lombok.Data;

/**
 * 销售退货入库标识
 */
@Data
public class WmsSalesReturnsFlagVO {

    /**
     * 物流单是否存在
     */
    private Integer  exists = WmsManagerConstant.YesOrNo.NO.getCode();


    /**
     * 物流单状态为
     * 已退货/已补发？
     */
    private Integer returnedState = WmsManagerConstant.YesOrNo.NO.getCode();

}
