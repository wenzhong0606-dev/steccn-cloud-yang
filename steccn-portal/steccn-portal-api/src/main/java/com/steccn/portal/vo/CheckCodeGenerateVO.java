package com.steccn.portal.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 验证码生成vo
 */
@Data
public class CheckCodeGenerateVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 验证码的key
     */
    private String key;

    /**
     * 验证码
     */
    private String code;
}
