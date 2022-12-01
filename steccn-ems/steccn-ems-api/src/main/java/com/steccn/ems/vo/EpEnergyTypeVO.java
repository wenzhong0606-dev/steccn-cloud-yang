package com.steccn.ems.vo;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.BaseSelectVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EpEnergyTypeVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    /**
     * 能源类型名称
     */
    private String name;

    /**
     * 计量单位id
     */
    private Long measureUnit;

    /**
     * 计量单位名称
     */
    private String measureUnitName;

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

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

}
