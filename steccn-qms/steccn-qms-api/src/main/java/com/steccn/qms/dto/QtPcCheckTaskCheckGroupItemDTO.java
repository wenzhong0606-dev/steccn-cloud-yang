package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Integer itemId;

    /**
     * 结果
     */
    private Map<String, String> result;

    /**
     * 塞入不合格的checkResult
     *
     */
    private List<String> isQualifiedList;

}
