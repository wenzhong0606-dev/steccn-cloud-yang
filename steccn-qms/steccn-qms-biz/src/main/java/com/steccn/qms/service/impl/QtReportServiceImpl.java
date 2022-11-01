package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.domain.QtReport;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.mapper.QtReportMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author 86178
* @description 针对表【qt_report】的数据库操作Service实现
* @createDate 2022-11-01 16:42:01
*/
@Service
public class QtReportServiceImpl extends ServiceImpl<QtReportMapper, QtReport>
    implements QtReportService{

    @Override
    public Integer add(QtAddReportDTO dto) {
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto,qtReport);
        this.save(qtReport);
        return qtReport.getId();
    }
}




