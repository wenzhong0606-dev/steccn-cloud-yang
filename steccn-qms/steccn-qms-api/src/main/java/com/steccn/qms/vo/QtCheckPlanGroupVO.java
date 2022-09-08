package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 检验方案分组vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanGroupVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String groupName;
    private Integer sort;
    private List<QtCheckPlanItemVO> items;
}
