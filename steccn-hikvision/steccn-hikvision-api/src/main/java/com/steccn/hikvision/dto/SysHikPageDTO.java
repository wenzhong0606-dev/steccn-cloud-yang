package com.steccn.hikvision.dto;

import lombok.Data;

/**
 * 获取海康威视分页
 *
 * @author AndrewLuo
 * @date 2025/02/21
 */
@Data
public class SysHikPageDTO {
    private Integer pageNo;
    private Integer pageSize;
}
