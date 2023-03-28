package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.controller.QtImproveRecordApproveDTO;
import com.steccn.qms.domain.QtImproveStepMan;
import com.steccn.qms.dto.QtImproveRecordDisposeDTO;

/**
* @author mengle
* @description 针对表【Qt_Improve_Step_Man(，整改审批人，抄送人)】的数据库操作Service
* @createDate 2023-03-23 10:16:53
*/
public interface QtImproveStepManService extends IService<QtImproveStepMan> {

    Boolean saveMan(QtImproveRecordApproveDTO dto);
}
