package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.dto.QtCheckPlanPageDTO;
import com.steccn.qms.vo.QtCheckPlanVO;
import com.steccn.wms.domain.WmsWeighbridgeInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsWeighbridgeGerDTO;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import com.steccn.wms.vo.WmsWeighbridgeInfoPageVO;
import com.steccn.wms.vo.WmsWeighbridgeInfoVO;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Admin
* @description 针对表【wms_weighbridge_info(地磅信息)】的数据库操作Service
* @createDate 2024-05-16 14:30:55
*/
public interface WmsWeighbridgeInfoService extends IService<WmsWeighbridgeInfo> {

    /**
     * 保存地磅过磅记录
     * @param dto
     * @return
     */
    Boolean saveInfo(WmsWeighbridgeInfoDTO dto);

    /**
     * 分页VO
     * @param dto
     * @return
     */
    WmsWeighbridgeInfoPageVO<WmsWeighbridgeInfoVO> pageList(WmsWeighbridgeInfoDTO dto);

    List<String> companyName();

    List<String> materialName();

    /**
     * 生成采购入库单
     * @param dto
     * @return
     */
    Boolean generateList(WmsWeighbridgeGerDTO dto);

    /**
     * 修改扣重
     * @param dto
     * @return
     */
    Boolean changeDeduct(WmsWeighbridgeInfoDTO dto);

}
