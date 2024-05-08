package com.steccn.pms.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/05/07
 */
@Component
@Data
public class SrmUrl {
    @Value("${srm.url}")
    private String srmIp;
}
