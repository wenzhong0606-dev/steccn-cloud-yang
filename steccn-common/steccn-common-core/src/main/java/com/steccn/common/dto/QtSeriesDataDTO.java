package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 折线图第二层VO
 * @author 王志雄
 * @param
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QtSeriesDataDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 缺陷项名称或类型名称
     */
    private String name;

    /**
     * 固定值line
     */
    private String type;

    /**
     * 固定值
     */
    private String stack;

    /**
     * 缺陷数量
     */
    private List<T> data;
}
