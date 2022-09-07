package com.steccn.minio.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息VO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("文件信息VO")
public class FileInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("minio桶名")
    private String bucketName;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件后缀")
    private String fileExtension;

    @ApiModelProperty("文件大小")
    private Double fileSize;

    @ApiModelProperty("文件url")
    private String fileUrl;

    @ApiModelProperty("文件别名")
    private String fileAlias;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人姓名")
    private String createName;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人名称")
    private String updateName;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
