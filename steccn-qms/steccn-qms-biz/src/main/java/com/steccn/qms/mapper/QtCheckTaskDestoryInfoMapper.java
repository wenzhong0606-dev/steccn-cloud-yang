package com.steccn.qms.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.qms.domain.QtCheckTaskDestoryInfo;
import com.steccn.qms.dto.QtDestoryInfoFallDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 86187
* @description 针对表【qt_check_task_destory_info】的数据库操作Mapper
* @createDate 2022-11-03 10:29:18
* @Entity
*/
@Mapper
public interface QtCheckTaskDestoryInfoMapper extends BaseMapper<QtCheckTaskDestoryInfo> {

    /**
     * 落板记录集合
     * @param productLineList 产线集合
     * @param start 开始时间
     * @param end 结束时间
     * @return list
     */
    List<QtDestoryInfoFallDTO> falling(@Param("lineList") List<Integer> productLineList,@Param("start") LocalDateTime start,@Param("end") LocalDateTime end);
}




