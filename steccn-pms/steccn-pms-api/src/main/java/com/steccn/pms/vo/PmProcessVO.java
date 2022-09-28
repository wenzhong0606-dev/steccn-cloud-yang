package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序分页
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@ApiModel("工序分页Vo")
@Data
public class PmProcessVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("文件url")
    private String fileUrl;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("文件id")
    private Long fileId;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人名称")
    private String updateName;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("工位信息")
    private List<PmProcessStationVO> list;

    @ApiModelProperty("是否报工")
    private Integer isReportingWorks;


    @ApiModelProperty("是否报检")
    private Integer isCustomsInspection;

}
