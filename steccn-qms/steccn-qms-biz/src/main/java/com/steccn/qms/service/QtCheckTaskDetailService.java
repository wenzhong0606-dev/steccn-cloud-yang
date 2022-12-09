package com.steccn.qms.service;

import com.steccn.qms.domain.QtCheckTaskDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import com.steccn.qms.vo.QtIQCAcceptabilityCopyVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【qt_check_task_detail(巡检任务详细信息表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckTaskDetailService extends IService<QtCheckTaskDetail> {

    /**
     * 根据任务id 获取任务的详情信息
     * @param taskIds
     * @return
     */
    Map<Long, List<QtIQCAcceptabilityCopyVO>> listByTaskId(Set<Long> taskIds);

    /**
     * 根据班次日期获取数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<QtCheckTaskDetailItemMaterialVO> getByClassTime(LocalDate startDate, LocalDate endDate);
}
