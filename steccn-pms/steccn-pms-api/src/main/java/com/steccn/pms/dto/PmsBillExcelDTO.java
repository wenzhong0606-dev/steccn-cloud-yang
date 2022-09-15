package com.steccn.pms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.steccn.common.valid.InsertGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
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

    @NotNull(groups = InsertGroup.class,message = "清单编码不能为空")
    @ExcelProperty("清单编码")
    private String code;

    @NotNull(groups = InsertGroup.class,message = "物料编码不能为空")
    @ExcelProperty("物料编码")
    private String materialCode;

    @NotNull(groups = InsertGroup.class,message = "状态不能为空")
    @ExcelProperty("状态")
    private Integer status;

    @NotNull(groups = InsertGroup.class,message = "标准数量不能为空")
    @ExcelProperty("标准数量")
    private Integer quantity;

    @NotNull(groups = InsertGroup.class,message = "版本不能为空")
    @ExcelProperty("版本")
    private String version;

    @NotNull(groups = InsertGroup.class,message = "是否当前版本不能为空")
    @ExcelProperty("是否当前版本")
    private Integer isCurrentVersion;

    @NotNull(groups = InsertGroup.class,message = "有效期开始时间不能为空")
    @ExcelProperty("有效期开始时间")
    private LocalDateTime validBeginTime;

    @NotNull(groups = InsertGroup.class,message = "有效期结束时间不能为空")
    @ExcelProperty("有效期结束时间")
    private LocalDateTime validEndTime;

    @NotNull(groups = InsertGroup.class,message = "备注不能为空")
    @ExcelProperty("备注")
    private String node;

    @NotNull(groups = InsertGroup.class,message = "组件编码不能为空")
    @ExcelProperty("组件编码")
    private String moduleCode;

    @NotNull(groups = InsertGroup.class,message = "组件数量不能为空")
    @ExcelProperty("组件数量")
    private Integer moduleQuantity;

    @NotNull(groups = InsertGroup.class,message = "单位不能为空")
    @ExcelProperty("单位")
    private String unit;

    /**
     * 用于查找单位数据
     */
    @ExcelIgnore
    private Long unitId;

    @NotNull(groups = InsertGroup.class,message = "是否固定数量不能为空")
    @ExcelProperty("是否固定数量")
    private Integer isFixedQuantity;

    @NotNull(groups = InsertGroup.class,message = "是否副产品不能为空")
    @ExcelProperty("是否副产品")
    private Integer isCopruduct;

    @NotNull(groups = InsertGroup.class,message = "是否联产品不能为空")
    @ExcelProperty("是否联产品")
    private Integer isJointProduct;

    @NotNull(groups = InsertGroup.class,message = "是否反冲不能为空")
    @ExcelProperty("是否反冲")
    private Integer isBackCalculation;

    @NotNull(groups = InsertGroup.class,message = "工序报废%不能为空")
    @ExcelProperty("工序报废%")
    private Integer processScrapingPercent;

    @NotNull(groups = InsertGroup.class,message = "组件报废%不能为空")
    @ExcelProperty("组件报废%")
    private Integer componentScrapingPercent;

}
