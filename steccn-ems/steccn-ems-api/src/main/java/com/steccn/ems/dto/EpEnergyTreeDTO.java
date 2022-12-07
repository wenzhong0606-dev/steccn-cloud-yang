package com.steccn.ems.dto;

import com.steccn.ems.vo.EpPartitionVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 返回子树及节点
 *
 * @author AndrewLuo
 * @date 2022/12/07
 */
@Data
public class EpEnergyTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 子树信息
     */
    private List<EpPartitionVO> tree;

    /**
     * 所有关联实体表的节点
     */
    private Set<Long> partitionIds;
}
