package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * 领料单分页DTO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@Data
@ApiModel("领料单分页")
public class MaterialRequisitionPageDTO extends BasePageDTO {

    @ApiModelProperty("领料单号")
    private String code;

    @ApiModelProperty("开始时间")
    private LocalDate startTime;

    @ApiModelProperty("结束时间")
    private LocalDate endTime;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("部门id")
    private Long departmentId;
}
