package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTaskCopy;
import com.steccn.qms.service.QtCheckTaskCopyService;
import com.steccn.qms.mapper.QtCheckTaskCopyMapper;
import com.steccn.qms.vo.QtCheckTaskCopyVO;
import com.steccn.qms.vo.QtCheckTaskListVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
* @author 86178
* @description 针对表【qt_check_task_copy】的数据库操作Service实现
* @createDate 2022-11-10 16:25:47
*/
@Service
public class QtCheckTaskCopyServiceImpl extends ServiceImpl<QtCheckTaskCopyMapper, QtCheckTaskCopy>
    implements QtCheckTaskCopyService{

    @Override
    public List<QtCheckTaskCopyVO> getLocalDateList() {
        List<QtCheckTaskCopy> qtCheckTaskCopies = list(new LambdaQueryWrapper<QtCheckTaskCopy>().orderByAsc(QtCheckTaskCopy::getSamplingTime).groupBy(QtCheckTaskCopy::getTaskId));
        List<QtCheckTaskCopy> collect = qtCheckTaskCopies.stream().collect(
                Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(p -> p.getTaskId()))), ArrayList::new)
        );
        List<QtCheckTaskCopyVO> qtCheckTaskCopyVOS = Lists.newArrayList();
        collect.stream().forEach(p->{
            QtCheckTaskCopyVO qtCheckTaskCopyVO = new QtCheckTaskCopyVO();
            BeanUtils.copyProperties(p,qtCheckTaskCopyVO);
            qtCheckTaskCopyVOS.add(qtCheckTaskCopyVO);
        });
        return qtCheckTaskCopyVOS;
    }
}




