package com.steccn.qms.vo;

import com.steccn.qms.dto.QtMaterialLinkCheckPlanTypeDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料配置分页Vo
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@Data
public class QtMaterialPageVO implements Serializable {

    /**
     * 物料id
     */
    private Long id;

    /**
     * 物料编码
     */
    private String code;

    /**
     * 物料名称
     */
    private String name;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 已配检验类型
     */
    private List<String> matchedCheckType;

    /**
     * 已关联检验方案
     */
    private List<String> linkedCheckPlan;
}
