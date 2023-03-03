package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 客户物料分页请求dto
 * @date 2023/3/3
 */
@ApiModel("客户物料分页请求dto")
@Data
public class SysCustomerMaterialDTO extends BasePageDTO implements Serializable {

    @ApiModelProperty("包材编码")
    private String subMaterialCode;

    @ApiModelProperty("包材名称")
    private String subMaterialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("客户主键")
    private Integer customerId;

    @ApiModelProperty("创建时间-开始")
    private LocalDateTime start;

    @ApiModelProperty("创建时间-结束")
    private LocalDateTime end;

}
