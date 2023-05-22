package com.steccn.platform.dto;

import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SysOfficeDeviceUpdateDTO implements Serializable {
    /**
     * 主键
     */
    @NotNull(message = "主键id不能为空")
    private Long id;

    /**
     * 组织架构类型
     */
    @NotNull(message = "组织架构类型不能为空")
    private Integer officeType;

    /**
     * 组织架构id
     */
    @NotNull(message = "组织架构id不能为空")
    private Integer officeId;

    /**
     * 采集点编码
     */
    @NotBlank(message = "设备编码不能为空")
    private String code;

    /**
     * 采集点名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String name;

    /**
     * 备注
     */
    @Length(max = 255, message = "备注长度不能超过255")
    private String note;

    private static final long serialVersionUID = 1L;
}
