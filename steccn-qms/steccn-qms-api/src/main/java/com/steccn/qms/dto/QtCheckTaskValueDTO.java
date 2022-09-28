package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 巡检结果第二层dto
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
    @NotNull(message = "样本")
    private Integer copy;

    private List<QtCheckTaskValueListDTO> list;
}
