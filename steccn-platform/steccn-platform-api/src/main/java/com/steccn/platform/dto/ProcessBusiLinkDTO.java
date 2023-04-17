package com.steccn.platform.dto;


import lombok.Data;

/**
 * 接收Oa流程与业务关联的接口
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class ProcessBusiLinkDTO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 流程实例id
     */
    private Integer instanceId;

    /**
     * 业务类型
     */
    private Integer busiType;

    /**
     * 业务主键
     */
    private Long busiId;
}
