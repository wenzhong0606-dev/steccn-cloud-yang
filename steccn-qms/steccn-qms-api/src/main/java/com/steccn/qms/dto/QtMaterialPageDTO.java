package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * 物料配置查询DTO
 * @author AndrewLuo
 * @date 2022/09/08
 */
@Data
public class QtMaterialPageDTO extends BasePageDTO implements Serializable {
    /**
     * 物料名称
     */
    private String nameOrCode;
    /**
     * 检验类型
     */
    private String checkType;
    /**
     * 检验方案
     */
    private String linkedPlan;
}
