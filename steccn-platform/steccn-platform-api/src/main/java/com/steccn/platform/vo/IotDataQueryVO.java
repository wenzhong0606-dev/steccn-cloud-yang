package com.steccn.platform.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/18
 */
@Data
public class IotDataQueryVO {
    private String deviceID;
    private String pointID;
    private List<Map<String, Object>> values;
}
