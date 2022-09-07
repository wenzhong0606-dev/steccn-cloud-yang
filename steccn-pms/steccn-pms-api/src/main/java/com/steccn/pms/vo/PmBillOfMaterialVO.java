package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("物料清单VO")
public class PmBillOfMaterialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("物料ID")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty
    private String materialCode;

    @ApiModelProperty("状态： 1有效 2无效")
    private Integer status;

    @ApiModelProperty("标准数量")
    private Object quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unit;

    @ApiModelProperty("版本")
    private String version;

    @ApiModelProperty("是否当前版本 1是0否")
    private Integer isCurrentVersion;

    @ApiModelProperty("有效开始时间")
    private LocalDateTime validBeginTime;

    @ApiModelProperty("有效结束时间")
    private LocalDateTime validEndTime;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("物料组件")
    private List<PmMaterialVO> list;

}
