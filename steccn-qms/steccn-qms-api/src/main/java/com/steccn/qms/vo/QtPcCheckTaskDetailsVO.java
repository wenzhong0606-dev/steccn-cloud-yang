package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/10/13
 */
@Data
public class QtPcCheckTaskDetailsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private QtCheckTaskListVO QtPcCheckTaskDetailsVO;

    private List<QtCheckPlanGroupVO> checkGroup;
}
