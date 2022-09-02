package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件信息
 * @TableName file_info
 */
@TableName(value ="file_info")
@Data
public class FileInfo implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * minio桶名
     */
    private String bucketName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileExtension;

    /**
     * 文件大小
     */
    private Double fileSize;

    /**
     * 文件url
     */
    private String fileUrl;

    /**
     * 文件别名
     */
    private String fileAlias;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}