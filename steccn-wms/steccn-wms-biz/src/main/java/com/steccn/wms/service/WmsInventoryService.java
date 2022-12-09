package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.dto.WmsInventoryPageDTO;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_inventory(仓储库存)】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsInventoryService extends IService<WmsInventory> {


    /**
     * 库存分页列表
     * @param dto
     * @return
     */
    Page<WmsInventoryVO> pageList(WmsInventoryPageDTO dto);

    /**
     * 调整占用库存信息
     * @param infoList
     * @param state 0 增加占用库存  1:减少占用库存
     * @return
     */
    Boolean updateLockQuantity(List<WmsInventoryDTO> infoList, Integer state);
}
