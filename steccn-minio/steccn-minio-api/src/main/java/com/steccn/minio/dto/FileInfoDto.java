package com.steccn.minio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/16
 */
@Data
@ApiModel
public class FileInfoDto implements Serializable {

    @ApiModelProperty("文件信息")
    @NotNull
    private MultipartFile file;

    @ApiModelProperty("文件名称")
    private String fileName;
}
