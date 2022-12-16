package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysOfficeCollectUpdateDTO implements Serializable {
    /**
     * 主键
     */
    @NotNull(groups = {UpdateGroup.class},message = "主键id不能为空")
    private Long id;

    /**
     * 组织架构类型
     */
    @NotNull(groups = {UpdateGroup.class},message = "组织架构类型不能为空")
    private Integer officeType;

    /**
     * 组织架构id
     */
    @NotNull(groups = {UpdateGroup.class},message = "组织架构id不能为空")
    private Integer officeId;

    /**
     * 进站0 出站1
     */
    @NotNull(groups = {UpdateGroup.class},message = "进站0 出站1不能为空")
    private Integer type;

    /**
     * 采集点编码
     */
    @NotBlank(groups = {UpdateGroup.class},message = "采集点编码不能为空")
    private String code;

    /**
     * 采集点名称
     */
    @NotBlank(groups = {UpdateGroup.class},message = "采集点名称不能为空")
    private String name;

    /**
     * 备注
     */
    private String note;

    private static final long serialVersionUID = 1L;
}
