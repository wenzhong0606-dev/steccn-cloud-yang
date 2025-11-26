package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

/**
 * IOT设备查询
 *
 * @author AndrewLuo
 * @date 2025/11/11
 */
@Data
public class IotDeviceDTO extends BasePageDTO {
    private String search;
    private String groupId;
    private String deviceId;
}
