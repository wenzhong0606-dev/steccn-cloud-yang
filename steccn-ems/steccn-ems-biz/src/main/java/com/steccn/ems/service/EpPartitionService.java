package com.steccn.ems.service;

import com.steccn.ems.domain.EpPartition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpEnergyParamDTO;
import com.steccn.ems.dto.EpEnergyTreeDTO;
import com.steccn.ems.dto.EpPartitionDTO;
import com.steccn.ems.vo.EpPartitionNameListVO;
import com.steccn.ems.vo.EpPartitionVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_partition(能耗分区)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpPartitionService extends IService<EpPartition> {

    /**
     * 根据能源类型查询能耗分区树
     * @param energyType
     * @return
     */
    EpPartitionNameListVO getEpEnergyTypeVOList(Integer energyType);


    /**
     * 根据能源分区的ids获取能源分区的集合
     */
    Map<Long,EpPartitionVO> getEpPartitionNameListVO(Set<Long> ids);

    /**
     * 编辑能源分区树
     * @param dto
     * @return
     */
    Boolean edit(EpPartitionDTO dto);

    /**
     * 获取子树的节点
     * @param dto
     * @return
     */
    EpEnergyTreeDTO getChildTree(EpEnergyParamDTO dto);

    /**
     * 根据id获取名字
     * @param partitionIdList 分区节点id
     * @return map
     */
    Map<Long, String> listByIdCh(List<Long> partitionIdList);
}
