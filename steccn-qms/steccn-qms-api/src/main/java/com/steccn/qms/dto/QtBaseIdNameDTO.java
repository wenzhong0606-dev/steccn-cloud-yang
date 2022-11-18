package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/10/08
 */
@Data
public class QtBaseIdNameDTO implements Serializable{
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
}
