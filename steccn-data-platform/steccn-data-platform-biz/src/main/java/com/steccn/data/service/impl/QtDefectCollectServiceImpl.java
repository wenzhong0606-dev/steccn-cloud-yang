package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.domain.QtDefectCollect;
import com.steccn.data.domain.QtDefectCollectDetail;
import com.steccn.data.service.QtDefectCollectDetailService;
import com.steccn.data.service.QtDefectCollectService;
import com.steccn.data.mapper.QtDefectCollectMapper;
import com.steccn.data.vo.DwDefectCollectVO;
import com.steccn.qms.util.ShardTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
            //根据时间分片
            List<LocalDateTime> shardList = ShardTimeUtil.shard(start, end, 3600);
            resList = this.baseMapper.collectDetail(shardList);
            resList = resList.stream().filter(v-> lineSet.contains(v.getLine())).collect(Collectors.toList());
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }
}




