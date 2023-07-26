package com.steccn.pms.dto;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 不良日报表行视图对象
 * @date 2023/7/24
 */
@Data
@ApiModel("不良日报表行视图对象")
public class PmsDefectDayOrderBasicDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("生产订单no")
    private String produceOrderCode;

    @ApiModelProperty("客户名称")
    private Integer customerId;

    @ApiModelProperty("物料")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

}
