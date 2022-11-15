package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        List<QtCheckTaskCopy> qtCheckTaskCopies = list(new QueryWrapper<QtCheckTaskCopy>()
                .select("task_id ,sum(copy) as copy , any_value(id) as id , any_value(sample_num) as sample_num," +
                        " any_value(license_plate) as license_plate ,any_value(analyst) as analyst , any_value(analyst_name) analyst_name ," +
                        " any_value(sampling_time) as sampling_time ,any_value(analyst_time) as analyst_time ,any_value(ingredient_num) as ingredient_num ," +
                        "any_value(create_by) as create_by, any_value(create_name) as create_name , any_value(update_by) as update_by ," +
                        "any_value(update_name) update_name , any_value(del_flag) as del_flag , any_value(create_time) as create_time , " +
                        "any_value(update_time) as update_time")
                .lambda().groupBy(QtCheckTaskCopy::getTaskId).orderByAsc(QtCheckTaskCopy::getSamplingTime));
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




