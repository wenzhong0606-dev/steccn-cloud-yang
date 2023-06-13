package com.steccn.data.mapper;

import com.steccn.data.domain.QtDefectCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.vo.DwDefectCollectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_defect_collect】的数据库操作Mapper
 * @createDate 2022-12-16 13:23:55
 * @Entity com.steccn.data.domain.QtDefectCollect
 */
@Mapper
public interface QtDefectCollectMapper extends BaseMapper<QtDefectCollect> {

    /**
     * 根据开始时间，结束时间和产线 查询缺陷明细
     *
     * @param start   开始时间
     * @param end     结束时间
     * @param lineSet 产线集合
     * @return
     */
    List<DwDefectCollectVO> collectDetail(@Param("timeList") List<LocalDateTime> timeList);
}




