package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.qms.controller.QtImproveRecordApproveDTO;
import com.steccn.qms.domain.QtImproveRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
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


    /**
     * 编辑整改记录
     * @param dto
     * @return
     */
    Boolean edit(QtImproveRecordDTO dto);


    /**
     * 处理提交
     * @param dto
     * @return
     */
    Boolean dispose(QtImproveRecordDisposeDTO dto);


    /***
     * 退回
     * @param dto
     * @return
     */
    Boolean sendBack(QtImproveRecordSendBackDTO dto);


    /**
     * 获取整改记录详情
     * @param id
     * @return
     */
    QtImproveRecordVO get(Long id);

    /**
     * 中断
     * @param dto
     * @return
     */
    Boolean suspend(QtImproveRecordSuspendDTO dto);


    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Long id);


    /**
     * 提交审批
     * @param dto
     * @return
     */
    Boolean approve(QtImproveRecordApproveDTO dto);
}
