package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 样本详情DTO
 *
 * @author AndrewLuo
 * @date 2022/10/11
 */
@Data
public class QtCheckTaskSimpleDTO implements Serializable {


    /**
     * 标识是哪个样本字段
     */
    private String id;

    /**
     * 定义样本概况数据结构
     */
    private Map<String,Map<String,Object>> simpleMap;

}
