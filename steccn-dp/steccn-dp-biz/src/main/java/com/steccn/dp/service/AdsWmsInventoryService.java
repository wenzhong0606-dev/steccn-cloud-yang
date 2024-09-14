package com.steccn.dp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsWmsInventory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.vo.AdsWmsInventoryVO;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 34872
* @description 针对表【ads_wms_inventory(仓储期末库存统计)】的数据库操作Service
* @createDate 2024-03-15 10:43:26
*/
public interface AdsWmsInventoryService extends IService<AdsWmsInventory> {

    TupleDTO<BigDecimal,BigDecimal> ending(DpWmsDataDTO dto);

    TupleDTO<BigDecimal,BigDecimal> openning(DpWmsDataDTO dto);

    List<AdsWmsInventoryVO> endingData(DpWmsDataDTO dto);

    Page<AdsWmsInventoryVO> dataPage(DpWmsDataDTO dto);
}
