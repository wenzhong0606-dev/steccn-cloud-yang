package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@Data
@ApiModel("工序管理保存请求dto")
public class PmProcessDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空，编辑时存在")
    private Integer id;
    @ApiModelProperty("工序名称")
    private String name;

    @ApiModelProperty("工序编码")
    private String code;

    @ApiModelProperty("工序类型")
    private Integer type;

    @ApiModelProperty("工序状态")
    private Integer status;

    @ApiModelProperty("备注")
    private String node;

    @ApiModelProperty("文件id")
    private Long fileId;

    @ApiModelProperty("执行工位列表")
    private List<PmProcessStationDTO> list;


    @ApiModelProperty("是否报工")
    private Integer isReportingWorks;


    @ApiModelProperty("是否报检")
    private Integer isCustomsInspection;



}
