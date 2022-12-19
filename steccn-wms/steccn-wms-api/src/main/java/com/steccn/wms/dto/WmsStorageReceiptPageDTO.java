package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@Data
@ApiModel("入库凭证分页DTO")
public class WmsStorageReceiptPageDTO extends BasePageDTO {

    @ApiModelProperty("入库单")
    private String code;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("仓库")
    private List<Long> warehouseIds;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("货架编码")
    private String shelfCode;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("确认人")
    private String affirmName;

    @ApiModelProperty("创建开始日期")
    private LocalDate createStartDate;

    @ApiModelProperty("创建结束日期")
    private LocalDate createEndDate;

    @ApiModelProperty("创建开始时间")
    private LocalDateTime createStartTime;

    @ApiModelProperty("创建结束时间")
    private LocalDateTime createEndTime;

    @ApiModelProperty("确认开始日期")
    private LocalDate affirmStartDate;

    @ApiModelProperty("确认结束日期")
    private LocalDate affirmEndDate;

    @ApiModelProperty("确认开始时间")
    private LocalDateTime affirmStartTime;

    @ApiModelProperty("确认结束时间")
    private LocalDateTime affirmEndTime;
}
