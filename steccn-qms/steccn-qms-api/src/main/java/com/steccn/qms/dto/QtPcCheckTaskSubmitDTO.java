package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * pc巡检任务值提交
 *
 * @author AndrewLuo
 * @date 2022/10/10
 */
@Data
public class QtPcCheckTaskSubmitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务状态
     */
    private Integer status;

    private Integer copyNum;
}
