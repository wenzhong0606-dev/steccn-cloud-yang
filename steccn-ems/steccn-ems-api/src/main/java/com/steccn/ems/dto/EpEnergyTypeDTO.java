package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能源类型新增编辑dto
 */
@Data
public class EpEnergyTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 能源类型名称
     */
    @NotBlank(message = "能源类型名称不能为空")
    @Length(max = 20, message = "名称长度不能超过20个文字")
    private String name;

    /**
     * 计量单位
     */
    @NotNull(message = "计量单位不能为空")
    private Long measureUnit;

    /**
     * 计量仪表名称
     */
    @Length(max = 20, message = "计量仪表名称长度不能超过20个文字")
    @NotBlank(message = "计量仪表名称不能为空")
    private String measureMeterName;

    /**
     * 关联图表
     */
    @NotEmpty(message = "关联图表不能为空")
    private List<Integer> linkTable;

    /**
     * 备注
     */
    @Length(max = 100, message = "备注不能超过100个文字")
    private String note;
}
