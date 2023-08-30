package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 产线加工信息dto
 * @date 2023/8/29
 */
@Data
@ApiModel("产线加工信息dto")
public class DashboardProducePageDTO extends BasePageDTO {

    @ApiModelProperty("产线主键")
    private Integer productLineId;

    @ApiModelProperty("日期")
    private LocalDate date;

    @ApiModelProperty("状态（0 ：已创建  1：已下单  2：执行中 3：暂停中  4：已暂停 5：完成中 6：已完成 7：已关闭 ）")
    private Integer state;
}
