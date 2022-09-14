package com.steccn.pms.dto;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;


/**
 * 工序Excel导入
 *
 * @author Mongknife
 * @date 2022/9/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiOperation("工序Excel导入")
public class PmProcessExcelDTO implements Serializable {

    @ExcelProperty(value = "工序编码")
    private String code;

    @ExcelProperty(value = "物料编码")
    private String materialCode;

    @ExcelProperty(value = "类型")
    private Integer type;

    @ExcelProperty(value = "状态（0:无效 1：有效）")
    private Integer status;

    @ExcelProperty(value = "是否报工（0：否 1：是）")
    private Integer isReportingWorks;

    @ExcelProperty(value ="是否报检（0：否 1：是）")
    private Integer isCustomsInspection;

    @ExcelProperty(value = "备注")
    private String node;
}
