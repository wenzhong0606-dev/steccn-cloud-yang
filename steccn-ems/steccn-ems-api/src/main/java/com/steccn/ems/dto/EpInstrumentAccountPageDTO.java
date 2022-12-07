package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 仪表台账分页dto
 */
@Data
public class EpInstrumentAccountPageDTO extends BasePageDTO implements Serializable {
    /**
     * 仪表名称
     */
    private String meterName;

    /**
     * 所属分区节点id
     */
    private List<Long> partitionNode;

}
