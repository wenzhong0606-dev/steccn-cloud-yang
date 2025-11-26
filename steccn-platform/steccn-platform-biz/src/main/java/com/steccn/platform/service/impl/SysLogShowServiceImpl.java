package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.domain.SysLogShow;
import com.steccn.platform.dto.SysLogShowPageDTO;
import com.steccn.platform.mapper.SysLogMapper;
import com.steccn.platform.service.SysLogService;
import com.steccn.platform.service.SysLogShowService;
import com.steccn.platform.mapper.SysLogShowMapper;
import com.steccn.platform.vo.SysLogShowPageVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_log_show】的数据库操作Service实现
 * @createDate 2024-03-27 15:39:38
 */
@Service
@RequiredArgsConstructor
public class SysLogShowServiceImpl extends ServiceImpl<SysLogShowMapper, SysLogShow>
        implements SysLogShowService {
    private final SysLogMapper sysLogMapper;

    @Override
    public Page<SysLogShowPageVO> pageList(SysLogShowPageDTO dto) {
        LocalDateTime start = null, end = null;
        if (Objects.nonNull(dto.getStart())) {
            start = LocalDateUtil.getMorning(dto.getStart());
        }
        if (Objects.nonNull(dto.getEnd())) {
            end = LocalDateUtil.getMorning(dto.getEnd());
        }
        Page<SysLogShowPageVO> resPage = new Page<>();
//        Page<SysLogShow> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<SysLogShow>()
//                .eq(Objects.nonNull(dto.getUserId()), SysLogShow::getCreateBy, dto.getUserId())
//                .ge(Objects.nonNull(dto.getStart()), SysLogShow::getCreateTime,start)
//                .le(Objects.nonNull(dto.getEnd()), SysLogShow::getCreateTime, end)
//                .orderByDesc(SysLogShow::getId));
        Page<SysLog> page = sysLogMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<SysLog>()
                .eq(Objects.nonNull(dto.getUserId()), SysLog::getCreateBy, dto.getUserId())
                .ge(Objects.nonNull(dto.getStart()), SysLog::getCreateTime,start)
                .le(Objects.nonNull(dto.getEnd()), SysLog::getCreateTime, end)
                .orderByDesc(SysLog::getId));
        BeanUtils.copyProperties(page, resPage);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<SysLogShowPageVO> recordList = BeanUtil.copyToList(page.getRecords(), SysLogShowPageVO.class);
            resPage.setRecords(recordList);
        }
        return resPage;
    }

    @Override
    public List<TupleDTO<Long, String>> userList(String name) {
        List<TupleDTO<Long, String>> resList = Lists.newArrayList();
//        List<SysLogShow> list = list(new QueryWrapper<SysLogShow>()
//                .select("distinct create_by , create_name")
//                .lambda()
//                .like(StrUtil.isNotBlank(name), SysLogShow::getCreateName, name)
//                .ne(SysLogShow::getCreateBy, 0));
        List<SysLog> list = sysLogMapper.selectList(new QueryWrapper<SysLog>()
                .select("distinct create_by , create_name")
                .lambda()
                .like(StrUtil.isNotBlank(name), SysLog::getCreateName, name)
                .ne(SysLog::getCreateBy, 0));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(ele -> {
                resList.add(new TupleDTO<>(ele.getCreateBy(), ele.getCreateName()));
            });
            Comparator<Object> cmp = Collator.getInstance(Locale.CHINA);
            resList.sort((o1, o2) -> cmp.compare(o1.getField2(), o2.getField2()));
        }
        return resList;
    }
}




