package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtCheckTaskDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.qms.vo.QtSubtableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_check_task_detail(巡检任务详细信息表)】的数据库操作Mapper
 * @createDate 2022-09-02 08:54:09
 * @Entity com.steccn.qms.domain.QtCheckTaskDetail
 */
@Mapper
public interface QtCheckTaskDetailMapper extends BaseMapper<QtCheckTaskDetail> {

    /**
     * 获取巡检任务详情
     *
     * @param taskIds
     * @return
     */
    List<QtSubtableVO> listByTaskId(@Param("taskIds") Set<Long> taskIds);
}




