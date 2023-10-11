package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.pms.vo.WmsInventoryRecordVO;
import com.steccn.wms.domain.WmsInventoryRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsInventoryRecordDTO;
import com.steccn.wms.dto.WmsInventoryRecordPageDTO;

import java.util.List;

/**
* @author 孟乐
* @description 针对表【wms_inventory_record(库存流水记录表)】的数据库操作Service
* @createDate 2023-10-11 14:17:05
*/
public interface WmsInventoryRecordService extends IService<WmsInventoryRecord> {


    /**
     * 库存流水记录列表
     *
     * @param dto
     * @return
     */
    Page<WmsInventoryRecordVO> pageList(WmsInventoryRecordPageDTO dto);

}