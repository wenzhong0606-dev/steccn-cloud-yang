package com.steccn.portal.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 验证码生成dto
 */
@Data
public class CheckCodeGenerateDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 业务类型：默认为0 登录
     */
    private Integer busiType;

}
