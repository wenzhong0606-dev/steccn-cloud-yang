package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsReportWorkServiceImpl extends ServiceImpl<PmsReportWorkMapper, PmsReportWork>
        implements PmsReportWorkService {

    @Override
    public Page<ProcessReportWorkPageVO> pageList(ProcessReportWorkPageDTO dto) {
        //1.将日期拼接最大时间和最小时间
        //创建时间-开始
        if (Objects.nonNull(dto.getCreateDtStart())) {
            dto.setCreateTimeStart(LocalDateTime.of(dto.getCreateDtStart(), LocalTime.MIN));
        }
        //创建时间-结束
        if (Objects.nonNull(dto.getCreateDtEnd())) {
            dto.setCreateTimeEnd(LocalDateTime.of(dto.getCreateDtEnd(), LocalTime.MAX));
        }
        //开始时间-开始
        if (Objects.nonNull(dto.getStartDtStart())) {
            dto.setStartTimeStart(LocalDateTime.of(dto.getStartDtStart(), LocalTime.MIN));
        }
        //开始时间-结束
        if (Objects.nonNull(dto.getStartDtEnd())) {
            dto.setStartTimeEnd(LocalDateTime.of(dto.getStartDtEnd(), LocalTime.MAX));
        }
        //结束时间-开始
        if (Objects.nonNull(dto.getEndDtStart())) {
            dto.setEndTimeStart(LocalDateTime.of(dto.getEndDtStart(), LocalTime.MIN));
        }
        //结束时间-结束
        if (Objects.nonNull(dto.getEndDtEnd())) {
            dto.setEndTimeEnd(LocalDateTime.of(dto.getEndDtEnd(), LocalTime.MAX));
        }

        //2.自定义sql查询
        Page<ProcessReportWorkPageVO> page = this.baseMapper.processReportPage(new Page(dto.getCurrent(), dto.getSize()), dto);

        //3.将id映射成name

        return page;
    }
}