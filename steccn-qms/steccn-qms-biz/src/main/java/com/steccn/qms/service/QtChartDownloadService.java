package com.steccn.qms.service;

import com.steccn.qms.dto.QtChartYieldDownloadDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
}
