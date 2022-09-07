package com.steccn.pms.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.pms.vo.PmMaterialVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料清单DTO
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("物料清单dto")
public class PmBillOfMaterialDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("清单code")
    private String code;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料id")
    private String materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("状态： 1有效 2无效")
    private Integer status;

    @ApiModelProperty("标准数量")
    private Object quantity;

    @ApiModelProperty("单位id")
    private Integer unitId;

    @ApiModelProperty("版本")
    private String version;

    @ApiModelProperty("是否当前版本 1是0否")
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

    @ApiModelProperty("物料组件")
    private List<PmMaterialDTO> list;
}
