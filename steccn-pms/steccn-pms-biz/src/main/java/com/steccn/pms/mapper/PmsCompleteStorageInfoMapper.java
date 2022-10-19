package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author yequan
* @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteStorageInfo
*/
@Mapper
public interface PmsCompleteStorageInfoMapper extends BaseMapper<PmsCompleteStorageInfo> {

    /**
     * 根据订单id获取完工入库分页列表
     * @param page
     * @param produceOrderId
     * @return
     */
    Page<PmsCompleteStorageVO> pageByOrderId(Page page, @Param("orderId") Long produceOrderId);
}




