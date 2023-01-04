package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 质量日报表-首检数据视图对象
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class QtDayReportFirstDataVO {
    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;
}
