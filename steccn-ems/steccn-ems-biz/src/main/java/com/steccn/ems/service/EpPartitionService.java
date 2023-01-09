package com.steccn.ems.service;

import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpEnergyParamDTO;
import com.steccn.ems.dto.EpEnergyTreeDTO;
import com.steccn.ems.dto.EpPartitionChildTypeDTO;
import com.steccn.ems.dto.EpPartitionDTO;
import com.steccn.ems.vo.EpPartitionNameListVO;
import com.steccn.ems.vo.EpPartitionParentVO;
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
     * 根据能源分区的ids获取能源分区树的集合
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
     * 根据孩子id集合查询所有对应节点的父节点
     * @param childList
     * @return
     */
    Map<Long,EpPartitionVO> parentInfo(Set<Long> childList);

    /**
     * 根据id获取名字
     * @param partitionIdList 分区节点id
     * @return map
     */
    Map<Long, String> listByIdCh(List<Long> partitionIdList);


    /**
     * 根据id获取名字
     * @param partitionIdList 分区节点id
     * @return map
     */
    Map<Long, EpPartition> listById(Set<Long> partitionIdList);

    /**
     * 根据孩子id集合查询所有父节点
     * @param dto
     * @return
     */
    EpPartitionParentVO getParentList(EpPartitionChildTypeDTO dto);

    /**
     * 根据能源类型和组织机构id查询分区节点挂着的仪表
     * @param energyType 能源类型
     * @param officeIdSet 组织机构id
     * @return map key-组织机构id value-仪表对象
     */
    Map<Integer, EpInstrumentAccount> listByOfficeId(Integer energyType, Set<Integer> officeIdSet);

    /**
     * 获取所有下一级子节点
     * @param type 能源类型
     * @return
     */
    Map<Long,List<EpPartitionVO>> allNextChildren(Integer type);

    /**
     * 获取某一节点下的所有子节点id
     * @param partitionId
     * @return
     */
    List<EpPartitionVO> getAllChildrenById (Long partitionId);

    /**
     * 根据能耗类型查询能耗分区树根节点
     * @param energyType 能耗类型
     * @return
     */
    Map<Integer, EpPartition> rootMap(List<Integer> energyType);

    /**
     * 获取仅统计每日能耗（类型II）的节点
     * 节点N是产线节点或产线节点的下级节点：统计能耗考虑班次、产品厚度（类型I）
     * @return set
     */
    Set<Long> otherPartition(Set<Long> allPartitionIdSet);
}
