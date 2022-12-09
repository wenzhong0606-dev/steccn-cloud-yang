package com.steccn.ems.mapper;

import com.steccn.ems.domain.EpPartition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.ems.vo.EpPartitionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_partition(能耗分区)】的数据库操作Mapper
* @createDate 2022-11-29 19:27:28
* @Entity com.steccn.ems.domain.EpPartition
*/
@Mapper
public interface EpPartitionMapper extends BaseMapper<EpPartition> {


    List<EpPartitionVO> listParentInfo(Set<Long> childList);
}




