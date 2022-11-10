package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.ProduceOrderProcessNumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteDelivery
*/
@Mapper
public interface PmsCompleteDeliveryMapper extends BaseMapper<PmsCompleteDelivery> {


    /**
     * 根据时间 物料 工序id查询投料信息
     * @param page
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> pageList(Page page,@Param("dto") PmsCompleteDeliveryDTO dto);

    /**
     * 生产订单工序物料消耗和计划数量
     * @param produceOrderIdSet 订单集合
     * @return list
     */
    List<ProduceOrderProcessNumVO> processNumConsumeMap(@Param("produceOrderIdSet") Set<Long> produceOrderIdSet);
}




