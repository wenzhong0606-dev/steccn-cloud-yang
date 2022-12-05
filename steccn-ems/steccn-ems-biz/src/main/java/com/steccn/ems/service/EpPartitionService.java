package com.steccn.ems.service;

import com.steccn.ems.domain.EpPartition;
import com.baomidou.mybatisplus.extension.service.IService;
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
    Map<String,List<EpPartitionVO>> getEpEnergyTypeVOList(Integer energyType);


}
