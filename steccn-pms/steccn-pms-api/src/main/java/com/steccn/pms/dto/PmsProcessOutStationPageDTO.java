package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/11
 */

@Data
@ApiModel("工序出站信息DTO")
public class PmsProcessOutStationPageDTO extends BasePageDTO {


    @ApiModelProperty("工单id")
    @NotNull(message = "工单id不能为空")
    private Long produceOrderId;

    @ApiModelProperty("工序ID")
    private Long processId;

    @ApiModelProperty("工位id集合")
    @NotNull(message = "工位不能为空")
    private List<Integer> stationIds;

    @ApiModelProperty("批次号集合")
    @NotNull(message = "批次不能为空")
    private List<String> batchNos;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDate startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDate endTime;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("工位id")
    private Integer stationId;
}
