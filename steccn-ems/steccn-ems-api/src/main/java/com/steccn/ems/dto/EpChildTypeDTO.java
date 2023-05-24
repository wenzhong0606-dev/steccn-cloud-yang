package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Set;

/**
 * @author yequan
 * @desc
 * @date 2023/5/23
 */
@Data
@ApiModel("孩子类型")
public class EpChildTypeDTO implements Serializable {

    @ApiModelProperty("虚拟节点孩子类型")
    private Integer type;

    @ApiModelProperty("虚拟节点的孩子")
    private Set<Long> children;

    @ApiModelProperty("当前节点关联的仪表")
    private EpInstrumentAccountDTO account;

}
