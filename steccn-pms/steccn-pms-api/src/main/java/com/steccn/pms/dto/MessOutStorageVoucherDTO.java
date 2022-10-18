package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yequan
 */
@Data
@ApiModel("出库单messDTO")
public class MessOutStorageVoucherDTO {

    @ApiModelProperty("出库单编码")
    private String code;

    @ApiModelProperty("类型-数据字典code")
    private String type;

    @ApiModelProperty("创建人名称")
    private String createBy;

    @ApiModelProperty("物料信息")
    private List<MessInStorageVoucherListDTO> materialList;
}
