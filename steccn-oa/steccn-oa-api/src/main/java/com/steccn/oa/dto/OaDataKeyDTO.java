package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa通过sessionKey获取Data数据
 *
 * @author AndrewLuo
 * @date 2022/11/04
 */
@Data
public class OaDataKeyDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 返回的sessionId
     */
    private String dataKey;
    /**
     * 分页数据
     */
    private String current;
}
