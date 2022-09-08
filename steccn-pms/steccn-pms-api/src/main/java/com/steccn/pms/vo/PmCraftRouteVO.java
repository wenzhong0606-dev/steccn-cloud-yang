package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线VO")
public class PmCraftRouteVO  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工艺路线编码")
    private String code;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("工艺路线状态：1有效0无效")
    private Integer status;

    @ApiModelProperty("版本")
    private String version;

    @ApiModelProperty("是否当前版本：1是0否")
    private Integer isCurrentVersion;

    @ApiModelProperty("有效开始时间")
    private LocalDateTime validBeginTime;

    @ApiModelProperty("有效结束时间")
    private LocalDateTime validEndTime;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("工艺路线组件信息")
    private List<PmCraftRouteModuleVO> list;

    @ApiModelProperty("路线图")
    private List<String> roadmap;

}
