package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 为解决mp泛型擦除
 * @date 2023/12/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdNameDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;
}
