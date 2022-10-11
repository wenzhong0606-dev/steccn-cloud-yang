package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * pcItem类
 *
 * @author AndrewLuo
 * @date 2022/10/10
 */
@Data
public class QtPcCheckTaskCheckGroupItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验项id
     */
    private Integer id;

    /**
     * 结果
     */
    private Map<String, String> result;

    /**
     * 是否合格
     * 1合格 0不合格
     */
    private Integer isQualified;
}
