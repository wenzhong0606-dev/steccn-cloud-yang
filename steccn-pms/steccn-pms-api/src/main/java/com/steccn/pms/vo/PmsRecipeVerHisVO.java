package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配方单历史版本
 *
 * @author AndrewLuo
 * @date 2023/02/16
 */
@Data
public class PmsRecipeVerHisVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 创建人姓名
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
