package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsTransferSlipDTO;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.vo.WmsTransferSlipVO;

/**
* @author W10
* @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsTransferSlipService extends IService<WmsTransferSlip> {

    /**
     * 调拨单分页内容
     * @param dto
     * @return
     */
    Page<WmsTransferSlipVO> pageList(WmsTransferSlipPageDTO dto);

    /**
     * 调拨单新增
     * @param dto
     * @return
     */
    Boolean add(WmsTransferSlipDTO dto);

    /**
     * 调拨单编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsTransferSlipDTO dto);

    /**
     * 调拨单确认
     * @param id
     * @return
     */
    Boolean affirm(Long id);

    /**
     * 调拨单删除
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 调拨单详情
     * @param id
     * @return
     */
    WmsTransferSlipVO get(Long id);

    /**
     * 同步erp
     * @param id
     * @return
     */
    Boolean linkErp(Long id);
}
