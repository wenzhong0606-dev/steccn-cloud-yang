package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@ApiModel("任务报表vo")
@Data
public class QtCheckTaskReportVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("任务类型 0原始任务  1复检任务")
    private Integer type;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料车间")
    private QtBaseIdNameDTO materialWorkshop;

    @ApiModelProperty("物料车间id")
    private Integer materialWorkshopId;

    @ApiModelProperty("物料车间名字")
    private String materialWorkshopName;

    @ApiModelProperty("生产车间")
    private QtBaseIdNameDTO productWorkshop;

    @ApiModelProperty("生产车间id")
    private Integer productWorkshopId;

    @ApiModelProperty("生产车间名字")
    private String productWorkshopName;

    @ApiModelProperty("产线")
    private QtBaseIdNameDTO productLine;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("生产工单")
    private String productOrder;

    @ApiModelProperty("生产批次")
    private String productBatch;

    @ApiModelProperty("检验方案id")
    private Integer checkPlanId;

    @ApiModelProperty("任务检验")
    private Integer status;

    @ApiModelProperty("检验数量")
    private Integer checkNum;

    @ApiModelProperty("合格数量")
    private Integer goodNum;

    @ApiModelProperty("不合格数量")
    private Integer badNum;

    @ApiModelProperty("客户信息")
    private QtBaseIdNameDTO customer;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户id")
    private Integer customerId;

    @ApiModelProperty("缺陷总数")
    private Integer defectNum;

    @ApiModelProperty("缺陷记录开始时间")
    private LocalDateTime defectStartTime;

    @ApiModelProperty("缺陷记录结束时间")
    private LocalDateTime defectEndTime;

    @ApiModelProperty("任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7")
    private Integer taskType;

    @ApiModelProperty("任务单号")
    private String code;

    @ApiModelProperty("处理把localDateTime转成String")
    private String date;

    @ApiModelProperty("工位id")
    private Long stationId;

    @ApiModelProperty("工位名称")
    private String stationName;

    @ApiModelProperty("班次 早班 0 晚班 1")
    private Integer classesId;

    @ApiModelProperty("班次时间")
    private LocalDateTime classTime;

    @ApiModelProperty("任务完成时间")
    private LocalDateTime finishedTime;

    @ApiModelProperty("取样时间")
    private LocalDateTime samplingTime;

    @ApiModelProperty("供应商名字")
    private String supplierName;

    @ApiModelProperty("供应商id")
    private Integer supplierId;

    @ApiModelProperty("结果判定")
    private String result;

    @ApiModelProperty("合格率")
    private String passRate;

    private static final long serialVersionUID = 1L;
}
