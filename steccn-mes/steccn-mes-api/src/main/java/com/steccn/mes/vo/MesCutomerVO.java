package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备客户VO
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Data
public class MesCutomerVO implements Serializable {
    private String customerCode;
    private String name;
    private LocalDateTime lastModifiedDateTime;
}
