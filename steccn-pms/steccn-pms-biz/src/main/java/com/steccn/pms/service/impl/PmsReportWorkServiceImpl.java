package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsReportWorkServiceImpl extends ServiceImpl<PmsReportWorkMapper, PmsReportWork>
        implements PmsReportWorkService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private PmsReportWorkHourService reportWorkHourService;
    @Autowired
    private PmsProduceOrderService produceOrderService;
    @Autowired
    private PmsProduceOrderCraftService produceOrderCraftService;
    @Autowired
    private PmsWorkOrderBatchService workOrderBatchService;
    @Autowired
    private PmsWorkOrderBomService workOrderBomService;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;

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
        Set<Long> unitList = Sets.newHashSet();
        Set<Integer> organIdSet = Sets.newHashSet();
        records.forEach(ele -> {
            unitList.add(ele.getUnitId());
            organIdSet.add(ele.getStationId());
        });
        Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(unitList)).get();
        Map<Integer, String> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(organIdSet)).get();
        records.forEach(ele -> {
            ele.setUnitName(sysDictMap.get(ele.getUnitId()));
            ele.setStationName(sysOfficeMap.get(ele.getStationId()));
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
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listById(Sets.newHashSet(dto.getProductLineId(), dto.getStationId()))).get();
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

    @Override
    public Page<ProcessReportWorkPageVO> recordPage(ProcessReportWorkRecordDTO dto) {
        ProcessReportWorkPageDTO pageDTO = new ProcessReportWorkPageDTO();
        BeanUtils.copyProperties(dto, pageDTO);
        return pageList(pageDTO);
    }

    @Override
    public List<ProcessReportWorkMaterialListDTO> materialConsume(ProcessReportWorkMaterialConsumeDTO dto) {
        //检验生产订单是否存在
        PmsProduceOrder pmsProduceOrder = produceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");

        //校验工序是否存在
        PmsProduceOrderCraft pmsProduceOrderCraft = produceOrderCraftService.getById(dto.getProcessId());
        Assert.notNull(pmsProduceOrderCraft, "工序不存在");

        //工位是否存在
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listById(Sets.newHashSet(dto.getStationId()))).get();
        Assert.checkBetween(sysOfficeMap.size(), 1, 1, "工位不存在");

        //根据生产订单id和工序
        List<PmsWorkOrderBom> pmsWorkOrderBomList = workOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>()
                .eq(PmsWorkOrderBom::getWorkProduceId, pmsProduceOrder.getId())
                .eq(PmsWorkOrderBom::getProcessId, pmsProduceOrderCraft.getId()));
        //定义返回集合
        List<ProcessReportWorkMaterialListDTO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(pmsWorkOrderBomList)) {
            EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();
            Set<Long> unitIdList = Sets.newHashSet();
            List<Long> idList = Lists.newArrayList();
            for (PmsWorkOrderBom pmsWorkOrderBom : pmsWorkOrderBomList) {
                unitIdList.add(pmsWorkOrderBom.getUnitId());
                idList.add(pmsWorkOrderBom.getModuleId());
            }
            Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(unitIdList)).get();
            Map<Long, EqpSpareVO> eqpSpareMap = FeignOptional.of(remoteSpareFeignApi.listById(idList)).get();

            pmsWorkOrderBomList.forEach(ele -> {
                ProcessReportWorkMaterialListDTO resDTO = new ProcessReportWorkMaterialListDTO();
                BeanUtils.copyProperties(ele, resDTO);
                resDTO.setProduceOrderId(pmsProduceOrder.getId());
                resDTO.setProcessName(pmsProduceOrderCraft.getName());
                resDTO.setUnitName(sysDictMap.get(resDTO.getUnitId()));
                EqpSpareVO eqpSpareVO = eqpSpareMap.getOrDefault(ele.getModuleId(), defaultEqpSpareVO);
                resDTO.setModuleName(eqpSpareVO.getName());
                resDTO.setModuleCode(eqpSpareVO.getCode());
                BigDecimal divide = ele.getQuantity().multiply(dto.getProcessQuantity()).divide(pmsProduceOrder.getQuantity(), 4, RoundingMode.HALF_UP);
                resDTO.setQuantity(divide);
                resList.add(resDTO);
            });
        }
        return resList;
    }

    @Override
    public Page<PmsReportWorkVO> reportWorkPageList(PmsReportWorkPageDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsReportWork> eq = new LambdaQueryWrapper<PmsReportWork>()
                .eq(Objects.nonNull(dto.getBatchNo()),PmsReportWork::getProcessId, dto.getProcessId())
                .eq(Objects.nonNull(dto.getBatchNo()),PmsReportWork::getBatchNo, dto.getBatchNo())
                .eq(Objects.nonNull(dto.getStationId()),PmsReportWork::getStationId, dto.getStationId())
                .ge(Objects.nonNull(dto.getStartTime()), PmsReportWork::getCreateTime, dto.getStartTime())
                .le(Objects.nonNull(dto.getEndTime()), PmsReportWork::getCreateTime, dto.getStartTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsReportWorkVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsReportWorkVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> stationIds = Sets.newHashSet();
        res.forEach(pmsReportWorkVO -> {
            dictIds.add(pmsReportWorkVO.getUnitId());
            stationIds.add(pmsReportWorkVO.getStationId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        //查询机构数据
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(stationIds)).get();
        res.forEach(pmsReportWorkVO -> {
            pmsReportWorkVO.setStation(officeMap.get(pmsReportWorkVO.getStationId()));
            pmsReportWorkVO.setUnit(dictMap.get(pmsReportWorkVO.getUnitId()));
        });
        //todo 班次数据待定(OA获取)
        return page.setRecords(res);
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
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCode().substring(one.getCode().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return PmsCodeUtil.getNextCode(prefix, PmsCodeUtil.NUM_BIT_4, currentMax);
    }
}