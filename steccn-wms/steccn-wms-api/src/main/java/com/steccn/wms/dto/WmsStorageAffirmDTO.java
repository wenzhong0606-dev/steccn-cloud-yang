package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel("入库单确认dto")
public class WmsStorageAffirmDTO implements Serializable {

    @ApiModelProperty("入库单id")
    private Long receiptId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String userName;
}
