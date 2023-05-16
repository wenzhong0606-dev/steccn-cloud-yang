package com.steccn.qms.service;

import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.dto.QtCheckTaskDtDTO;
import com.steccn.qms.vo.QtCheckTaskDtVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数字孪生service
 *
 * @author AndrewLuo
 * @date 2023/05/16
 */
public interface QtCheckTaskDtService {
    /**
     * 获取所有的基片产线
     * @return
     */
    List<SysOfficeVO> getOfficeList();

    /**
     * 获取数字孪生数据
     * @param dto
     * @return
     */
    Map<String,List<QtCheckTaskDtVO>> getDtData(QtCheckTaskDtDTO dto);
}
