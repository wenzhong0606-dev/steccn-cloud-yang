package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.domain.QtDefectCollect;
import com.steccn.data.mapper.QtDefectCollectMapper;
import com.steccn.data.service.QtDefectCollectService;
import com.steccn.data.vo.DwDefectCollectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_defect_collect】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDefectCollectServiceImpl extends ServiceImpl<QtDefectCollectMapper, QtDefectCollect>
        implements QtDefectCollectService {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);

    @Override
    public List<DwDefectCollectVO> collectDetail(LocalDateTime start, LocalDateTime end, Set<String> lineSet) {
        List<DwDefectCollectVO> resList = null;
        if (Objects.nonNull(start) && Objects.nonNull(end) && LocalDateUtil.gt(end, start) && CollUtil.isNotEmpty(lineSet)) {
            String startStr = start.format(DATE_TIME_FORMATTER);
            String endStr = end.format(DATE_TIME_FORMATTER);
            Set<Integer> newLineSet = Sets.newHashSet();
            lineSet.forEach(ele -> {
                try {
                    newLineSet.add(Integer.parseInt(ele));
                } catch (Exception e) {
                    log.error("转换异常");
                }
            });
            long l = System.currentTimeMillis();
            resList = this.baseMapper.collectDetail(startStr, endStr, newLineSet);
            log.info("耗费时间 -> {} s", (System.currentTimeMillis() - l)/1000.0);

        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }
}




