package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 能耗分区树
 */
@Data
public class EpPartitionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 能源类型id
     */
    @NotNull(message = "能源类型id不能为空")
    private Integer type;

    /**
     * 机构集合
     */
    private List<EpPartitionListDTO> epPartitionListDTO;

}
