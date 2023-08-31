package com.steccn.pms.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 产线加工信息返回视图
 * @date 2023/8/29
 */
@Data
@ApiModel("产线加工信息返回视图")
public class DashboardProducePageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("当前规格")
    private String specifications;

    @ApiModelProperty("当前批次")
    private String batchNo;

    @ApiModelProperty("批次开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("批次加工总数")
    private BigDecimal completeQuantity;

    @ApiModelProperty("分页列表")
    private Page<DashboardProducePageListVO> pageList;

}
