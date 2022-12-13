package com.steccn.wms.dto;

import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel
public class WmsDeliveryReceiptDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("仓库Id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("明细集合")
    private List<WmsDeliveryReceiptInfoDTO> list;
}
