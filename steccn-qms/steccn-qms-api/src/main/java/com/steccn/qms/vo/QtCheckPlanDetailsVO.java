package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验方案回显vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanDetailsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String note;
    private String code;
    private String name;
    private List<QtCheckPlanGroupVO> checkGroup;
    private String createName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /**
     * 任务详情专用vo
     */
    private List<QtCheckTaskDetailCopyVO> copy;
}
