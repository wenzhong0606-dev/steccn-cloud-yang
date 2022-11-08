package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.domain.QtReportGroup;
import com.steccn.qms.dto.QtReportGroupDTO;
import com.steccn.qms.service.QtReportGroupService;
import com.steccn.qms.mapper.QtReportGroupMapper;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.vo.QtReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
* @author 86178
* @description 针对表【qt_report_sort】的数据库操作Service实现
* @createDate 2022-11-08 11:29:13
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class QtReportGroupServiceImpl extends ServiceImpl<QtReportGroupMapper, QtReportGroup>
    implements QtReportGroupService {
        private final QtReportService qtReportService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtReportGroupDTO dto) {
        QtReportGroup reportGroup = new QtReportGroup();
        BeanUtils.copyProperties(dto, reportGroup);
        QtReportGroup qtReportGroup = getOne(new LambdaQueryWrapper<QtReportGroup>().eq(QtReportGroup::getReportGroupName, dto.getReportGroupName()));
        Assert.isNull(qtReportGroup, "该报表卡片类型已存在,请勿重复创建!");
        this.save(reportGroup);
        return Boolean.TRUE;
    }

    @Override
    public List<QtReportVO> getQtReportList(Integer id) {
        List<QtReportVO> qtReportList = qtReportService.getQtReportList(id);
        return qtReportList;
    }
}




