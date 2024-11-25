package com.steccn.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 前端vo
 */
@Data
public class FrontTableDTO<T> implements Serializable {


    /**
     * 表头关系
     */
    private List<FrontTableElement> columns;

    /**
     * 数据
     */
    private List<Map<String, T>> tableList;
}
