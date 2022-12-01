package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
    @Length(max = 20, message = "名称长度不能超过20个中文")
    private String name;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 计量仪表名称
     */
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "计量仪表名称长度不能超过20个中文")
    private String measureMeterName;

    /**
     * 关联图表
     */
    private List<Integer> linkTable;

    /**
     * 备注
     */
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注不能超过100个中文")
    private String note;

    /**
     * 是否删除 0 否 1是
     */
    private Integer delFlag;
}
