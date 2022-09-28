package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 物料配置DTO
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@Data
@ApiModel("物料绑定检验方案DTO")
public class QtMaterialLinkCheckPlanDTO implements Serializable {
    /**
     * 关联物料id
     */
    @NotNull
    private String materialId;

    /**
     * 关联类型
     */
    private List<QtMaterialLinkCheckPlanTypeDTO> type;
}
