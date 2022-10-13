package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsProduceOrderPageDTO;
import com.steccn.pms.dto.ProduceOrderAcceptPageDTO;
import com.steccn.pms.dto.ReportWorkOrderPageDTO;
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

    /**
     * 根据条件筛选订单信息
     * @param page
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> pageList(Page page, @Param("dto") PmsProduceOrderPageDTO dto);

    /**
     * 获取订单接收数据
     *
     * @param page
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> acceptPage(Page page, @Param("dto") ProduceOrderAcceptPageDTO dto);

    /**
     * 订单报工-根据条件筛选订单信息
     * @param page 分页信息
     * @param dto dto
     * @return 报工信息
     */
    Page<PmsProduceOrderVO> reportWorkOrderPage(Page page, @Param("dto") ReportWorkOrderPageDTO dto);
}




