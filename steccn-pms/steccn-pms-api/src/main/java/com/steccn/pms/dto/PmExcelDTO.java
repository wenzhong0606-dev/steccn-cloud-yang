package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
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
 * @date 2022/9/8
 */
@Data
@ApiModel("Excel更新DTO")
public class PmExcelDTO implements Serializable {

    @ApiModelProperty("选择是否更新已存在数据，若选中则根据编码更新对应数据")
    private Integer updateFlag;

    @NotNull(groups = InsertGroup.class,message = "文件不能为空")
    @ApiModelProperty("excel文件")
    private MultipartFile file;
}
