package com.steccn.wms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("物流单DTO")
public class WmsLogisticsOrderDTO {

    @ApiModelProperty("物流单主键")
    private Long id;

    @ApiModelProperty("物流单编码")
    private String code;

    @ApiModelProperty("类型")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "类型不能为空")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("物流方")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物流方不能为空")
    private Long logisticsCompany;

    @ApiModelProperty("物流方名称")
    private String logisticsCompanyName;

    @ApiModelProperty("关联单据")

    private String associatedNo;

    @ApiModelProperty("预计送达时间")
    private LocalDateTime predictTime;

    @ApiModelProperty("实际送达时间")
    private LocalDateTime practicalTime;

    @ApiModelProperty("收货机构")
    @Valid
    private WmsLogisticsInfoDTO receiving;

    @ApiModelProperty("发货机构")
    @Valid
    private WmsLogisticsInfoDTO shipments;

    @ApiModelProperty("备注")
    @Size(min = 0, max = 256, message = "备注长度不能超过 256 个字符")
    private String note;

    @ApiModelProperty("运输方式")
    @Valid
    private List<WmsLogisticsTransportationDTO> wayList;

    @ApiModelProperty("详情")
    @Valid
    private List<WmsLogisticsDetailDTO> list;
}
