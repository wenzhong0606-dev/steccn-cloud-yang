package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.dto.QtChartYieldDownloadDTO;
import com.steccn.qms.vo.QtCheckTaskListVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 图形下载excelService
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
public interface QtChartDownloadService {
    /**
     * 下载基片成品率
     */
    void downloadSubstrate(HttpServletResponse response, QtChartYieldDownloadDTO dto) throws IOException;

    /**
     * 下载深加工巡检合格率趋势
     * @param response
     */
    void downloadDeepProcess(HttpServletResponse response)throws IOException;

    /**
     * 下载深加工缺陷项类别分布
     */
    void downloadDefectSubstrate(HttpServletResponse response) throws IOException;

    /**
     * 下载基片损耗分部详情
     * @param response
     * @param dto
     */
    void downloadSubstrateLoss(HttpServletResponse response, QtChartYieldDownloadDTO dto) throws IOException;

    /**
     *
     * @param dto
     * @return
     */
    Page<ProducePassRateVO>  details(QtChartYieldDownloadDTO dto);

    /**
     * 获取成品率损耗分页
     * @param dto
     * @return
     */
    Page<QtChartSubDetailDTO>  lossDetails(QtChartYieldDownloadDTO dto);

    /**
     * 下载成品率详情
     * @param dto
     * @return
     */
    void  downloadLossDetails(QtChartYieldDownloadDTO dto,HttpServletResponse response) throws IOException ;
}
