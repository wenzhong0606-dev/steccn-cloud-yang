package com.steccn.pms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/8
 */
@Data
public class PmsBillExcelDTO implements Serializable {


    @ExcelProperty("清单编码")
    private String code;

    @ExcelProperty("物料编码")
    private String materialCode;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("标准数量")
    private String quantity;

    @ExcelProperty("版本")
    private String version;

    @ExcelProperty("是否当前版本")
    private String isCurrentVersion;

    @ExcelProperty("有效期开始时间")
    private LocalDateTime validBeginTime;

    @ExcelProperty("有效期结束时间")
    private LocalDateTime validEndTime;

    @ExcelProperty("备注")
    private String node;

    @ExcelProperty("组件编码")
    private String moduleCode;

    @ExcelProperty("组件数量")
    private String moduleQuantity;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("是否固定数量")
    private Integer isFixedQuantity;

    @ExcelProperty("是否副产品")
    private Integer isCopruduct;

    @ExcelProperty("是否联产品")
    private Integer isJointProduct;

    @ExcelProperty("是否反冲")
    private Integer isBackCalculation;

    @ExcelProperty("工序报废%")
    private Integer processScrapingPercent;

    @ExcelProperty("组件报废%")
    private Integer componentScrapingPercent;

}
