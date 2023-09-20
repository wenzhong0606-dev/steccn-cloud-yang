package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 前端统一封装的行
 *
 * @author AndrewLuo
 * @date 2023/09/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SteccnTableColumn implements Serializable{
    /**
     * 对应list对象中的字段
     */
    private String prop;
    /**
     * 行显示的字段
     */
    private String label;
    /**
     * 多级表头
     */
    private List<SteccnTableColumn> children;
}
