package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工艺路线DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线DTO")
public class PmCraftRouteDTO extends BasePageDTO implements Serializable {

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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validBeginTime;

    @ApiModelProperty("有效结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validEndTime;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("工艺路线组件")
    private List<PmCraftRouteModuleDTO> list;

    @ApiModelProperty("工艺路线图")
    private List<String> roadmap;

    @ApiModelProperty("工序信息")
    private List<PmCraftRouteProcessDTO> processDTOList;

}
