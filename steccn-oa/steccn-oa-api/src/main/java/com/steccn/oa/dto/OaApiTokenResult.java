package com.steccn.oa.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 请求oa后台apiToken返回值
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Data
@ApiModel("oaApiToken返回值")
public class OaApiTokenResult {
    private boolean status;
    private String token;
    private String msg;
}
