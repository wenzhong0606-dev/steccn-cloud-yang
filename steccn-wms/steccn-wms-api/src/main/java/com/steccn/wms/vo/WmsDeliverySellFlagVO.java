package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("销售出库标识VO")
public class WmsDeliverySellFlagVO {
    /**
     * 单号是否存在
     */
    private Integer number = 0;

    /**
     * 状态为草稿/已预约/已同步锁/已入场
     */
    private Integer createStateFlag = 0;


    /**
     * 合同未发货>0
     */
    private Integer contractNotDeliveredFlag = 1;



}
