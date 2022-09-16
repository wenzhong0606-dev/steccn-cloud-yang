package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 检验结果
 *
 * @author AndrewLuo
 * @date 2022/09/16
 */
@Data
public class QtCheckTaskCheckGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验项id
     */
    @NotNull(message = "检验项id信息不能为空")
    private Integer itemId;


    /**
     * 检验结果的快照json，包含id和name
     */
    @NotNull(message = "检验结果不能为空")
    private String checkResult;

    /**
     * 缺陷项id
     */
    private Integer defectId;
}
