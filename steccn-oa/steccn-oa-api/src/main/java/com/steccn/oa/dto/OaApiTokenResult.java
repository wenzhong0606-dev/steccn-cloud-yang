package com.steccn.oa.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求oa后台apiToken返回值
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Data
@ApiModel("oaApiToken返回值")
public class OaApiTokenResult implements Serializable {
    /**
     * 状态 true表示正常 false表示错误
     */
    private boolean status;
    /**
     * token的值
     */
    private String token;
    /**
     * 返回的错误消息
     */
    private String msg;
}
