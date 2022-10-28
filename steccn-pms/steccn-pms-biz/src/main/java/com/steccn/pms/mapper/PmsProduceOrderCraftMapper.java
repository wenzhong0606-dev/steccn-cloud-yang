package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_produce_order_craft(生产订单-工艺信息)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsProduceOrderCraft
*/
@Mapper
public interface PmsProduceOrderCraftMapper extends BaseMapper<PmsProduceOrderCraft> {

    /**
     * 删除不在集合中的数据
     * @param idSet
     * @param produceId
     * @return
     */
    Boolean deleteCrafts(@Param("idSet") Set<Long> idSet,@Param("produceId") Long produceId);
}




