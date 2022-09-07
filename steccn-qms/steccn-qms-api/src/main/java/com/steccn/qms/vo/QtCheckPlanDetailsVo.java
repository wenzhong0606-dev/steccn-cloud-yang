package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 检验方案回显vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanDetailsVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String note;
    private String code;
    private String name;
    private List<QtCheckPlanGroupVo> checkGroup;
}
