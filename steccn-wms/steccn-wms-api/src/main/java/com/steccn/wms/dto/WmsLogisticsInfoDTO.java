package com.steccn.wms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 收发货信息VO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("收发货信息VO")
public class WmsLogisticsInfoDTO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("收发货类型（0：收 1：发）")
    private Integer type;

    @ApiModelProperty("机构id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "机构不能为空")
    private Integer officeId;

    @ApiModelProperty("机构名称")
    private String officeName;

    @ApiModelProperty("地址")
    @Size(min = 0, max = 256, message = "收货地址长度不能超过256个字符")
    private String site;

    @ApiModelProperty("联系人")
    @Size(min = 0, max = 64, message = "联系人长度不能超过64个字符")
    private String linkman;

    @ApiModelProperty("联系方式")
    @Size(min = 0, max = 11, message = "联系方式长度不能超过11个字符")
    private String contactWay;

}
