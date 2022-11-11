package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.FeedQuantityDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_complete_delivery_info(出库详情)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteDeliveryInfo
*/
@Mapper
public interface PmsCompleteDeliveryInfoMapper extends BaseMapper<PmsCompleteDeliveryInfo> {

    /**
     * 根据订单id查询出库信息
     * @param orderId
     * @param page
     * @return
     */
    Page<PmsCompleteDeliveryVO> listByOrderId(Page page, @Param("orderId") Long orderId);

    /**
     * 获取投料信息
     * @param feedList
     * @return
     */
    List<FeedQuantityDTO> listQuantity(@Param("feedList") List<FeedQuantityDTO> feedList);
}




