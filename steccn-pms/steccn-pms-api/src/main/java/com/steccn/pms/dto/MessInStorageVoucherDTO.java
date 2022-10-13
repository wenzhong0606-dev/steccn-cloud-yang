package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @author yequan
 */
@ApiModel("mess入库 凭证")
@Data
public class MessInStorageVoucherDTO implements Serializable {

    @ApiModelProperty("入库单编码")
    private String code;

    @ApiModelProperty("类型-数据字典code")
    private String type;

    @ApiModelProperty("创建人名称")
    private String createBy;

    @ApiModelProperty("物料信息")
    private List<MessInStorageVoucherListDTO> materialList;

}
