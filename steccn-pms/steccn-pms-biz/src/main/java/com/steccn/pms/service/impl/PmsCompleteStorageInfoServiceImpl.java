package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.JointUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.mapper.PmsCompleteStorageInfoMapper;
import com.steccn.pms.vo.FurtherProduceReportDataVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsCompleteStorageInfoServiceImpl extends ServiceImpl<PmsCompleteStorageInfoMapper, PmsCompleteStorageInfo> implements PmsCompleteStorageInfoService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsBarcodePrintService barcodePrintService;

    @Override
    public Boolean existCaseNo(Long typeId, Set<String> caseNoSet) {
        long count = baseMapper.existCaseNo(typeId, caseNoSet);
        return count != 0;
    }

    @Override
    public Page<PmsCompleteStorageVO> pageByOrderId(PmsCompleteStorageDTO dto) {
        Page<PmsCompleteStorageVO> page = this.baseMapper.pageByOrderId(new Page(dto.getCurrent(), dto.getSize()), dto.getProduceOrderId());
        if (CollUtil.isNotEmpty(page.getRecords())) {
            Set<String> caseNoList = page.getRecords().stream().map(PmsCompleteStorageVO::getCaseNo).collect(Collectors.toSet());
            Map<String, TupleDTO<Integer, Integer>> cntMap = barcodePrintService.casePrintAndCnt(caseNoList);
            page.getRecords().forEach(ele -> {
                TupleDTO<Integer, Integer> tupleDTO = cntMap.getOrDefault(ele.getCaseNo(), new TupleDTO<>());
                ele.setPrintFlag(tupleDTO.getField1());
                ele.setPrintCount(tupleDTO.getField2());
            });
        }
        return page;
    }

    @Override
    public Map<Long, PmsCompleteStorageNumDTO> numByOrderId(Set<Long> orderSet) {
        Map<Long, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsCompleteStorageInfo> list = list(new QueryWrapper<PmsCompleteStorageInfo>().select("produce_order_id,sum(quantity) as quantity ,unit_id").lambda().in(PmsCompleteStorageInfo::getProduceOrderId, orderSet).groupBy(PmsCompleteStorageInfo::getProduceOrderId));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(pmsCompleteStorageInfo -> {
                    PmsCompleteStorageNumDTO storageNumDTO = new PmsCompleteStorageNumDTO();
                    storageNumDTO.setProduceOrderId(pmsCompleteStorageInfo.getProduceOrderId());
                    storageNumDTO.setQuantity(pmsCompleteStorageInfo.getQuantity());
                    storageNumDTO.setUnitId(pmsCompleteStorageInfo.getUnitId());
                    resMap.put(pmsCompleteStorageInfo.getProduceOrderId(), storageNumDTO);
                });
            }
        }
        return resMap;
    }


    @Override
    public List<String> getCaseNo(Long id) {
        List<PmsCompleteStorageInfo> list = list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().eq(PmsCompleteStorageInfo::getCompleteStorageId, id));
        List<String> caseNoList = list.stream().map(PmsCompleteStorageInfo::getCaseNo).collect(Collectors.toList());
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("未找到完工入库-详情");
        }
        return caseNoList;
    }

    @Override
    public Map<Long, Map<Long, BigDecimal>> materialMap(Set<Long> materialSet, LocalDate date) {
        Map<Long, Map<Long, BigDecimal>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(materialSet) && Objects.nonNull(date)) {

            LocalDateTime start = LocalDateUtil.getMorning(date);
            LocalDateTime end = start.plusDays(1);

            List<FurtherProduceReportDataVO> resList = this.baseMapper.materialList(materialSet, start, end);
            if (CollUtil.isNotEmpty(resList)) {
                resList.forEach(ele -> {
                    if (resMap.containsKey(ele.getMaterialId())) {
                        resMap.get(ele.getMaterialId()).put(ele.getClassesId(), ele.getProcessQuantity());
                    } else {
                        Map<Long, BigDecimal> classMap = Maps.newHashMap();
                        classMap.put(ele.getClassesId(), ele.getProcessQuantity());
                        resMap.put(ele.getMaterialId(), classMap);
                    }
                });
            }
        }
        return resMap;
    }

    public Map<String , BigDecimal> materialMap(List<Integer> lineIdList , Set<Long> materialSet, LocalDate date) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(materialSet) && Objects.nonNull(date)) {
            LocalDateTime start = LocalDateUtil.getMorning(date);
            LocalDateTime end = start.plusDays(1);
            List<FurtherProduceReportDataVO> resList = this.baseMapper.materialLineList(lineIdList, materialSet, start, end);
            if (CollUtil.isNotEmpty(resList)) {
                resList.forEach(ele -> {
                    resMap.put(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, ele.getProductLineId(), ele.getMaterialId(), ele.getClassesId()), ele.getProcessQuantity());
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> inStorage(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<FurtherProduceReportDataVO> resList = baseMapper.inStorage(lineList, start, end);
        if (CollUtil.isNotEmpty(resList)) {
            Set<Long> dictIdSet = resList.stream().map(FurtherProduceReportDataVO::getClassesId).collect(Collectors.toSet());
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(Lists.newArrayList(dictIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            resList.forEach(ele -> ele.setClassDate(LocalDateUtil.getClassDate(ele.getCreateTime(), Boolean.FALSE)));
            for (FurtherProduceReportDataVO data : resList) {
                SysDictVO sysDictVO = dictMap.get(data.getClassesId());
                if (Objects.nonNull(sysDictVO)) {
                    resMap.put(data.getProductLineId() + "_" + data.getClassDate() + "_" + sysDictVO.getCode() + "_" + data.getProduceOrderId(), data.getProcessQuantity());
                }
            }
        }
        return resMap;
    }


    @Override
    public Map<String, BigDecimal> inStorageWeek(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<FurtherProduceReportDataVO> resList = baseMapper.inStorageWeek(lineList, start, end);
        if (CollUtil.isNotEmpty(resList)) {
            for (FurtherProduceReportDataVO data : resList) {
                resMap.put(data.getProductLineId() + "_" + data.getWeekStr(), data.getProcessQuantity());
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> inStorageMonth(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<FurtherProduceReportDataVO> resList = baseMapper.inStorageMonth(lineList, start, end);
        if (CollUtil.isNotEmpty(resList)) {
            for (FurtherProduceReportDataVO data : resList) {
                resMap.put(data.getProductLineId() + "_" + data.getWeekStr(), data.getProcessQuantity());
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, Map<Long, BigDecimal>> materialByDateMap(Set<Long> materialSet, LocalDate date) {
        Map<Long, Map<Long, BigDecimal>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(materialSet) && Objects.nonNull(date)) {
            //当前日期的一号早八点
            LocalDateTime start = LocalDateUtil.getMorning(date.withDayOfMonth(1));
            //当前日期的晚班结束时间
            LocalDateTime end = LocalDateUtil.getMorning(date).plusDays(1);
            List<FurtherProduceReportDataVO> resList = this.baseMapper.materialList(materialSet, start, end);
            if (CollUtil.isNotEmpty(resList)) {
                resList.forEach(ele -> {
                    if (resMap.containsKey(ele.getMaterialId())) {
                        resMap.get(ele.getMaterialId()).put(ele.getClassesId(), ele.getProcessQuantity());
                    } else {
                        Map<Long, BigDecimal> classMap = Maps.newHashMap();
                        classMap.put(ele.getClassesId(), ele.getProcessQuantity());
                        resMap.put(ele.getMaterialId(), classMap);
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public List<FurtherProduceReportDataVO> materialByDateList(Set<Long> materialSet, LocalDate date) {
        List<FurtherProduceReportDataVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(materialSet) && Objects.nonNull(date)) {
            //当前日期的一号早八点
            LocalDateTime start = LocalDateUtil.getMorning(date.withDayOfMonth(1));
            //当前日期的晚班结束时间
            LocalDateTime end = LocalDateUtil.getMorning(date).plusDays(1);
            resList = this.baseMapper.materialList(materialSet, start, end);
        }
        return resList;
    }

}




