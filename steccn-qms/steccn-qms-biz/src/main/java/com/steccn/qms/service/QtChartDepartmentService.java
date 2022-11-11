package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.vo.QtDefectChartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门分析驾驶舱概览
 */
public interface QtChartDepartmentService {

    public QtDefectChartVO getChartDepartmentInfo(QtChatDepartmentDTO qtChatDepartmentDTO);

    public QtDefectChartVO getChartDepartmentInfoSJG(QtChatDepartmentDTO qtChatDepartmentDTO);


}
