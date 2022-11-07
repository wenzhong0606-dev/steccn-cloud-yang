package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtCheckTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.*;
import io.swagger.models.auth.In;

import java.util.List;

/**
* @author AndrewLuo
* @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckTaskService extends IService<QtCheckTask> {
    /**
     * 新增巡检任务
     * @param dto
     * @return
     */
    Long add(QtAddCheckTaskDTO dto);

    /**
     * 巡检列表Vo
     * @param dto
     * @return
     */
    List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto);

    /**
     * 巡检详情
     * @param id
     * @return
     */
    QtCheckTaskDetailVO taskDetail(Long id,Boolean isPc);

    /**
     * 修改任务状态
     * @param dto 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
     * @return
     */
    Boolean updateStatus(QtCheckTaskStatusDTO dto);

    /**
     * 加入值
     * @param dto
     * @return
     */
    Boolean itemValue(QtCheckTaskSubmitDTO dto);

    /**
     * pc加入值
     * @param dto
     * @return
     */
    Boolean pcItemValue(QtPcCheckTaskSubmitDTO dto);

    /**
     * 完成
     * @param dto
     * @return
     */
    Boolean finished(QtCheckResultDTO dto);

    /**
     * 复检任务
     * @param dto
     * @return
     */
    Long retask(QtReTaskDTO dto);

    /**
     * 通过类型查询任务
     * @return
     */
    List<QtCheckTaskTypeCountVO> countTaskByType();

    Page<QtCheckTaskListVO> pageList(QtCheckTaskListDTO dto);

    QtCheckTaskListVO productOrderInfo(Integer id);

    QtProduceOrderLineVO selectProductOrder(Integer id);

    Page<QtCheckProduceTaskVO> pageProductOrder(QtCheckaProductTaskListDTO dto);


    /**
     * 深加工/基片巡检合格率趋势
     * @param dto
     * @return
     */
    QtChartAcceptabilityVO getSubOrDeepProcess(QtSubOrDeepAcceptabilityDTO dto);
}
