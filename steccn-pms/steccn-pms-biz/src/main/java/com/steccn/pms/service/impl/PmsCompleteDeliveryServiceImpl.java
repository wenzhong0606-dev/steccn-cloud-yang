package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.Tuple4DTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.dto.PmsCompleteDeliveryInfoDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkMaterialListDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsCompleteDeliveryMapper;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple5;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteDeliveryServiceImpl extends ServiceImpl<PmsCompleteDeliveryMapper, PmsCompleteDelivery> implements PmsCompleteDeliveryService {


    @Autowired
    private PmsProduceOrderCraftService pmsProduceOrderCraftService;
    @Autowired
    private PmsCompleteDeliveryInfoService pmsCompleteDeliveryInfoService;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private PmsWorkOrderBomService pmsWorkOrderBomService;
    @Autowired
    private PmsReportWorkMapper  pmsReportWorkMapper;


    @Override
    public Page<PmsCompleteDeliveryVO> pageList(PmsCompleteDeliveryDTO dto) {
        //1.自定义Mapper查询投料信息
        Page<PmsCompleteDeliveryVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询详情信息
        Page<PmsCompleteDeliveryVO> page = pmsCompleteDeliveryInfoService.listByOrderId(dto);
        //2.处理数据
        List<Long> materialIds = Lists.newArrayList();
        Set<Integer> office = Sets.newHashSet();
        Set<Long> ids = Sets.newHashSet();
        List<Long> processIds = Lists.newArrayList();
        List<PmsCompleteDeliveryVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(pmsCompleteDeliveryVO -> {
                materialIds.add(pmsCompleteDeliveryVO.getMaterialId());
                ids.add(pmsCompleteDeliveryVO.getUnitId());
                ids.add(pmsCompleteDeliveryVO.getTypeId());
                if (Objects.nonNull(pmsCompleteDeliveryVO.getWarehouseId())) {
                    office.add(pmsCompleteDeliveryVO.getWarehouseId());
                }
                if (Objects.nonNull(pmsCompleteDeliveryVO.getShelfId())) {
                    office.add(pmsCompleteDeliveryVO.getShelfId());
                }
                processIds.add(pmsCompleteDeliveryVO.getProcessId());
            });
            //获取单位数据
            Map<Long, String> dictData = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取仓库货架信息
            Map<Integer, String> officeMapData = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(office), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取工序信息
            Map<Long, PmsProduceOrderCraftVO> processMap = pmsProduceOrderCraftService.listById(processIds);
            PmsProduceOrderCraftVO defaultCraftVO = new PmsProduceOrderCraftVO();
            if (CollUtil.isNotEmpty(records)) {
                records.forEach(pmsCompleteDeliveryVO -> {
                    PmsProduceOrderCraftVO pmsProduceOrderCraftVO = processMap.getOrDefault(pmsCompleteDeliveryVO.getProcessId(), defaultCraftVO);
                    //处理单位数据
                    pmsCompleteDeliveryVO.setUnit(dictData.get(pmsCompleteDeliveryVO.getUnitId()));
                    pmsCompleteDeliveryVO.setTypeName(dictData.get(pmsCompleteDeliveryVO.getTypeId()));
                    //处理工序数据
                    pmsCompleteDeliveryVO.setProcessName(pmsProduceOrderCraftVO.getName());
                    //处理仓库货架信息
                    pmsCompleteDeliveryVO.setWarehouseName(officeMapData.get(pmsCompleteDeliveryVO.getWarehouseId()));
                    pmsCompleteDeliveryVO.setShelfName(officeMapData.get(pmsCompleteDeliveryVO.getShelfId()));
                });
            }
        }
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> feedPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询投料信息
        Page<PmsCompleteDeliveryVO> page = pageList(dto);
        //2.处理数据
        Set<Long> unitIds = Sets.newHashSet();
        Set<Integer> officeIds = Sets.newHashSet();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                unitIds.add(pmsCompleteDeliveryVO.getUnitId());
                officeIds.add(pmsCompleteDeliveryVO.getShelfId());
                officeIds.add(pmsCompleteDeliveryVO.getWarehouseId());
            });
            //查询字典数据
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //查询机构数据
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                pmsCompleteDeliveryVO.setUnit(dictMap.get(pmsCompleteDeliveryVO.getUnitId()));
                pmsCompleteDeliveryVO.setWarehouseName(officeMap.get(pmsCompleteDeliveryVO.getWarehouseId()));
            });
        }
        return page;
    }

    @Override
    public Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> processNumConsumeMap(Set<Long> produceOrderIdSet) {
        //定义返回结构 key-订单id value【key-工序id value【key-物料id，value-数据对象】】
        Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> resMap = Maps.newHashMap();
        //如果生产订单不为空
        if (CollUtil.isNotEmpty(produceOrderIdSet)) {
            //查询分组数据
            List<ProduceOrderProcessNumVO> dataList = this.baseMapper.processNumConsumeMap(produceOrderIdSet);
            //分组数据不为空
            if (CollUtil.isNotEmpty(dataList)) {
                List<Long> craftProcessIdList = dataList.stream().map(ProduceOrderProcessNumVO::getProcessId).collect(Collectors.toList());
                Map<Long, PmsProduceOrderCraftVO> craftMap = pmsProduceOrderCraftService.listById(craftProcessIdList);

                //根据生产订单id分组  key-生产订单id value-分组数据
                Map<Long, List<ProduceOrderProcessNumVO>> produceOrderMap = dataList.stream().collect(Collectors.groupingBy(ProduceOrderProcessNumVO::getProduceOrderId));
                for (Map.Entry<Long, List<ProduceOrderProcessNumVO>> produceOrderEntry : produceOrderMap.entrySet()) {
                    //根据工序分组  key-工序id value-工序map
                    Map<Long, List<ProduceOrderProcessNumVO>> processMap = produceOrderEntry.getValue().stream().collect(Collectors.groupingBy(ProduceOrderProcessNumVO::getProcessId));
                    Map<Long, Map<Long, ProduceOrderProcessNumVO>> processResMap = Maps.newHashMap();
                    for (Map.Entry<Long, List<ProduceOrderProcessNumVO>> processEntry : processMap.entrySet()) {
                        Map<Long, ProduceOrderProcessNumVO> materialResMap = Maps.newHashMap();
                        for (ProduceOrderProcessNumVO materialVO : processEntry.getValue()) {
                            materialResMap.put(materialVO.getMaterialId(), materialVO);
                            //基于生产订单的bom去查询计划数量
                            PmsWorkOrderBom one = pmsWorkOrderBomService.getOne(new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, produceOrderEntry.getKey()).eq(Objects.nonNull(craftMap.get(processEntry.getKey())), PmsWorkOrderBom::getProcessId, craftMap.get(processEntry.getKey()).getProcessId()).eq(PmsWorkOrderBom::getModuleId, materialVO.getMaterialId()), false);
                            if (Objects.nonNull(one) && Objects.nonNull(one.getQuantity())) {
                                materialVO.setAvaQuantity(one.getQuantity());
                            } else {
                                materialVO.setAvaQuantity(BigDecimal.ZERO);
                            }
                        }
                        processResMap.put(processEntry.getKey(), materialResMap);
                    }
                    resMap.put(produceOrderEntry.getKey(), processResMap);
                }
            }
        }
        return resMap;
    }

    @Override
    public List<SubstrateIngredientVO> subIngredientReport(PmsDayReportDTO dto) {
        List<SubstrateIngredientVO> resList = Lists.newArrayList();

        List<SubstrateIngredientConsumeVO> dataList = this.baseMapper.subIngredientReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), dto.getDate());
        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(ele -> {
                SubstrateIngredientVO vo = new SubstrateIngredientVO();
                BeanUtils.copyProperties(ele, vo);
                vo.setOutKg(ele.getQuantity());
                vo.setOutTon(BigDecimalUtil.divide(ele.getQuantity(), BigDecimal.valueOf(1000)));
                if (StrUtil.isNotBlank(ele.getProduceOrderIdStr())) {
                    String[] split = ele.getProduceOrderIdStr().split(",");
                    Set<Long> produceOrderIdSet = Sets.newHashSet();
                    for (String str : split) {
                        try {
                            produceOrderIdSet.add(Long.parseLong(str));
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                    }
                    vo.setProduceOrderIdSet(produceOrderIdSet);
                }
                resList.add(vo);
            });
        }
        return resList;
    }

    @Override
    public List<ProcessReportWorkMaterialListDTO> getMaterialConsume(Long reportWorkId) {
        List<ProcessReportWorkMaterialListDTO> resList = Lists.newArrayList();
        PmsCompleteDelivery pmsCompleteDelivery = getOne(new LambdaQueryWrapper<PmsCompleteDelivery>().eq(PmsCompleteDelivery::getReportWorkId, reportWorkId).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(pmsCompleteDelivery)) {
            List<PmsCompleteDeliveryInfo> dbList = pmsCompleteDeliveryInfoService.list(new LambdaQueryWrapper<PmsCompleteDeliveryInfo>().eq(PmsCompleteDeliveryInfo::getCompleteDeliveryId, pmsCompleteDelivery.getId()));
            if (CollUtil.isNotEmpty(dbList)) {
                Set<Integer> organIdSet = Sets.newHashSet();
                Set<Long> dictIdSet = Sets.newHashSet();
                dbList.forEach(ele -> {
                    organIdSet.add(ele.getWarehouseId());
                    organIdSet.add(ele.getShelfId());
                    dictIdSet.add(ele.getUnitId());
                });
                Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Map<Integer, String> organMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(organIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                dbList.forEach(ele -> {
                    ProcessReportWorkMaterialListDTO resVo = new ProcessReportWorkMaterialListDTO();
                    BeanUtils.copyProperties(ele, resVo);
                    resVo.setModuleId(ele.getMaterialId());
                    resVo.setModuleCode(ele.getMaterialCode());
                    resVo.setModuleName(ele.getMaterialName());
                    resVo.setCreateDate(ele.getCreateTime());
                    resVo.setUnitName(dictMap.get(resVo.getUnitId()));
                    resVo.setWarehouseName(organMap.get(resVo.getWarehouseId()));
                    resVo.setShelfName(organMap.get(resVo.getShelfId()));
                    resVo.setProduceOrderId(pmsCompleteDelivery.getProduceOrderId());
                    resList.add(resVo);
                });
            }
        }
        return resList;
    }

    @Override
    public Boolean generateOrderByCompleteStorage(PmsCompleteDelivery pmsCompleteDelivery, WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO) {
        pmsCompleteDelivery.setId(IdUtil.getSnowflake().nextId());
        pmsCompleteDelivery.setCode(maxOutVouchCode());
        pmsCompleteDelivery.setState(PmManagerConstant.InVouchEnum.FINISH.getCode());
        pmsCompleteDelivery.setDeliveryTime(LocalDateTime.now());
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_4, SysDictConstant.TYPE_OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        pmsCompleteDelivery.setTypeId(sysDictVO.getId());
        if (CollUtil.isNotEmpty(wmsDeliveryReceiptDTO.getList())) {
            List<PmsCompleteDeliveryInfo> infoList = Lists.newArrayList();
            wmsDeliveryReceiptDTO.getList().forEach(info -> {
                PmsCompleteDeliveryInfo deliveryInfo = new PmsCompleteDeliveryInfo();
                BeanUtils.copyProperties(info, deliveryInfo);
                deliveryInfo.setCompleteDeliveryId(pmsCompleteDelivery.getId());
                deliveryInfo.setBatchNo(info.getBatch());
                infoList.add(deliveryInfo);
            });
            pmsCompleteDeliveryInfoService.saveBatch(infoList);
        }
        return save(pmsCompleteDelivery);
    }

    @Override
    public  Map<Long,List<PmsCompleteDeliveryInfoDTO>> reportWorkMaterial(Set<Long> reportIds) {
        Map<Long,List<PmsCompleteDeliveryInfoDTO>> res = Maps.newHashMap();
        if (CollUtil.isNotEmpty(reportIds)){
            List<PmsCompleteDelivery> list = list(new LambdaQueryWrapper<PmsCompleteDelivery>().in(PmsCompleteDelivery::getReportWorkId, reportIds));
            if (CollUtil.isNotEmpty(list)){
                Set<Long> collect = list.stream().map(PmsCompleteDelivery::getId).collect(Collectors.toSet());
                List<PmsCompleteDeliveryInfo> infoList = pmsCompleteDeliveryInfoService.list(new LambdaQueryWrapper<PmsCompleteDeliveryInfo>().in(PmsCompleteDeliveryInfo::getCompleteDeliveryId, collect));
                if (CollUtil.isNotEmpty(infoList)){
                    Map<Long, List<PmsCompleteDeliveryInfo>> map = infoList.stream().collect(Collectors.groupingBy(PmsCompleteDeliveryInfo::getCompleteDeliveryId));
                    for (PmsCompleteDelivery pmsCompleteDelivery : list) {
                        List<PmsCompleteDeliveryInfo> deliveryInfoList = map.get(pmsCompleteDelivery.getId());
                        List<PmsCompleteDeliveryInfoDTO> infos = BeanUtil.copyToList(deliveryInfoList, PmsCompleteDeliveryInfoDTO.class);
                        res.put(pmsCompleteDelivery.getReportWorkId(),infos);
                    }
                }
            }
        }
        return res;
    }
    /**
     * 获取报工物料消耗（异丙醇/镀膜液/油墨）
     * @param dto 车间/部门id，开始日期，结束日期，粒度类型,类型 部门：0/车间：1
     * @return key为-1表示合计，key为产线车间主键表示对应的产线车间的值，list[0]:异丙醇，list[1]:镀膜液，list[2]:油墨
     */
    @Override
    public List<TupleDTO<String,Map<Integer,BigDecimal>>> materialConsumption(Tuple5DTO<Integer, LocalDate, LocalDate, Integer,Integer> dto) {
        //获取车间/部门下对应的产线
        TupleDTO<String, Set<Integer>> tupleDTO = new TupleDTO<>();
        tupleDTO.setField1("9");
        tupleDTO.setField2(Sets.newHashSet(dto.getField1()));
        Map<Integer, List<Integer>> officeMap = FeignOptional.of(() -> remoteOfficeFeign.childType(tupleDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取对应时间段内报工的物料消耗id
        LocalDateTime start = LocalDateUtil.getMorning(dto.getField2());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getField3()).plusDays(1);
        Set<Long> materialIds = pmsReportWorkMapper.materialConsumeNoMain(Sets.newHashSet(officeMap.get(dto.getField1())), start, end);
        //获取物料的消耗数据
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getField4());
        List<Tuple4DTO<String, String, BigDecimal, Integer>> list = materialConsumptionValue(officeMap.get(dto.getField1()), materialIds, start, end, granularity);
        //根据物料名称分类
        Map<String, List<Tuple4DTO<String, String, BigDecimal, Integer>>> dataMap = list.stream().collect(Collectors.groupingBy(Tuple4DTO::getField2));
        List<TupleDTO<String,Map<Integer,BigDecimal>>> resList = Lists.newArrayList();
        dataMap.forEach((k,v)->{
            Map<Integer,BigDecimal> map = Maps.newHashMap();
            Map<Integer, List<Tuple4DTO<String, String, BigDecimal, Integer>>> valueMap;
            //数据根据产线分类
            Map<Integer, List<Tuple4DTO<String, String, BigDecimal, Integer>>> lineValueMap = v.stream().collect(Collectors.groupingBy(Tuple4DTO::getField4));
            if (dto.getField5().equals(0)){
                //获取产线的上级车间
                Map<Integer, Integer> lineParentMap = FeignOptional.of(() -> remoteOfficeFeign.parentLine(Sets.newHashSet(officeMap.get(dto.getField1()))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                valueMap = parentMap(lineValueMap, lineParentMap);
            }else {
                valueMap = lineValueMap;
            }
            valueMap.forEach((line,value)->{
                    BigDecimal reduce = value.stream()
                            .map(Tuple4DTO::getField3)
                            .reduce(BigDecimal.ZERO, BigDecimalUtil::add);
                    map.put(line,reduce);
                });
                map.put(-1,valueMap.values().stream()
                        .flatMap(List::stream)
                        .map(Tuple4DTO::getField3)
                        .reduce(BigDecimal.ZERO, BigDecimalUtil::add));
            resList.add(new TupleDTO<>(k,map));
        });
        return resList;
    }

    /**
     * 根据车间分类
     * @param lineValueMap
     * @param lineParentMap
     * @return
     */
    public  Map<Integer, List<Tuple4DTO<String, String, BigDecimal, Integer>>> parentMap(Map<Integer, List<Tuple4DTO<String, String, BigDecimal, Integer>>> lineValueMap,  Map<Integer, Integer> lineParentMap){
        Map<Integer, List<Tuple4DTO<String, String, BigDecimal, Integer>>> newMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : lineParentMap.entrySet()) {
            Integer parent = entry.getValue();
            List<Tuple4DTO<String, String, BigDecimal, Integer>> value = lineValueMap.get(parent);
            if (value != null) {
                newMap.put(parent, value);
            }
        }
        return newMap;
    }

    public  List<Tuple4DTO<String, String, BigDecimal,Integer>> materialConsumptionValue(List<Integer> lineIds,Set<Long> materialIds,LocalDateTime start,LocalDateTime end,QtManagerConstant.QtGranularityEnum granularity){
        Assert.notNull(granularity,"粒度不能为空");
        List<Tuple4DTO<String, String, BigDecimal,Integer>> list = Lists.newArrayList();
            if (CollUtil.isNotEmpty(materialIds)) {
                if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                    list = baseMapper.materialConsumptionWeekValue(lineIds, materialIds, start, end);
                } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
                    list = baseMapper.materialConsumptionMonthValue(lineIds, materialIds, start, end);
                } else {
                    list = baseMapper.materialConsumptionYearValue(lineIds, materialIds, start, end);
                }
            }
        return list;
    }

    /**
     * 排序
     * @param dataMap
     * @param materialNames
     * @return
     */
    private  Map<String, List<Tuple4DTO<String, String, BigDecimal, Integer>>>  sort( Map<String, List<Tuple4DTO<String, String, BigDecimal, Integer>>> dataMap,  List<String> materialNames ){
        Comparator<String> customComparator = (o1, o2) -> {
            int index1 = materialNames.indexOf(o1);
            int index2 = materialNames.indexOf(o2);
            if (index1 == -1 && index2 != -1) {
                return 1;
            } else if (index1 != -1 && index2 == -1) {
                return -1;
            } else {
                return Integer.compare(index1, index2);
            }
        };
      return dataMap.entrySet().stream().sorted(Map.Entry.comparingByKey(customComparator)).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    /**
     * 最大出库code
     *
     * @return
     */
    private String maxOutVouchCode() {
        String prefix = PmsCodeUtil.PREFIX_SO + LocalDate.now().toString().replaceAll(PmsCodeUtil.SHORT_LINE, Strings.EMPTY) + PmsCodeUtil.SHORT_LINE;
        PmsCompleteDelivery one = getOne(new LambdaQueryWrapper<PmsCompleteDelivery>().likeRight(PmsCompleteDelivery::getCode, prefix).orderByDesc(PmsCompleteDelivery::getCode).last(MpConstant.STR_LIMIT_1));
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




