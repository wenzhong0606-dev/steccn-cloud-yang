package com.steccn.ems.vo;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.BaseSelectVO;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EpAddEnergyTypeVO {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 能源类型名称
     */
    private String name;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 计量仪表名称
     */
    private String measureMeterName;

    /**
     * 关联图表
     */
    private List<BaseSelectVO<Integer>> linkTable;

    /**
     * 备注
     */
    private String note;

    /**
     * 是否删除 0 否 1是
     */
    private Integer delFlag;
}
