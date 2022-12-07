package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单DTO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("调拨单DTO")
public class WmsTransferSlipDTO extends BasePageDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("源仓库")
    private Integer sourceWarehouse;

    @ApiModelProperty("目标仓库")
    private Integer targetWarehouse;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("详情")
    private List<WmsTransferSlipInfoDTO> list;

}
