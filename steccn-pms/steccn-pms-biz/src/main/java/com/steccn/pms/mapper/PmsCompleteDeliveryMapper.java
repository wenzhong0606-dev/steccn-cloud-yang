package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteDelivery
*/
@Mapper
public interface PmsCompleteDeliveryMapper extends BaseMapper<PmsCompleteDelivery> {

}




