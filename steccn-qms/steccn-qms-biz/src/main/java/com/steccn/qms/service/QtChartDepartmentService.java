package com.steccn.qms.service;

import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.vo.QtDefectChartVO;

import java.util.List;

/**
 * 部门分析驾驶舱概览
 */
public interface QtChartDepartmentService {

    public List<QtDefectChartVO> getChartDepartmentInfo(QtChatDepartmentDTO qtChatDepartmentDTO);


}
