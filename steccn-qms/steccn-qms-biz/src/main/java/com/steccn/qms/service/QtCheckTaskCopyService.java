package com.steccn.qms.service;

import com.steccn.qms.domain.QtCheckTaskCopy;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.vo.QtCheckTaskCopyVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author 86178
* @description 针对表【qt_check_task_copy】的数据库操作Service
* @createDate 2022-11-10 16:25:47
*/
public interface QtCheckTaskCopyService extends IService<QtCheckTaskCopy> {

    /**
     * 获取每个样本取样时间的最早时间,返回任务id以及取样时间
     * @return
     */
    List<QtCheckTaskCopyVO> getLocalDateList();

    /**
     * 根据taskId查询最小的取样日期
     * @param taskIdSet 任务id集合
     * @return map
     */
    Map<Long, LocalDateTime> minSamplingTimeMap(Set<Long> taskIdSet);
}
