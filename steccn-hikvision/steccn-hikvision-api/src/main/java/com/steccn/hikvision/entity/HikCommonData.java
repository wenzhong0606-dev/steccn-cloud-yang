package com.steccn.hikvision.entity;

import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/05/17
 */
@Data
public class HikCommonData<T> {
    private Integer total;
    private Integer pageNo;

    private Integer pageSize;

    private List<T> list;
}
