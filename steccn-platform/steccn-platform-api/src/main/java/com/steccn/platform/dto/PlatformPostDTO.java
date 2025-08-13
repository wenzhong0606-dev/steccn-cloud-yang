package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/31
 */
@Data
public class PlatformPostDTO extends BasePageDTO {
    /**
     * id
     */
    private Integer id;

    /**
     * 岗位编码
     */
    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    /**
     * 岗位名称
     */
    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    /**
     * 岗位排序（升序）
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    private String remarks;
}
