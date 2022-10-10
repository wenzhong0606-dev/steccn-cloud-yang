package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.vo.BarcodePrintRecordVO;

/**
* @author yequan
* @description 针对表【pms_barcode_print(条码打印)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsBarcodePrintService extends IService<PmsBarcodePrint> {

    /**
     * 打印条码
     * @param dto dto
     * @return 是否成功
     */
    Boolean print(BarcodePrintDTO dto);

    /**
     * 打印记录
     * @param dto dto
     * @return 分页数据
     */
    Page<BarcodePrintRecordVO> printRecord(BarcodePrintRecordDTO dto);
}
