package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.data.domain.QtDefectCollect;
import com.steccn.data.domain.QtDefectCollectDetail;
import com.steccn.data.service.QtDefectCollectDetailService;
import com.steccn.data.service.QtDefectCollectService;
import com.steccn.data.mapper.QtDefectCollectMapper;
import com.steccn.data.vo.DwDefectCollectVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qt_defect_collect】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
@RequiredArgsConstructor
public class QtDefectCollectServiceImpl extends ServiceImpl<QtDefectCollectMapper, QtDefectCollect>
        implements QtDefectCollectService {

    private final QtDefectCollectDetailService qtDefectCollectDetailService;

    @Override
    public List<DwDefectCollectVO> collectDetail(LocalDateTime start, LocalDateTime end, Set<String> lineSet) {
        List<DwDefectCollectVO> resList = Lists.newArrayList();
        if (Objects.nonNull(start) && Objects.nonNull(end) && CollUtil.isNotEmpty(lineSet)) {
            //去主表根据产线和日期查询
            List<QtDefectCollect> dbList = list(new LambdaQueryWrapper<QtDefectCollect>()
                    .ge(QtDefectCollect::getProductTime, start)
                    .lt(QtDefectCollect::getProductTime, end)
                    .in(QtDefectCollect::getLine, lineSet));
            if (CollUtil.isNotEmpty(dbList)) {
                //获取所有的主表id集合
                Set<Long> mainIdSet = dbList.stream().map(QtDefectCollect::getId).collect(Collectors.toSet());
                Map<Long, QtDefectCollect> collectMap = Maps.newHashMap();
                dbList.forEach(ele -> collectMap.put(ele.getId(), ele));
                //根据主表id查询子表
                List<QtDefectCollectDetail> detailList = qtDefectCollectDetailService.list(new LambdaQueryWrapper<QtDefectCollectDetail>().in(QtDefectCollectDetail::getPushId, mainIdSet));
                if (CollUtil.isNotEmpty(detailList)) {
                    for (QtDefectCollectDetail ele : detailList) {
                        DwDefectCollectVO vo = new DwDefectCollectVO();
                        BeanUtils.copyProperties(ele, vo);
                        QtDefectCollect qtDefectCollect = collectMap.get(ele.getPushId());
                        if (Objects.nonNull(qtDefectCollect)) {
                            vo.setProductTime(collectMap.get(ele.getPushId()).getProductTime());
                            resList.add(vo);
                        }
                    }
                }
            }
        }
        return resList;
    }
}




