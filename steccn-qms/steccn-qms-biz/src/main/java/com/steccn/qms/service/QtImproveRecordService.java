package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.qms.domain.QtImproveRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtImproveRecordDTO;
import com.steccn.qms.dto.QtImproveRecordPageDTO;
import com.steccn.qms.vo.QtImproveRecordVO;

/**
* @author yequan
* @description 针对表【qms_improve_record(整改记录)】的数据库操作Service
* @createDate 2023-03-15 10:16:54
*/
public interface QtImproveRecordService extends IService<QtImproveRecord> {


    /**
     * 整改记录列表
     * @param dto
     * @return
     */
    Page<QtImproveRecordVO> listPage(QtImproveRecordPageDTO dto);

    /**
     * 新增整改记录
     * @param dto
     * @return
     */
    Boolean add(QtImproveRecordDTO dto);
}
