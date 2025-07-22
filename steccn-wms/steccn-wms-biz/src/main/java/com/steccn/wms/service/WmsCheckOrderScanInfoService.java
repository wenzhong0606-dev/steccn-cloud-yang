package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.wms.domain.WmsCheckOrderScanInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsCheckOrderScanInfoDTO;
import com.steccn.wms.dto.WmsCheckOrderScanInfoListDTO;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import com.steccn.wms.vo.WmsCheckOrderScanInfoVO;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.Set;

/**
* @author 34872
* @description 针对表【wms_check_order_scan_info(盘点扫描明细)】的数据库操作Service
* @createDate 2024-06-07 12:46:50
*/
public interface WmsCheckOrderScanInfoService extends IService<WmsCheckOrderScanInfo> {

    Boolean addScanCheckInfo(WmsCheckOrderScanInfoDTO dto);

    Boolean editScanCheckInfo(WmsCheckOrderScanInfoDTO dto);

    Page<WmsCheckOrderScanInfoVO> listScanCheckInfo(WmsCheckOrderScanInfoListDTO dto);

    List<WmsCheckOrderAreaVO> areaCountList(Long checkOrderId, Set<Long> shelfIds);
}
