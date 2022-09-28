package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 巡检详情页面
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 单个物料
     */
    private QtCheckTaskListVO qtCheckTaskListVo;

    /**
     * 找出检验方案
     */

    /**
     *
     */
    private QtCheckPlanDetailsVO qtCheckPlanDetailsVO;
}
