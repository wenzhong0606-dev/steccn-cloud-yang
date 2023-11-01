package com.steccn.wms.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
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
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("类型")
//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "类型id不能为空")
    private Long type;

    @ApiModelProperty("仓库Id")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "仓库id不能为空")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("备注")
    @Size(min = 0, max = 256, message = "备注长度不能超过 256 个字符")
    private String note;

    @ApiModelProperty("明细集合")
    private List<WmsDeliveryReceiptInfoDTO> list;

    @ApiModelProperty("冲销入库单code")
    private String chargeAgainstCode;

    @ApiModelProperty("出库单关联主键")
    private Long linkedId;


    /**
     * 是否自动确认 0：否 1：是
     */
    private String affirmFlag;

    private Long createBy;

    private String createName;

    /**
     * 物流单号
     */
    private String logisticsNumber;

}
