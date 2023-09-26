package com.steccn.wms.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Component
@Data
public class SrmConfig {
    @Value("${srm.url}")
    private String srmIp;

    @Value("${srm.applicationGroupCode}")
    private String applicationGroupCode;

    @Value("${srm.externalSystemCode}")
    private String externalSystemCode;

    @Value("${srm.interfaceCode}")
    private String interfaceCode;

    @Value("${srm.applicationCode}")
    private String applicationCode;
}
