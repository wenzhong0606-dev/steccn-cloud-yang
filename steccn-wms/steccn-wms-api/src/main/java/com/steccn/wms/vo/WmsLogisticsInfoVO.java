package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 收发货信息VO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("收发货信息VO")
public class WmsLogisticsInfoVO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("收发货类型（0：收 1：发）")
    private Integer type;

    @ApiModelProperty("机构id")
    private Integer officeId;

    @ApiModelProperty("机构名称")
    private String officeName;

    @ApiModelProperty("地址")
    private String site;

    @ApiModelProperty("联系人")
    private String linkman;

    @ApiModelProperty("联系方式")
    private String contactWay;

}
