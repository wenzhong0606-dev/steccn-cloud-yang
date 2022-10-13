package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("mess 生成入库凭证")
public class MessInStorageVoucherVO implements Serializable {

    @ApiModelProperty("结果")
    private Boolean result;

    @ApiModelProperty("消息")
    private String message;
}
