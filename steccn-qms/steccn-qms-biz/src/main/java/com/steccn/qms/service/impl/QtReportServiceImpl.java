package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.domain.QtReport;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.QtMaterialService;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.mapper.QtReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
* @author 86178
* @description 针对表【qt_report】的数据库操作Service实现
* @createDate 2022-11-01 16:42:01
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class QtReportServiceImpl extends ServiceImpl<QtReportMapper, QtReport>
    implements QtReportService{

    @Override
    public Integer add(QtAddReportDTO dto) {
        Assert.isNull(dto,"新建报表不能为空");
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto,qtReport);
        QtReport report = getById(new LambdaQueryWrapper<QtReport>().eq(QtReport::getName, dto.getName()));
        Assert.isNull(report,"该报表卡片名称已存在");
        this.save(qtReport);
        return qtReport.getId();
    }
}




