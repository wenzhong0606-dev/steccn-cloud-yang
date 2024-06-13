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
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.JointUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.mapper.PmsCompleteStorageInfoMapper;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.FurtherProduceReportDataVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import com.steccn.pms.vo.PmsPullForceDataVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsPullForceService pmsPullForceService;


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

    public Map<String, BigDecimal> materialMap(List<Integer> lineIdList, Set<Long> materialSet, LocalDate date) {
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
    public PmsProducerOrderInfoDTO getNumberByCaseNo(String caseNo) {
        PmsProducerOrderInfoDTO dto = new PmsProducerOrderInfoDTO();
        PmsCompleteStorageInfo info = getOne(new LambdaQueryWrapper<PmsCompleteStorageInfo>().eq(PmsCompleteStorageInfo::getCaseNo, caseNo).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(info)) {
            dto.setProduceOrderId(info.getProduceOrderId());
            dto.setBatchNo(info.getBatchNo());
            dto.setMaterialName(info.getMaterialName());
            dto.setMaterialCode(info.getMaterialCode());
            dto.setMaterialId(info.getMaterialId());
        }
        return dto;
    }

    @Override
    public List<CommonReportDTO> substrate(DpPmsOperateDTO dto) {
        List<CommonReportDTO> resList = Lists.newArrayList();
        //获取车间下的产线
        List<Integer> officeIds = Lists.newArrayList(dto.getOfficeId());
        Map<Integer, List<Integer>> officeMap = FeignOptional.of(() -> remoteOfficeFeign.childWorkshop(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> integers = officeMap.get(dto.getOfficeId());
        //获取转换单位
        SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO unitTon = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //根据班次处理时间并获取数据
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<CompleteStorageInfoDTO> requisitionInfoList = substrateData(granularity, integers, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        if (CollUtil.isNotEmpty(requisitionInfoList)) {
            Map<String, List<CompleteStorageInfoDTO>> collect = requisitionInfoList.stream().collect(Collectors.groupingBy(CompleteStorageInfoDTO::getMaterialName));
            Set<Long> materialIdSet = requisitionInfoList.stream().map(CompleteStorageInfoDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            collect.forEach((k, v) -> {
                CommonReportDTO commonReportDTOTON = new CommonReportDTO();
                commonReportDTOTON.setName(k);
                commonReportDTOTON.setUnit(PmManagerConstant.UNIT_T);

                CommonReportDTO commonReportDTOM = new CommonReportDTO();
                commonReportDTOM.setName(k);
                commonReportDTOM.setUnit(PmManagerConstant.MEASURE_UNIT_SM);

                CommonReportDTO commonReportDTOSlice = new CommonReportDTO();
                commonReportDTOSlice.setName(k);
                commonReportDTOSlice.setUnit(PmManagerConstant.MEASURE_UNIT_PIECE);

                Map<String, BigDecimal> valueMapTON = Maps.newHashMap();
                Map<String, BigDecimal> valueMapM = Maps.newHashMap();
                Map<String, BigDecimal> valueMapSplice = Maps.newHashMap();
                v.forEach(completeStorageInfoDTO -> {
                    //单位转换
                    BigDecimal m = UnitConvertUtil.convertMaterial(materialConvertMap.get(completeStorageInfoDTO.getMaterialId()), unitPiece.getId(), unitSm.getId(), completeStorageInfoDTO.getQuantity(), 4);
                    BigDecimal ton = UnitConvertUtil.convertMaterial(materialConvertMap.get(completeStorageInfoDTO.getMaterialId()), unitPiece.getId(), unitTon.getId(), completeStorageInfoDTO.getQuantity(), 4);
                    String convert = MysqlDateKeyUtil.convert(granularity, completeStorageInfoDTO.getTime());
                    MapUtil.add(valueMapM, convert, m, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(valueMapTON, convert, ton, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(valueMapSplice, convert, completeStorageInfoDTO.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
                });
                commonReportDTOSlice.setValueMap(valueMapSplice);
                commonReportDTOM.setValueMap(valueMapM);
                commonReportDTOTON.setValueMap(valueMapTON);
                resList.add(commonReportDTOSlice);
                resList.add(commonReportDTOM);
                resList.add(commonReportDTOTON);
            });

            if (CollUtil.isNotEmpty(resList)) {
                //合计
                List<CommonReportDTO> totalList = Lists.newArrayList();
                //数据按照单位分类进行合计
                Map<String, List<CommonReportDTO>> unitMap = resList.stream().collect(Collectors.groupingBy(CommonReportDTO::getUnit));
                unitMap.forEach((k, v) -> {
                    CommonReportDTO commonReportDTOTON = new CommonReportDTO();
                    commonReportDTOTON.setName(PmManagerConstant.TOTAL);
                    commonReportDTOTON.setUnit(k);
                    Map<String, BigDecimal> valueMap = Maps.newHashMap();
                    v.forEach(reportDTO -> {
                        Map<String, BigDecimal> map = reportDTO.getValueMap();
                        map.forEach((key, value) -> {
                            MapUtil.add(valueMap, key, value, (x, y) -> BigDecimalUtil.add(x, y));
                        });
                    });
                    commonReportDTOTON.setValueMap(valueMap);
                    totalList.add(commonReportDTOTON);
                });
                //小计
                //根据物料名称获取物料信息以及分类
                List<String> materialNames = resList.stream().map(CommonReportDTO::getName).collect(Collectors.toList());
                Map<String, EqpSpareVO> spareNameMap = FeignOptional.of(() -> remoteSpareFeignApi.listByName(materialNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //根据物料的类型数据进行分类
                Map<String, List<CommonReportDTO>> typeMap = new HashMap<>();
                for (String key : spareNameMap.keySet()) {
                    EqpSpareVO eqpSpareVO = spareNameMap.get(key);
                    for (CommonReportDTO commonReportDTO : resList) {
                        if (commonReportDTO.getName().equals(key)) {
                            typeMap.computeIfAbsent(eqpSpareVO.getTypeName(), k -> new ArrayList<>()).add(commonReportDTO);
                        }
                    }
                }
                typeMap.forEach((k, v) -> {
                    CommonReportDTO commonReportDTOM = new CommonReportDTO();
                    commonReportDTOM.setName(k + PmManagerConstant.SUBTOTAL);
                    commonReportDTOM.setUnit(PmManagerConstant.MEASURE_UNIT_SM);
                    Map<String, BigDecimal> valueMapM = Maps.newHashMap();
                    List<CommonReportDTO> filteredList = v.stream()
                            .filter(reportDTO -> PmManagerConstant.MEASURE_UNIT_SM.equals(reportDTO.getUnit()))
                            .collect(Collectors.toList());
                    filteredList.forEach(reportDTO -> {
                        Map<String, BigDecimal> valueMap = reportDTO.getValueMap();
                        valueMap.forEach((key, value) -> {
                            MapUtil.add(valueMapM, key, value, (x, y) -> BigDecimalUtil.add(x, y));
                        });
                    });
                    commonReportDTOM.setValueMap(valueMapM);
                    resList.add(commonReportDTOM);
                });
                resList.addAll(totalList);
            }
        }
        return resList;
    }

    @Override
    public List<CommonReportDTO> substrateProduceLine(DpPmsOperateDTO dto) {
        List<CommonReportDTO> resList = Lists.newArrayList();
        //获取车间下的产线
        List<Integer> officeIds = Lists.newArrayList(dto.getOfficeId());
        Map<Integer, List<Integer>> officeMap = FeignOptional.of(() -> remoteOfficeFeign.childWorkshop(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> integers = officeMap.get(dto.getOfficeId());
        //根据产线获取对应时间的完工入库的记录
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<CompleteStorageInfoDTO> requisitionInfoList = substrateData(granularity, integers, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        //根据产线分组
        Map<Integer, List<CompleteStorageInfoDTO>> lineMap = requisitionInfoList.stream().collect(Collectors.groupingBy(CompleteStorageInfoDTO::getProductLineId));
        //获取产线名称
        Set<Integer> lineIdSet = requisitionInfoList.stream().map(CompleteStorageInfoDTO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, String> lineNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(lineIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取拉引量数据
        PmsPullForceQueryDTO pmsPullForceQueryDTO = new PmsPullForceQueryDTO();
        pmsPullForceQueryDTO.setStartDate(dto.getStart());
        pmsPullForceQueryDTO.setEndDate(dto.getEnd());
        pmsPullForceQueryDTO.setProductLineIds(lineIdSet);
        List<PmsPullForceDataVO> dataFromPeriods = pmsPullForceService.getDataFromPeriods(pmsPullForceQueryDTO);
        Map<Integer, List<PmsPullForceDataVO>> pullForceData = dataFromPeriods.stream().collect(Collectors.groupingBy(PmsPullForceDataVO::getProductLineId));
        //获取物料的分类
        List<Long> materialIds = requisitionInfoList.stream().map(CompleteStorageInfoDTO::getMaterialId).collect(Collectors.toList());
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<CommonReportDTO> pullForceList = Lists.newArrayList();
        lineMap.forEach((k, v) -> {
            //根据物料的类型数据进行分类
            Map<String, List<CompleteStorageInfoDTO>> typeMap = new HashMap<>();
            for (Long key : spareMap.keySet()) {
                EqpSpareVO vo = spareMap.get(key);
                for (CompleteStorageInfoDTO infoDTO : v) {
                    if (infoDTO.getMaterialId().equals(key)) {
                        typeMap.computeIfAbsent(vo.getTypeName(), k1 -> new ArrayList<>()).add(infoDTO);
                    }
                }
            }
            if (CollUtil.isNotEmpty(typeMap)) {
                typeMap.forEach((typeMapKey, typeMapValue) -> {
                    CommonReportDTO commonReportDTO = new CommonReportDTO();
                    //名称为分类的名称
                    commonReportDTO.setName(typeMapKey);
                    //单位数据为产线名称
                    commonReportDTO.setUnit(lineNameMap.get(k));
                    Map<String, BigDecimal> valueMap = Maps.newHashMap();
                    if (CollUtil.isNotEmpty(typeMapValue)) {
                        typeMapValue.forEach(ele -> {
//                            String convert = MysqlDateKeyUtil.convert(granularity,ele.getInStorageTime());
                            MapUtil.add(valueMap, ele.getInStorageTime(), ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
                        });
                    }
                    commonReportDTO.setValueMap(valueMap);
                    resList.add(commonReportDTO);
                });
            }
            //获取产线数据
            List<PmsPullForceDataVO> pmsPullForceDataVOS = pullForceData.get(k);
            CommonReportDTO commonReportDTO = new CommonReportDTO();
            //名称为分类的名称
            commonReportDTO.setName(lineNameMap.get(k) + "拉引量(T)");
            commonReportDTO.setUnit(lineNameMap.get(k));
            Map<String, BigDecimal> valueMap = Maps.newHashMap();
            pmsPullForceDataVOS.forEach(data -> {
                String convert = MysqlDateKeyUtil.convert(granularity, data.getPullForceDate());
                MapUtil.add(valueMap, convert, data.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
            commonReportDTO.setValueMap(valueMap);
            pullForceList.add(commonReportDTO);
        });
        if (CollUtil.isNotEmpty(resList)) {
            //小计
            //根据产线将数据分类小计
            Map<String, List<CommonReportDTO>> unitMap = resList.stream().collect(Collectors.groupingBy(CommonReportDTO::getUnit));
            unitMap.forEach((k, v) -> {
                CommonReportDTO commonReportDTO = new CommonReportDTO();
                //名称为分类的名称
                commonReportDTO.setName(k + PmManagerConstant.SUBTOTAL);
                //单位数据为产线名称
                commonReportDTO.setUnit(k);
                Map<String, BigDecimal> valueMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(v)) {
                    v.forEach(ele -> {
                        Map<String, BigDecimal> eleValueMap = ele.getValueMap();
                        eleValueMap.forEach((key, value) -> {
                            MapUtil.add(valueMap, key, value, (x, y) -> BigDecimalUtil.add(x, y));
                        });
                    });
                }
                commonReportDTO.setValueMap(valueMap);
                resList.add(commonReportDTO);
            });
        }
        resList.addAll(pullForceList);
        //排序
        resList.sort(Comparator.comparing(CommonReportDTO::getUnit, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        // 提取每个产线名称中的数字
                        Pattern pattern = Pattern.compile("([一二三四五六七八九十])");
                        Matcher matcher1 = pattern.matcher(o1);
                        Matcher matcher2 = pattern.matcher(o2);
                        int num1 = matcher1.find() ? PmManagerConstant.ChineseNumber.getArabicByChinese(matcher1.group(1)) : 0;
                        int num2 = matcher2.find() ? PmManagerConstant.ChineseNumber.getArabicByChinese(matcher2.group(1)) : 0;
                        // 比较两个数字
                        return num1 - num2;
                    }
                })
                .thenComparing(CommonReportDTO::getName, Comparator.nullsLast(String::compareTo)));

        return resList;
    }

    @Override
    public Boolean deleteInfoByCaseNo(Set<String> caseNos) {
        return remove(new LambdaQueryWrapper<PmsCompleteStorageInfo>().in(PmsCompleteStorageInfo::getCaseNo, caseNos));
    }

    public List<CompleteStorageInfoDTO> substrateData(QtManagerConstant.QtGranularityEnum granularity, List<Integer> integers, TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<CompleteStorageInfoDTO> infoList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            infoList = this.baseMapper.substrateDayData(integers, dto.getField1(), dto.getField2());
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            infoList = this.baseMapper.substrateWeekData(integers, dto.getField1(), dto.getField2());
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            infoList = this.baseMapper.substrateMonthData(integers, dto.getField1(), dto.getField2());
        } else {
            infoList = this.baseMapper.substrateYearData(integers, dto.getField1(), dto.getField2());
        }
        return infoList;
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




