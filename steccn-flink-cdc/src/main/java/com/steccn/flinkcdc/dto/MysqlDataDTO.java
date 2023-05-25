package com.steccn.flinkcdc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mysql转换数据
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MysqlDataDTO {

    /**
     * 数据库
     */
    private String database;
    /**
     * 表
     */
    private String table;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 变更前数据
     */
    private String before;
    /**
     * 变更后数据
     */
    private String after;
}
