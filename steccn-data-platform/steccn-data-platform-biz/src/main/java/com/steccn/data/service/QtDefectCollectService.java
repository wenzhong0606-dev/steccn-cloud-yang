package com.steccn.data.service;

import com.steccn.data.domain.QtDefectCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.data.vo.DwDefectCollectVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【qt_defect_collect】的数据库操作Service
* @createDate 2022-12-16 13:23:55
*/
public interface QtDefectCollectService extends IService<QtDefectCollect> {

    /**
     * 根据产线集合获取指定日期的缺陷数量
     * @param start 开始日期
     * @param end 结束日期
     * @param lineSet 产线集合
     * @return list
     */
    List<DwDefectCollectVO> collectDetail(LocalDateTime start, LocalDateTime end, Set<String> lineSet);
}
