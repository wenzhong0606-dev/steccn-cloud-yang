package com.steccn.platform.vo;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/18
 */
@Data
public class IotDataQueryDetailVO {
    private Map<String, Object> extra = new HashMap<>();

    @JsonAnySetter
    public void addExtra(String key, Object value) {

    }
}
