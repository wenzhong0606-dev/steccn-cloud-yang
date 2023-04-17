package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtImproveRecord;
import com.steccn.qms.domain.QtImproveRecordDetail;

import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【qms_improve_record_detail(整改记录详情)】的数据库操作Service
* @createDate 2023-03-15 10:16:54
*/
public interface QtImproveRecordDetailService extends IService<QtImproveRecordDetail> {

    /**
     * 获取当前状态的详情
     * @param recordId  记录id
     * @param status 记录状态
     * @return
     */
    QtImproveRecordDetail getDetail(Long recordId,Integer status);

}
