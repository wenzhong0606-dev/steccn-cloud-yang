package com.steccn.qms.dto;

import com.sun.istack.internal.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 设置目标值
 *
 * @author AndrewLuo
 * @date 2022/11/08
 */
@Data
public class QtChatTargetValueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer key;

    @NotNull
    private BigDecimal value;
}
