package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.FeedQuantityDTO;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery_info(出库详情)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsCompleteDeliveryInfoService extends IService<PmsCompleteDeliveryInfo> {

    /**
     * 根据订单id 查询出库信息分页
     *
     * @param pmsCompleteDeliveryDTO
     * @return
     */
    Page<PmsCompleteDeliveryVO> listByOrderId(PmsCompleteDeliveryDTO pmsCompleteDeliveryDTO);


    /**
     * 获取投料数量信息
     * @param feedList
     * @return
     */
    Map<FeedQuantityDTO, BigDecimal> listQuantity(List<FeedQuantityDTO> feedList);
}
