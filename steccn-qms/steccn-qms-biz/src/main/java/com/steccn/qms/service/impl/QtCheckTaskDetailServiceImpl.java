package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.qms.domain.QtCheckTaskCopy;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.service.QtCheckTaskCopyService;
import com.steccn.qms.service.QtCheckTaskDetailService;
import com.steccn.qms.mapper.QtCheckTaskDetailMapper;
import com.steccn.qms.vo.QtIQCAcceptabilityCopyVO;
import com.steccn.qms.vo.QtSubtableVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qt_check_task_detail(巡检任务详细信息表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
public class QtCheckTaskDetailServiceImpl extends ServiceImpl<QtCheckTaskDetailMapper, QtCheckTaskDetail>
        implements QtCheckTaskDetailService {

    private final QtCheckTaskCopyService qtCheckTaskCopyService;

    @Override
    public Map<Long, List<QtIQCAcceptabilityCopyVO>> listByTaskId(Set<Long> taskIds) {
        Map<Long, List<QtIQCAcceptabilityCopyVO>> resMap = Maps.newHashMap();
        //1.自定义sql获取动态表格内容
        List<QtSubtableVO> list = this.baseMapper.listByTaskId(taskIds);
        //2.处理数据
        if (CollUtil.isNotEmpty(list)) {
            //根据任务分组
            Map<Long, List<QtSubtableVO>> map = list.stream().collect(Collectors.groupingBy(QtSubtableVO::getTaskId));
            for (Map.Entry<Long, List<QtSubtableVO>> entry : map.entrySet()) {
                List<QtIQCAcceptabilityCopyVO> copyVOList = Lists.newArrayList();
                Map<String, List<QtSubtableVO>> tableMap = entry.getValue().stream().collect(Collectors.groupingBy(QtSubtableVO::getSampleNum));
                List<QtSubtableVO> voList = Lists.newArrayList();
                entry.getValue().forEach(
                        qtSubtableVO -> {
                            List<QtSubtableVO> orDefault = tableMap.getOrDefault(qtSubtableVO.getSampleNum(), voList);
                            QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
                            qtIQCAcceptabilityCopyVO.setSampleNum(qtSubtableVO.getSampleNum());
                            qtIQCAcceptabilityCopyVO.setLicensePlate(qtSubtableVO.getLicensePlate());
                            qtIQCAcceptabilityCopyVO.setList(orDefault);
                            copyVOList.add(qtIQCAcceptabilityCopyVO);
                        }
                );
                resMap.put(entry.getKey(), copyVOList);
            }
        }
        //3.返回数据
        return resMap;
    }
}




