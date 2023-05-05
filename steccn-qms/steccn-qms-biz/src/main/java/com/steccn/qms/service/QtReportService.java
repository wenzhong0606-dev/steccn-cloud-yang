package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.qms.domain.QtReport;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author 86178
* @description 针对表【qt_report】的数据库操作Service
* @createDate 2022-11-01 16:42:01
*/
public interface QtReportService extends IService<QtReport> {

    /**
     * 新建报表任务
     * @param dto
     * @return
     */
    Integer add(QtReportDTO dto);

    /**
     * 根据物料id以及检验类型选择检验分组
     * @param materialId
     * @param checkType
     * @return
     */
   List<QtCheckGroupVO> getGroupIdList(Long materialId, Integer checkType);

    /**
     * 根据检验分组id选择检验项
     * @param groupid
     * @return
     */
    List<QtCheckItemSPCVO> getQtCheckItemList(Integer groupid);

    /**
     * 根据id删除spc控制图卡片
     * @param id
     * @return
     */
    Boolean reportDelete(Integer id);

    /**
     * 根据id查询Spc控制图卡片
     * @param id
     * @return
     */
    QtReportVO getQtReport(Integer id);

    /**
     * 编辑SPC报表
     * @param qtReportDTO
     * @return
     */
    QtReportVO edit(QtReportDTO qtReportDTO);

    /**
     * 明细表分页
     * @param dto
     * @return
     */
    Page<QtSpcReportDetailVO> pageList(QtSpcReportDetailPageDTO dto);

    /**
     * 根据Spc卡片类型查询卡片列表
     * @param reportCardType
     * @return
     */
    List<QtReportVO> getQtReportVOListByType(String reportCardType);

    /**
     * 根据检验项的id返回前端标准值、上偏差、下偏差,单位
     * @param groupId
     * @param itemId
     * @return
     */
    QtCheckItemUnitVO getQtCheckItemUnitVO(Integer groupId,Integer itemId);

   /**
    * 根据分组id查询报表卡片列表
    * @param reportGroupId
    * @return
    */
   List<QtReportChartVO> getQtReportList(Integer reportGroupId);

    /**
     * 根据分组id批量删除卡片
     * @param id
     * @return
     */
    Boolean deleteByGroupId(Integer id);

    /**
     * 根据报表id查询图表详情
     * @param id
     * @return
     */
    QtReportChartVO getQtReportChartVO(Integer id);

    /**
     * 下载spc报表
     * @param response
     * @param dto
     */
    void downloadSPC(HttpServletResponse response, QtReportExcelDTO dto) ;


    /**
     * spc告警
     * @param startTime 开始时间
     * @param endTime  结束时间
     * @param granularity 粒度:小时、天、周、月、季度、年
     * @return
     */
    Boolean warnSPC(LocalDateTime startTime, LocalDateTime endTime, String granularity);


    /**
     * 预警连续上升或者下降 -- 依赖时间区间有几段
     * @param list 时间区间
     * @param granularity 粒度
     * @return
     */
    Boolean warnSerialUpOrDown(List<LocalDateTime> list, String granularity);
}
