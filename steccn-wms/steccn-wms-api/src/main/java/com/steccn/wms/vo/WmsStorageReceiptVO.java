package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@Data
@ApiModel("入库凭证VO")
public class WmsStorageReceiptVO {
    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("仓库Id")
    private Integer warehouseId;

    @ApiModelProperty("仓库编码")
    private String warehouseCode;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("货架编码")
    private String shelfCode;

    @ApiModelProperty("确认人")
    private String affirmName;

    @ApiModelProperty("确认时间")
    private LocalDateTime affirmTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("创建名称")
    private String createName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("入库时间")
    private LocalDateTime comeTime;

    @ApiModelProperty("明细集合")
    private List<WmsStorageReceiptInfoVO> list;
}
