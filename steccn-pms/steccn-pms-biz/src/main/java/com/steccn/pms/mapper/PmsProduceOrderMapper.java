package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsProduceOrderPageDTO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsProduceOrder
 */
@Mapper
public interface PmsProduceOrderMapper extends BaseMapper<PmsProduceOrder> {

    Page<PmsProduceOrderVO> pageList(Page page, @Param("dto") PmsProduceOrderPageDTO dto);
}




