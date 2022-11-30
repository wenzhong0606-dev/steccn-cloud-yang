package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.time.LocalDate;

/**
 * 调拨单分页DTO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("调拨单分页DTO")
public class WmsTransferSlipPageDTO extends BasePageDTO {

    @ApiModelProperty("调拨单单号")
    private String code;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("源仓库")
    private Integer sourceWarehouse;

    @ApiModelProperty("源货架")
    private Integer sourceShelf;

    @ApiModelProperty("目标仓库")
    private Integer targetWarehouse;

    @ApiModelProperty("目标货架")
    private Integer targetShelf;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("确认人")
    private Long affirmBy;

    @ApiModelProperty("创建开始日期")
    private LocalDate createStartDate;

    @ApiModelProperty("创建结束日期")
    private LocalDate createEndDate;

    @ApiModelProperty("确认开始日期")
    private LocalDate affirmStartDate;

    @ApiModelProperty("确认结束日期")
    private LocalDate affirmEndDate;


}
