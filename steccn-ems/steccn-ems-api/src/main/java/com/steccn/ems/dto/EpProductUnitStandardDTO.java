package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.BaseSelectVO;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EpProductUnitStandardDTO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 标准名称
     */
    @NotBlank(message="标准名称不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "标准名称长度不能超过50个中文")
    private String name;

    /**
     * 能源类型id
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "能源类型不能为空")
    private Integer energyType;

    /**
     * 标准类型【企业标准、行业标准、部门标准】
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "标准类型不能为空")
    private Long standardType;


    /**
     * 应用部门
     */
    private List<Long> applyOffice;

    /**
     * 产品类型【基片、深加工】
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "应用部门不能为空")
    private Long productType;

    /**
     * 产品厚度
     */
    private Long productThick;

    /**
     * 单耗标准值
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "单耗标准值不能为空")
    private BigDecimal unitValue;

    /**
     * 计量单位
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "计量的单位不能为空")
    private Long measureUnit;

    /**
     * 有效期-开始
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "有效期开始时间不能为空")
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "有效期结束时间不能为空")
    private LocalDate validEnd;

    /**
     * 备注
     */
    private String note;

}
