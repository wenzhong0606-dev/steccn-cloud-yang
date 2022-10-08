package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.ProcessReportWorkHourDTO;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.dto.ProcessReportWorkSubmitDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PmsReportWorkHourService reportWorkHourService;
    private final PmsProduceOrderService produceOrderService;
    private final PmsProduceOrderCraftService produceOrderCraftService;
    private final PmsWorkOrderBatchService workOrderBatchService;

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
        Set<Long> batchSet = Sets.newHashSet();
        records.forEach(ele -> {
            if (Objects.nonNull(ele.getUnitId())) {
                unitList.add(ele.getUnitId());
            }
            if (Objects.nonNull(ele.getStationId())) {
                organIdSet.add(ele.getStationId());
            }
        });
        Map<Long, SysDictVO> sysDictMap = FeignOptional.of(remoteDictFeignApi.listById(unitList)).get();
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listById(organIdSet)).get();
        records.forEach(ele -> {
            ele.setUnitName(sysDictMap.getOrDefault(ele.getUnitId(), new SysDictVO()).getName());
            ele.setStationName(sysOfficeMap.getOrDefault(ele.getStationId(), new SysOfficeVO()).getOfficeName());
        });
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitReportWork(ProcessReportWorkSubmitDTO dto) {
        //检验生产订单是否存在
        PmsProduceOrder pmsProduceOrder = produceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
        //校验产线和工位是否存在
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listById(Sets.newHashSet(dto.getProduceLineId(), dto.getStationId()))).get();
        Assert.checkBetween(sysOfficeMap.size(), 2, 2, "产线或工位不存在");
        //校验工序是否存在
        PmsProduceOrderCraft pmsProduceOrderCraft = produceOrderCraftService.getById(dto.getProcessId());
        Assert.notNull(pmsProduceOrderCraft, "工序不存在");
        //校验批次是否存在
        PmsWorkOrderBatch pmsWorkOrderBatch = workOrderBatchService.getById(dto.getBatch());
        Assert.notNull(pmsWorkOrderBatch, "批次不存在");
        //校验班次和班组是否存在
        //校验单位是否存在
        Boolean unitBoolean = FeignOptional.of(remoteDictFeignApi.existTypeId(SysDictConstant.TYPE_MEASURE_UNIT, dto.getUnitId())).get();
        Assert.isTrue(unitBoolean, "单位不存在");
        //生成报工记录
        PmsReportWork pmsReportWork = new PmsReportWork();
        BeanUtils.copyProperties(dto, pmsReportWork);
        pmsReportWork.setCode(reportWorkCode(pmsProduceOrder.getCode(), pmsProduceOrderCraft.getSort()));
        pmsReportWork.setProduceOrderNo(pmsProduceOrder.getCode());
        pmsReportWork.setBatchNo(pmsWorkOrderBatch.getCode());
        pmsReportWork.setId(IdUtil.getSnowflake().nextId());
        save(pmsReportWork);
        //报工工时信息
        List<PmsReportWorkHour> hourModelList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getHourDTOList())) {
            for (ProcessReportWorkHourDTO hourDTO : dto.getHourDTOList()) {
                PmsReportWorkHour hourModel = new PmsReportWorkHour();
                BeanUtils.copyProperties(hourDTO, hourModel);
                hourModel.setReportWorkId(pmsReportWork.getId());
                hourModelList.add(hourModel);
            }
            reportWorkHourService.saveBatch(hourModelList);
        }
        //生成出库单
        return Boolean.TRUE;
    }

    /**
     * 获取报工编码
     *
     * @param orderNo     订单编号
     * @param processSort 工序号-排序字段
     * @return 报工编码
     */
    private String reportWorkCode(String orderNo, Integer processSort) {
        String prefix = orderNo + processSort;
        PmsReportWork one = getOne(new LambdaQueryWrapper<PmsReportWork>()
                .like(PmsReportWork::getCode, prefix)
                .orderByDesc(PmsReportWork::getCode)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = 0L;
        if (Objects.nonNull(one)) {
            String substring = one.getCode().substring(one.getCode().length() - 4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return PmsCodeUtil.getNextCode(prefix, PmsCodeUtil.NUM_BIT_4, currentMax);
    }
}