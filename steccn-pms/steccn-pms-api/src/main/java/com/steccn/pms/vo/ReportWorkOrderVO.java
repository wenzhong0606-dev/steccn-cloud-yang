package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 */
@ApiModel("工单报工订单信息")
@Data
public class ReportWorkOrderVO implements Serializable {

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("生产订单code")
    private String produceOrderCode;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("批次主键")
    private Long batch;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("工序信息Id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("工位")
    private String stationName;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("班次名称")
    private String classesName;

    @ApiModelProperty("班组id")
    private Long classGroupId;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;
}

