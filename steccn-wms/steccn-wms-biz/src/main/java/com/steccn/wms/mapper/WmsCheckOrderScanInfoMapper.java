package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsCheckOrderScanInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.Set;


/**
* @author 34872
* @description 针对表【wms_check_order_scan_info(盘点扫描明细)】的数据库操作Mapper
* @createDate 2024-06-07 12:46:50
* @Entity com.steccn.wms.domain.WmsCheckOrderScanInfo
*/
@Mapper
public interface WmsCheckOrderScanInfoMapper extends BaseMapper<WmsCheckOrderScanInfo> {

    List<WmsCheckOrderAreaVO> areaCountList(@Param("checkOrderId") Long checkOrderId, @Param("shelfIds") Set<Long> shelfIds);
}




