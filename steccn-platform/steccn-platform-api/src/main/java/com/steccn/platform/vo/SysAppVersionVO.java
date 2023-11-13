package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * app版本控制vo
 *
 * @author Mongknife
 * @date 2023/1/31
 */
@Data
@ApiModel("app版本控制vo")
public class SysAppVersionVO {

    /**
     * 主键id
     */
    @ApiModelProperty("主键id")
    private Long id;

    /**
     * 版本
     */
    @ApiModelProperty("版本号")
    private String version;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String note;

    /**
     * 文件id
     */
    @ApiModelProperty("app文件id")
    private Long fileId;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * app下载url
     */
    @ApiModelProperty("app下载url")
    private String fileUrl;

    /**
     * 平台 0-Android 1-ios
     */
    private Integer platform;
}
