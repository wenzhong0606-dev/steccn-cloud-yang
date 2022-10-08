package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsReportWorkServiceImpl extends ServiceImpl<PmsReportWorkMapper, PmsReportWork>
        implements PmsReportWorkService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;

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
        List<ProcessReportWorkPageVO> records = page.getRecords();
        List<Long> unitList = new ArrayList<>(records.size());
        Set<Integer> organIdSet = Sets.newHashSet();
        records.forEach(ele -> {
            if (Objects.nonNull(ele.getUnitId())) {
                unitList.add(ele.getUnitId());
            }
            if (Objects.nonNull(ele.getStationId())) {
                organIdSet.add(ele.getStationId());
            }
        });
        Map<Long, SysDictVO> sysDictMap = FeignOptional.of(remoteDictFeignApi.listById(unitList)).get();
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(remoteOfficeFeign.listById(organIdSet)).get();
        records.forEach(ele -> {
            ele.setUnitName(sysDictMap.getOrDefault(ele.getUnitId(), new SysDictVO()).getName());
            ele.setStationName(sysOfficeVOMap.getOrDefault(ele.getStationId(), new SysOfficeVO()).getOfficeName());
        });
        return page;
    }
}