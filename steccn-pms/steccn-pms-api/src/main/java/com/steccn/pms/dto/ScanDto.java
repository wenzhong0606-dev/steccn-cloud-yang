package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("领料单扫描dto")
public class ScanDto {
    @ApiModelProperty("类型  0：出库单 1：调拨单")
    @NotNull(message = "类型不能为空")
    private Integer type;


    @ApiModelProperty("扫描明细")
    private List<ScanInfoDto> list;
}
