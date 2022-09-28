package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 检验结果DTO
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long id;
    /**
     * 检验数量
     */
    @NotNull(message = "检验数量不能为空")
    private Integer checkNum;

    /**
     * 合格数量
     */
    @NotNull(message = "合格数量不能为空")
    private Integer goodNum;

    /**
     * 不合格数量
     */
    @NotNull(message = "不合格数量不能为空")
    private Integer badNum;

    /**
     * 结果判定
     */
    private String result;

    /**
     * 备注
     */
    private String note;

}
