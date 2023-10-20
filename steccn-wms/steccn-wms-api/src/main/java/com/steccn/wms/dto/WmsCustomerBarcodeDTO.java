package com.steccn.wms.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("客户条码绑定DTO")
public class WmsCustomerBarcodeDTO {


    /**
     * 箱号
     */
    @ApiModelProperty("箱号")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "箱号不能为空")
    private String caseNo;

    /**
     * 客户条码
     */
    @ApiModelProperty("客户条码")
    private String customerBarcode;
}
