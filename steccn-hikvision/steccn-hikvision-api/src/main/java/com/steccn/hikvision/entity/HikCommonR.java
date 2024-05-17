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
public class HikCommonR<T> {
    private int code;
    private String msg;
    private HikCommonData<T> data;
}
