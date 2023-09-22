package com.steccn.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端统一封装的表格
 *
 * @author AndrewLuo
 * @date 2023/09/19
 */
@Data
public class SteccnTableDataDTO<T> implements Serializable {
    /**
     * 表头
     */
    private List<SteccnTableColumn> columns;

    /**
     * 数据
     */
    private List<T> tableList;
}
