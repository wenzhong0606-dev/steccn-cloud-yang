package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EpPartitionNameListVO implements Serializable {
    /**
     * 能源分区名称
     */
    private String name;

    /**
     * 能源分区树
     */
    private List<EpPartitionVO> epPartitionVOList;
}
