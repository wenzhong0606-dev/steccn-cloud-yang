package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.util.Json;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @NotNull(groups = UpdateGroup.class, message = "编辑数据时编码不能为空")
    @ApiModelProperty("工艺路线编码")
    private String code;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料不能为空")
    @ApiModelProperty("物料id")
    private Long materialId;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料不能为空")
    @ApiModelProperty("物料名称")
    private String materialName;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料不能为空")
    @ApiModelProperty("物料编码")
    private String materialCode;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "状态不能为空")
    @ApiModelProperty("工艺路线状态：1有效0无效")
    @Max(value = 1, message = "状态只能是 1有效 0无效")
    @Min(value = 0, message = "状态只能是 1有效 0无效")
    private Integer status;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "版本不能为空")
    @ApiModelProperty("版本")
    private String version;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "是否当前版本不能为空")
    @ApiModelProperty("是否当前版本：1是0否")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isCurrentVersion;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "有效开始时间不能为空")
    @ApiModelProperty("有效开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validBeginTime;

    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "有效结束时间不能为空")
    @ApiModelProperty("有效结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validEndTime;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("工艺路线组件")
    private List<PmCraftRouteModuleDTO> list;

    @ApiModelProperty("工艺路线图")
    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "工艺路线图不能为空")
    private String roadmap;

    @ApiModelProperty("工序信息")
    private List<PmCraftRouteProcessDTO> processDTOList;

}
