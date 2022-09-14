package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskValueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验分组id
     */
    @NotNull(message = "分组信息不能为空")
    private Integer groupId;

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
