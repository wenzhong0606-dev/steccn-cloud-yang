package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.vo.PassRateOverViewVO;
import com.steccn.qms.vo.QtDefectChartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门分析驾驶舱概览
 */
public interface QtChartDepartmentService {

    //public QtDefectChartVO getChartDepartmentInfo(LocalDateTime selectDateTime);

    //public QtDefectChartVO getChartDepartmentInfoSJG(LocalDateTime selectDateTime);

    /**
     * 根据类型获取合格率
     *
     * @param type
     * @return
     */
    PassRateOverViewVO passRateOverview(Integer type, LocalDate selectDateTime);


     //QtChartDefectVO getPictureJPFB(QtChartParamDTO dto);
}
