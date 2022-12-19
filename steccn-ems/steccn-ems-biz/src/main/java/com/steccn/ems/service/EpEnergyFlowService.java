package com.steccn.ems.service;

import com.steccn.common.vo.EpTreeGraphVO;
import com.steccn.ems.dto.EpEnergyFlowDTO;
import com.steccn.ems.dto.EpEnergyFlowLeaderDTO;
import com.steccn.ems.vo.EpConsumeDetailVO;

import javax.servlet.http.HttpServletResponse;

/**
 * 能源流向service
 *
 * @author AndrewLuo
 * @date 2022/12/12
 */
public interface EpEnergyFlowService {
    /**
     * 获取能源流向全量数据表详情DTO
     * @param dto
     * @return
     */
    EpConsumeDetailVO getDetails(EpEnergyFlowDTO dto);

    /**
     * 生成树状图表数据
     * @param dto
     * @return
     */
    EpTreeGraphVO generateCharts(EpEnergyFlowDTO dto);

    /**
     * 生成领导驾驶舱图表数据
     * @param dto
     * @return
     */
    EpTreeGraphVO generateLeaderCharts(EpEnergyFlowLeaderDTO dto);

    /**
     * 下载全量数据表
     * @param dto
     * @param response
     */
    void downloadFlow(EpEnergyFlowDTO dto, HttpServletResponse response);
}
