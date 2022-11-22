package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.qms.domain.QtCheckTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.*;
import io.swagger.models.auth.In;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author AndrewLuo
* @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckTaskService extends IService<QtCheckTask> {
    /**
     * 新增巡检任务
     *
     * @param dto
     * @return
     */
    Long add(QtAddCheckTaskDTO dto);

    /**
     * 巡检列表Vo
     *
     * @param dto
     * @return
     */
    List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto);

    /**
     * 巡检详情
     *
     * @param id
     * @return
     */
    QtCheckTaskDetailVO taskDetail(Long id, Boolean isPc);

    /**
     * 修改任务状态
     *
     * @param dto 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
     * @return
     */
    Boolean updateStatus(QtCheckTaskStatusDTO dto);

    /**
     * 加入值
     *
     * @param dto
     * @return
     */
    Boolean itemValue(QtCheckTaskSubmitDTO dto);

    /**
     * pc加入值
     *
     * @param dto
     * @return
     */
    Boolean pcItemValue(QtPcCheckTaskSubmitDTO dto);

    /**
     * 完成
     *
     * @param dto
     * @return
     */
    Boolean finished(QtCheckResultDTO dto);

    /**
     * 复检任务
     *
     * @param dto
     * @return
     */
    Boolean retask(QtReTaskDTO dto);

    /**
     * 通过类型查询任务
     *
     * @return
     */
    List<QtCheckTaskTypeCountVO> countTaskByType();

    Page<QtCheckTaskListVO> pageList(QtCheckTaskListDTO dto);

    QtProduceOrderLineVO productOrderInfo(String code);

    QtProduceOrderLineVO selectProductOrder(Integer id);

    Page<QtCheckProduceTaskVO> pageProductOrder(QtCheckaProductTaskListDTO dto);


    /**
     * 深加工/基片巡检合格率趋势明细分页
     *
     * @param dto
     * @return
     */
    Page<QtDeepOrSubAcceptabilityDetailVO> getQtDeepOrSubAcceptabilityDetailVO(String keyword, QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 深加工或基片合格率图表
     *
     * @param keyword 深加工 || 基片
     * @param dto     dto
     * @return
     */
    QtLineChartBiBarDTO getQtChartDeepOrSubBarVO(String keyword, QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 根据条件查询任务
     *
     * @param dto dto条件
     * @return list
     */
    List<QtCheckTask> conditionTaskList(QtCheckTaskConditionDTO dto);

    /**
     * 配料抽检合格率的明细分页
     *
     * @param dto
     * @return
     */
    Page<QtAccessoryAcceptabilityDetailVO> getQtAccessoryAcceptabilityDetailVO(QtAccessoryAcceptabilityDetailDTO dto);

    /**
     * 配料抽检合格率的柱状图
     *
     * @param dto
     * @return
     */
    QtChartBarVO getAccessoryChartDefectVO(QtAccessoryAcceptabilityDTO dto);

    /**
     * 基片化验抽检合格率趋势明细分页
     *
     * @param dto
     * @return
     */
    Page<QtCheckTaskReportVO> getQtSubLaboratoryAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 来料检合格率明细分页
     *
     * @param dto
     * @return
     */
    Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 来料检合格率柱状图表
     *
     * @param dto
     * @return
     */
    QtLineChartBiBarDTO accessoryChart(QtSubOrDeepAcceptabilityDTO dto);


    /**
     * 来料检合格率
     *
     * @param dto dto
     * @return 柱状图
     */
    QtLineChartBiBarDTO getQtIQCAcceptabilityChart(QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 下载配料抽检合格率
     *
     * @param dto
     * @param response
     */
    void downloadAccessoryAcceptability(AccessoryAcceptabilityDownDTO dto, HttpServletResponse response);

    /**
     * 下载基片化验抽检
     *
     * @param dto
     * @return
     */
    void downloadQtSubLaboratoryAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto, HttpServletResponse response);

    /**
     * 来料检下载
     *
     * @param dto
     * @param response
     */
    void downloadAcceptability(QtSubOrDeepAcceptabilityDTO dto, HttpServletResponse response);


}


