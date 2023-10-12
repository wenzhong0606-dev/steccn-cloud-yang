package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.listener.ExcelListener;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.mapper.PmsProducePlanMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.PmsProducePlanVO;
import com.steccn.pms.vo.ProducePlanImportVO;
import com.steccn.pms.vo.ProducePlanMsgVO;
import com.steccn.pms.vo.ProducePlanVO;
import com.steccn.wms.dto.MaterialInventoryDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

/**
 * @author yequan
 * @description 针对表【pms_produce_plan(生产月计划)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@RequiredArgsConstructor
@Service
public class PmsProducePlanServiceImpl extends ServiceImpl<PmsProducePlanMapper, PmsProducePlan>
        implements PmsProducePlanService {

    private final PmsMrpInfoService pmsMrpInfoService;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteMessFeignApi remoteMessFeignApi;

    private final RedissonClient redissonClient;

    private final RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final PmsReportWorkMapper pmsReportWorkMapper;


    @Override
    public List<PmsProducePlanDTO> list(String monthDate) {
        //根据月份查询数据
        List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().like(PmsProducePlan::getPlanDate, monthDate + "%"));
        List<PmsProducePlanVO> producePlanVOList = new ArrayList<>();
        List<Long> materialId = producePlanList.stream().map(PmsProducePlan::getMaterialId).collect(Collectors.toList());
        Map<Long, EqpSpareVO> map = remoteSpareFeignApi.listById(materialId).getData();
        EqpSpareVO spare = new EqpSpareVO();
        for (PmsProducePlan pmsProducePlan : producePlanList) {
            EqpSpareVO spareVO = map.getOrDefault(pmsProducePlan.getMaterialId(), spare);
            PmsProducePlanVO producePlanVO = new PmsProducePlanVO();
            BeanUtils.copyProperties(pmsProducePlan, producePlanVO);
            producePlanVO.setMaterialCode(spareVO.getCode());
            producePlanVO.setMaterialName(spareVO.getName());
            producePlanVOList.add(producePlanVO);
        }

        Map<String, List<PmsProducePlanVO>> groupBy = producePlanVOList
                .stream()
                .collect(Collectors.groupingBy(pmsProducePlanVO -> pmsProducePlanVO.getMaterialName() + "," + pmsProducePlanVO.getMaterialCode() + "," + pmsProducePlanVO.getMaterialId() + "," + pmsProducePlanVO.getCustomerId()+"," + pmsProducePlanVO.getCustomerCode() + "," + pmsProducePlanVO.getCustomerName()));

        List<PmsProducePlanDTO> pmsProducePlanDTOList = new ArrayList<>();
        LocalDate firstMonthDate = LocalDate.parse(monthDate + "-01", ISO_DATE);
        //获取指定月份所有天数
        List<LocalDate> localDateList = LocalDateUtil.getCurrentMonthDateAll(firstMonthDate);
        //查找入库信息
        List<PmsStorageCountDTO> storageCount = pmsReportWorkMapper.getStorageCount(LocalDateTime.of(firstMonthDate, LocalTime.MIN), LocalDateTime.of(firstMonthDate.withDayOfMonth(firstMonthDate.lengthOfMonth()), LocalTime.MAX));
        for (Map.Entry<String, List<PmsProducePlanVO>> entry : groupBy.entrySet()) {
            //每天最大的数字
            Map<LocalDate,BigDecimal> maxDayCount = Maps.newHashMap();
            BigDecimal maxTotalCount = BigDecimal.ZERO;
            //每种物料默认出现三次
            for(int i = 0;i < 3; i++){
                PmsProducePlanDTO pmsProducePlanDTO = new PmsProducePlanDTO();
                String[] materials = entry.getKey().split(",");
                pmsProducePlanDTO.setMaterialName(materials[0]);
                pmsProducePlanDTO.setMaterialCode(materials[1]);
                pmsProducePlanDTO.setMaterialId(Long.parseLong(materials[2]));
                if(!materials[4].equals("null")){
                    pmsProducePlanDTO.setCustomerCode(materials[4]);
                }
                if(!materials[5].equals("null")){
                    pmsProducePlanDTO.setCustomerName(materials[5]);
                }
                pmsProducePlanDTO.setType(i);
                List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList = new ArrayList<>();
                //用作入库数量加总计数
                Map<Integer,BigDecimal> countStorage = Maps.newHashMap();
                //0表示采购导入 1表示客户导入 2表示入库数量
                if(i!=2){
                    for (LocalDate localDate : localDateList) {
                        PmsProducePlanDetailsDTO planDetailsDTO = new PmsProducePlanDetailsDTO();

                        int finalI = i;
                        Optional<PmsProducePlanVO> present = entry.getValue().stream().filter(producePlanVO -> producePlanVO.getPlanDate().equals(localDate)&&producePlanVO.getType().equals(finalI)).findAny();
                        if (present.isPresent()) {
                            BeanUtils.copyProperties(present.get(), planDetailsDTO);
                        } else {
                            planDetailsDTO.setQuantity(BigDecimal.ZERO);
                        }
                        // 使用 compute 方法来处理值,取每天的最大值
                        maxDayCount.compute(localDate, (key, oldValue) -> {
                            if (oldValue == null || planDetailsDTO.getQuantity().compareTo(oldValue) > 0) {
                                return planDetailsDTO.getQuantity();
                            } else {
                                return oldValue;
                            }
                        });
                        planDetailsDTO.setPlanDate(localDate);
                        planDetailsDTO.setType(i);
                        producePlanDetailsDTOList.add(planDetailsDTO);
                    }
                }else {
                    //查找入库数量
                    for (LocalDate localDate : localDateList) {
                        PmsProducePlanDetailsDTO planDetailsDTO = new PmsProducePlanDetailsDTO();
                        List<PmsStorageCountDTO> storageCountTmp = Lists.newArrayList();
                        if(materials[3].equals("null")){
                            storageCountTmp = storageCount.stream().filter(v -> v.getMaterialId().equals(pmsProducePlanDTO.getMaterialId())
                                    && v.getCustomerId() == null
                                    && v.getEnterTime() != null
                                    && LocalDateUtil.isRange(v.getEnterTime(), localDate.atTime(LocalTime.MIN), localDate.atTime(LocalTime.MAX), true, false)).collect(Collectors.toList());
                        }else {
                            storageCountTmp = storageCount.stream().filter(v -> v.getMaterialId().equals(pmsProducePlanDTO.getMaterialId())
                                    && v.getCustomerId().equals(Integer.parseInt(materials[3]))
                                    && v.getEnterTime() != null
                                    && LocalDateUtil.isRange(v.getEnterTime(), localDate.atTime(LocalTime.MIN), localDate.atTime(LocalTime.MAX), true, false)).collect(Collectors.toList());
                        }
                        if(!materials[4].equals("null")){
                            pmsProducePlanDTO.setCustomerCode(materials[4]);
                        }
                        if(!materials[5].equals("null")){
                            pmsProducePlanDTO.setCustomerName(materials[5]);
                        }
                        if (CollUtil.isNotEmpty(storageCountTmp)) {
                            List<BigDecimal> bigDecimals = storageCountTmp.stream().map(PmsStorageCountDTO::getQuantity).collect(Collectors.toList());
                            BigDecimal quantity = BigDecimalUtil.sum(bigDecimals);
                            if(quantity==null){
                                quantity = BigDecimal.ZERO;
                            }
                            BigDecimalUtil.accumulate(countStorage,0,quantity);
                            planDetailsDTO.setQuantity(quantity);
                        } else {
                            planDetailsDTO.setQuantity(BigDecimal.ZERO);
                        }
                        // 使用 compute 方法来处理值,取每天的最大值
                        maxDayCount.compute(localDate, (key, oldValue) -> {
                            if (oldValue == null || planDetailsDTO.getQuantity().compareTo(oldValue) > 0) {
                                return planDetailsDTO.getQuantity();
                            } else {
                                return oldValue;
                            }
                        });
                        planDetailsDTO.setPlanDate(localDate);
                        planDetailsDTO.setType(i);
                        producePlanDetailsDTOList.add(planDetailsDTO);
                    }

                }
                if(i!=2){
                    List<BigDecimal> totalList = entry.getValue().stream().map(PmsProducePlanVO::getQuantity).collect(Collectors.toList());
                    BigDecimal total = BigDecimalUtil.sum(totalList);
                    //做特殊处理,总和为空,则为0
                    if(Objects.isNull(total)){
                        total = BigDecimal.ZERO;
                    }
                    if(total.compareTo(maxTotalCount)==1){
                        maxTotalCount = total;
                    }
                    pmsProducePlanDTO.setTotal(total);
                }else {
                    //入库数量特殊加总
                    BigDecimal countTmp = BigDecimal.ZERO;
                    if(countStorage.get(0)!=null){
                        countTmp = countStorage.get(0);
                    }
                    if(countTmp.compareTo(maxTotalCount)==1){
                        maxTotalCount = countStorage.get(0);
                    }
                    pmsProducePlanDTO.setTotal(countTmp);
                }
                LocalDateTime updateTime = entry.getValue().stream().max(Comparator.comparing(PmsProducePlanVO::getUpdateTime)).get().getUpdateTime();
                pmsProducePlanDTO.setProducePlanDetailsDTOList(producePlanDetailsDTOList);
                pmsProducePlanDTO.setUpdateTime(updateTime);
                pmsProducePlanDTOList.add(pmsProducePlanDTO);
            }
            //回退三次算每天的百分比
            for(int i = 0; i < 3; i++){
                PmsProducePlanDTO pmsProducePlanTmp = pmsProducePlanDTOList.get(pmsProducePlanDTOList.size() - 1 - i);
                pmsProducePlanTmp.getProducePlanDetailsDTOList().forEach(tmp->{
                    tmp.setPercentage(BigDecimalUtil.percent(tmp.getQuantity(),maxDayCount.get(tmp.getPlanDate()),2));
                });
                //总百分比
                pmsProducePlanTmp.setTotalPercent(BigDecimalUtil.percent(pmsProducePlanTmp.getTotal(),maxTotalCount));
            }
        }
        //添加序号
        for (int i = 0; i < pmsProducePlanDTOList.size(); i++) {
            pmsProducePlanDTOList.get(i).setId(i);
        }
        //排序
        return pmsProducePlanDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrEditBatch(PmsProducePlanDTO dto) {
        //需要新增的list
        List<PmsProducePlan> addProducePlanList = new ArrayList<>();
        //需要修改的list
        List<PmsProducePlan> updateProducePlanList = new ArrayList<>();
        SysOfficeVO sysOfficeVO = null;
        if(dto.getCustomerId()!=null){
            sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getCustomerId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(dto.getCustomerId());
        }else if(StrUtil.isNotBlank(dto.getCustomerCode())){
            sysOfficeVO = FeignOptional.of(()->remoteOfficeFeign.listByCodeSet(Sets.newHashSet(dto.getCustomerCode())),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(dto.getCustomerCode());
        }
        for (PmsProducePlanDetailsDTO planDetailsDTO : dto.getProducePlanDetailsDTOList()) {
            PmsProducePlan pmsProducePlan = new PmsProducePlan();
            if(dto.getMaterialCode()!=null && dto.getMaterialId()==null){
                EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.listByCode(com.google.common.collect.Lists.newArrayList(dto.getMaterialCode())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(dto.getMaterialCode());
                if(Objects.isNull(eqpSpareVO)){
                    throw new BusinessException("物料不存在");
                }
                dto.setMaterialId(eqpSpareVO.getId());
            }
            pmsProducePlan.setMaterialId(dto.getMaterialId());
            pmsProducePlan.setPlanDate(planDetailsDTO.getPlanDate());
            pmsProducePlan.setQuantity(planDetailsDTO.getQuantity());
            pmsProducePlan.setPosition(dto.getPosition());
            pmsProducePlan.setOrderCode(dto.getOrderCode());
            //加入客户
            if(Objects.nonNull(sysOfficeVO)){
                pmsProducePlan.setCustomerId(sysOfficeVO.getId());
                pmsProducePlan.setCustomerCode(sysOfficeVO.getOfficeCode());
                pmsProducePlan.setCustomerName(sysOfficeVO.getOfficeName());
            }

            //如果有id就修改，没有就新增
            if (Objects.isNull(planDetailsDTO.getId())) {
                pmsProducePlan.setStatus(0);
                //只能新增用户数据
                if(Objects.isNull(dto.getType())){
                    pmsProducePlan.setType(OaConstant.YES);
                }else {
                    pmsProducePlan.setType(dto.getType());
                }
                addProducePlanList.add(pmsProducePlan);
            } else {
                pmsProducePlan.setId(planDetailsDTO.getId());
                //校验值,只有用户数据才可修改
                if(pmsProducePlan.getType().equals(OaConstant.YES)){
                    throw new BusinessException("只能导入数据才可修改");
                }
                updateProducePlanList.add(pmsProducePlan);
            }
        }
        if (CollectionUtils.isNotEmpty(addProducePlanList)) {
            //当为客户导入数据时,需要校验重复值
            if(addProducePlanList.get(0).getType().equals(OaConstant.YES)){
                List<LocalDate> localDateList = addProducePlanList.stream().map(PmsProducePlan::getPlanDate).collect(Collectors.toList());
                List<PmsProducePlan> list = list(new LambdaQueryWrapper<PmsProducePlan>().eq(PmsProducePlan::getMaterialId, dto.getMaterialId())
                        .in(PmsProducePlan::getPlanDate, localDateList)
                        .eq(PmsProducePlan::getType,OaConstant.YES));
                if(CollUtil.isNotEmpty(list)){
                    addProducePlanList.forEach(tmp->{
                        //若有相同的物料和客户则无法新增
                        PmsProducePlan pmsProducePlan = list.stream().filter(v -> v.getMaterialId().equals(tmp.getMaterialId()) && v.getCustomerId().equals(tmp.getCustomerId())).findFirst().orElse(null);
                        if(Objects.nonNull(pmsProducePlan)){
                            throw new BusinessException("该物料已存在生产计划，新增失败");
                        }
                    });
                }
            }
            if (!saveBatch(addProducePlanList)) {
                throw new BusinessException("生产月计划新增失败");
            }
        }
        if (CollectionUtils.isNotEmpty(updateProducePlanList)) {
            if (!updateBatchById(updateProducePlanList)) {
                throw new BusinessException("生产月计划修改失败");
            }
        }
        // 处理MRP 计算逻辑
//        addProducePlanList.addAll(updateProducePlanList);
////        return pmsMrpInfoService.add(dto);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addMaterialBatch(List<PmsProducePlanDTO> dto) {
        if(CollUtil.isNotEmpty(dto)){
            //添加时先删除相关数据
            List<String> materialCodes = dto.stream()
                    .map(PmsProducePlanDTO::getMaterialCode)
                    .collect(Collectors.toList());
            Map<String, EqpSpareVO> spareVO = FeignOptional.of(() -> remoteSpareFeignApi.listByCode(materialCodes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            LocalDate planDate = dto.get(0).getProducePlanDetailsDTOList().get(0).getPlanDate();
            //先删除所有数据
            remove(new LambdaQueryWrapper<PmsProducePlan>()
                    .eq(PmsProducePlan::getType,OaConstant.NO)
                    .between(PmsProducePlan::getPlanDate,planDate.withDayOfMonth(1),planDate.withDayOfMonth(planDate.lengthOfMonth())));
            for(PmsProducePlanDTO tmp:dto){
                tmp.setType(OaConstant.NO);
                this.addOrEditBatch(tmp);
            }
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProducePlanImportVO excelImportData(PmExcelDTO dto) throws IOException {
        Integer updateFlag = dto.getUpdateFlag();
        //文件类型校验
        if (Objects.nonNull(dto.getFile())) {
            //获取文件拓展名
            String fileExtension = Objects.requireNonNull(dto.getFile().getOriginalFilename()).substring(dto.getFile().getOriginalFilename().lastIndexOf("."));
            if (!fileExtension.equals(PmManagerConstant.FILE_EXTENSION_XLSX)) {
                throw new BusinessException("文件类型不支持,请使用模板进行导入");
            }
        }
        //定义读取excel数据的集合
        List<PmsProducePlanNewExcelDTO> dtoList = Lists.newArrayList();
        List<ProducePlanMsgVO> planMsgVOList = Lists.newArrayList();

        EasyExcel.read(dto.getFile().getInputStream(), PmsProducePlanNewExcelDTO.class, new ExcelListener(dtoList, planMsgVOList)).sheet().doRead();

        //物料编码
        List<String> codeList = dtoList.stream().map(PmsProducePlanNewExcelDTO::getMaterialCode).collect(Collectors.toList());
        //客户编码
        Set<String> customerCode = dtoList.stream().map(PmsProducePlanNewExcelDTO::getCustomerMaterialCode).collect(Collectors.toSet());
        Map<String, EqpSpareVO> spareCodeMap = remoteSpareFeignApi.listByCode(codeList).getData();
        if(CollUtil.isEmpty(customerCode)){
            throw new BusinessException("客户编码不能为空");
        }
        Map<String, SysOfficeVO> customerCodeMap = FeignOptional.of(()->remoteOfficeFeign.listByCodeSet(customerCode),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取已存在的物料信息
        List<ProducePlanVO> producePlanVOList = getUpdateMap(dtoList, codeList);
        //校验导入内容
        planMsgVOList.addAll(checkPlanExcelDTO(dtoList, updateFlag, spareCodeMap, producePlanVOList,customerCodeMap));

        //如果错误信息不为空组装数据返回
        ProducePlanImportVO planImportVO = new ProducePlanImportVO();
        if (CollectionUtils.isNotEmpty(planMsgVOList)) {
            planImportVO.setImportFlag(false);
            planImportVO.setProducePlanMsgVOList(planMsgVOList);
            return planImportVO;
        }

        planImportVO.setImportFlag(true);
        List<PmsProducePlan> addList = new ArrayList<>();
        List<PmsProducePlan> updateList = new ArrayList<>();
        //处理数据
        for (PmsProducePlanNewExcelDTO excelDTO : dtoList) {
            //获取当月天数
            LocalDate date = LocalDate.parse(excelDTO.getMonth() + PmsCodeUtil.SHORT_LINE + PmReportConstant.date_01, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<LocalDate> currentMonthDateAll = LocalDateUtil.getCurrentMonthDateAll(date);
            for (LocalDate localDate : currentMonthDateAll) {
                PmsProducePlan pmsProducePlan = new PmsProducePlan();
                //根据物料编码获取物料id
                pmsProducePlan.setMaterialId(spareCodeMap.get(excelDTO.getMaterialCode()).getId());
                //客户数据
                pmsProducePlan.setCustomerId(customerCodeMap.get(excelDTO.getCustomerMaterialCode()).getId());
                pmsProducePlan.setCustomerCode(customerCodeMap.get(excelDTO.getCustomerMaterialCode()).getOfficeCode());
                pmsProducePlan.setCustomerName(customerCodeMap.get(excelDTO.getCustomerMaterialCode()).getOfficeName());
                //获取当日的数量
                String day = localDate.toString().substring(8, 10);
                if (PmsCodeUtil.checkZero(day)) {
                    day = day.substring(1);
                }
                pmsProducePlan.setQuantity(getQuantity(excelDTO, day));
                pmsProducePlan.setPlanDate(localDate);
                pmsProducePlan.setType(OaConstant.YES);
                pmsProducePlan.setStatus(0);
                //如果等于0表示不覆盖
                if (!Objects.isNull(pmsProducePlan.getQuantity())) {
//                    if (updateFlag.equals(0)) {
//                        addList.add(pmsProducePlan);
//                    } else {
                        //判断是否有存在的物料信息
                        Optional<ProducePlanVO> optional = producePlanVOList.stream().filter(producePlanVO -> localDate.equals(producePlanVO.getPlanDate())
                                && Objects.equals(excelDTO.getMaterialCode(), producePlanVO.getMaterialCode())).findAny();
                        if (optional.isPresent()) {
                            pmsProducePlan.setId(optional.get().getId());
                            updateList.add(pmsProducePlan);
                        } else {
                            addList.add(pmsProducePlan);
                        }
//                    }
                }
            }
        }
        if (CollUtil.isNotEmpty(addList)) {
            //新增list去重
            addList = addList.stream().collect(
                    Collectors.collectingAndThen(
                            Collectors.toCollection(
                                    () -> new TreeSet<PmsProducePlan>(Comparator.comparing(p -> p.getPlanDate() + "," + p.getMaterialId()))), ArrayList::new));
            Set<Long> materialId = addList.stream().map(PmsProducePlan::getMaterialId).collect(Collectors.toSet());
            TupleDTO<LocalDateTime, LocalDateTime> dateRange = LocalDateUtil.calculateTimeRange(addList.stream().map(v -> v.getPlanDate().atStartOfDay()).collect(Collectors.toSet()));
            //新增之前删除所有相关天数据
            List<PmsProducePlan> producePlans = list(new LambdaQueryWrapper<PmsProducePlan>()
                    .eq(PmsProducePlan::getType, OaConstant.YES)
                    .in(PmsProducePlan::getMaterialId, materialId)
                    .between(PmsProducePlan::getPlanDate, dateRange.getField1(), dateRange.getField2()));

            addList.forEach(tmp -> {
                PmsProducePlan pmsProducePlan = producePlans.stream().filter(v -> v.getMaterialId().equals(tmp.getMaterialId())
                        && v.getPlanDate().equals(tmp.getPlanDate())
                        && tmp.getCustomerId().equals(v.getCustomerId())).findFirst().orElse(null);
                if(Objects.nonNull(pmsProducePlan)){
                    removeById(pmsProducePlan.getId());
                }
            });
            if (!saveBatch(addList)) {
                throw new BusinessException("生产月计划导入新增失败");
            }
        }
        if (CollUtil.isNotEmpty(updateList)) {
            if (!updateBatchById(updateList)) {
                throw new BusinessException("生产月计划导入修改失败");
            }
        }
        return planImportVO;
    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public ProducePlanImportVO excelImportData(PmExcelDTO dto) throws IOException {
//        Integer updateFlag = dto.getUpdateFlag();
//        //文件类型校验
//        if (Objects.nonNull(dto.getFile())) {
//            //获取文件拓展名
//            String fileExtension = Objects.requireNonNull(dto.getFile().getOriginalFilename()).substring(dto.getFile().getOriginalFilename().lastIndexOf("."));
//            if (!fileExtension.equals(PmManagerConstant.FILE_EXTENSION_XLSX)) {
//                throw new BusinessException("文件类型不支持,请使用模板进行导入");
//            }
//        }
//
//        List<PmsProducePlanExcelDTO> dtoList = new ArrayList<>();
//        List<ProducePlanMsgVO> planMsgVOList = new ArrayList<>();
//
//        EasyExcel.read(dto.getFile().getInputStream(), PmsProducePlanExcelDTO.class, new ExcelListener(dtoList,planMsgVOList)).sheet().doRead();
//
//
//        List<String> codeList = dtoList.stream().map(PmsProducePlanExcelDTO::getMaterialCode).collect(Collectors.toList());
//        Map<String, EqpSpareVO> spareCodeMap = remoteSpareFeignApi.listByCode(codeList).getData();
//        //获取已存在的物料信息
//        List<ProducePlanVO> producePlanVOList = getUpdateMap(dtoList, codeList);
//        //校验导入内容
//        planMsgVOList.addAll(checkPlanExcelDTO(dtoList, updateFlag, spareCodeMap, producePlanVOList));
//
//        //如果错误信息不为空组装数据返回
//        ProducePlanImportVO planImportVO = new ProducePlanImportVO();
//        if (CollectionUtils.isNotEmpty(planMsgVOList)) {
//            planImportVO.setImportFlag(false);
//            planImportVO.setProducePlanMsgVOList(planMsgVOList);
//            return planImportVO;
//        }
//
//        planImportVO.setImportFlag(true);
//        List<PmsProducePlan> addList = new ArrayList<>();
//        List<PmsProducePlan> updateList = new ArrayList<>();
//
//
//        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
//            LocalDate planDate = LocalDate.parse(excelDTO.getPlanDate(), ISO_DATE);
//            PmsProducePlan pmsProducePlan = new PmsProducePlan();
//            pmsProducePlan.setMaterialId(spareCodeMap.get(excelDTO.getMaterialCode()).getId());
//            pmsProducePlan.setQuantity(excelDTO.getQuantity());
//            pmsProducePlan.setPlanDate(planDate);
//            pmsProducePlan.setStatus(0);
//
//            //如果等于0表示不覆盖
//            if (updateFlag.equals(0)) {
//                addList.add(pmsProducePlan);
//            } else {
//                //判断是否有存在的物料信息
//                Optional<ProducePlanVO> optional = producePlanVOList.stream().filter(producePlanVO -> planDate.equals(producePlanVO.getPlanDate())
//                        && Objects.equals(excelDTO.getMaterialCode(), producePlanVO.getMaterialCode())).findAny();
//                if (optional.isPresent()) {
//                    pmsProducePlan.setId(optional.get().getId());
//                    updateList.add(pmsProducePlan);
//                } else {
//                    addList.add(pmsProducePlan);
//                }
//            }
//
//        }
//        if (CollectionUtils.isNotEmpty(addList)) {
//            //新增list去重
//            addList = addList.stream().collect(
//                    Collectors.collectingAndThen(
//                            Collectors.toCollection(
//                                    () -> new TreeSet<PmsProducePlan>(Comparator.comparing(p -> p.getPlanDate() + "," + p.getMaterialId()))), ArrayList::new));
//            if (!saveBatch(addList)) {
//                throw new BusinessException("生产月计划导入新增失败");
//            }
//        }
//        if (CollectionUtils.isNotEmpty(updateList)) {
//            if (!updateBatchById(updateList)) {
//                throw new BusinessException("生产月计划导入修改失败");
//            }
//        }
//
//        return planImportVO;
//    }

    @SneakyThrows
    @Override
    public void excelExportData(HttpServletRequest request, HttpServletResponse response) {
        String monthDate = request.getParameter("monthDate");
        List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().like(PmsProducePlan::getPlanDate, monthDate + "%"));
        //查询物料信息
        Set<Long> materialIdSet = producePlanList.stream().map(PmsProducePlan::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> materialMap = remoteSpareFeignApi.listById(new ArrayList<>(materialIdSet)).getData();
        List<PmsProducePlanExcelDTO> excelDTOList = new ArrayList<>();
        for (PmsProducePlan pmsProducePlan : producePlanList) {
            PmsProducePlanExcelDTO planExcelDTO = new PmsProducePlanExcelDTO();
            planExcelDTO.setMaterialCode(materialMap.get(pmsProducePlan.getMaterialId()).getCode());
            planExcelDTO.setMaterialName(materialMap.get(pmsProducePlan.getMaterialId()).getName());
            planExcelDTO.setPlanDate(LocalDateUtil.localDateFormat(pmsProducePlan.getPlanDate(), DatePattern.NORM_DATE_PATTERN));
            planExcelDTO.setQuantity(pmsProducePlan.getQuantity());
            excelDTOList.add(planExcelDTO);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("生产月计划", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PmsProducePlanExcelDTO.class).sheet("模板").doWrite(excelDTOList);
    }

    @Override
    public Boolean order(PmsProducePlanOrderDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.PLAN_ORDER);
        lock.lock();
        try {
            // 处理时间格式
            int year = Integer.parseInt(dto.getPlanDate().substring(0, 4));
            int mouth = Integer.parseInt(dto.getPlanDate().substring(5));
            LocalDate date = LocalDate.of(year, mouth, 01);
            // 获取当前月的第一天
            dto.setFirstDay(date.with(TemporalAdjusters.firstDayOfMonth()));
            // 获取当前月的最后一天
            dto.setLastDay(date.with(TemporalAdjusters.lastDayOfMonth()));
            List<PmsProducePlan> planList = list(new QueryWrapper<PmsProducePlan>()
                    .select("material_id,sum(quantity) as quantity")
                    .lambda().in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
                    .between(PmsProducePlan::getPlanDate, dto.getFirstDay(), dto.getLastDay())
                    .groupBy(PmsProducePlan::getMaterialId));
            Assert.isTrue(CollUtil.isNotEmpty(planList), "需要下达的物料数据不存在");
            //2.修改下达后物料的状态
            Set<Long> materialIdList = Sets.newHashSet();
            planList.forEach(pmsProducePlan -> {
                materialIdList.add(pmsProducePlan.getMaterialId());
            });
            //3.自定义sql 批量修改状态
            Boolean aBoolean = this.baseMapper.updateBatchStatus(materialIdList, dto.getFirstDay(), dto.getLastDay(), PmManagerConstant.OrderStateEnum.ONE.getCode());
            Assert.isTrue(aBoolean,
                    "修改物料下达状态失败");
            //1.获取需要下达的物料计划数据
            List<PmsProducePlan> list = list(new LambdaQueryWrapper<PmsProducePlan>()
                    .in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
                    .notIn(PmsProducePlan::getQuantity,BigDecimal.ZERO)
                    .between(PmsProducePlan::getPlanDate, dto.getFirstDay(), dto.getLastDay()));
            Map<Long, List<PmsProducePlan>> collect = list.stream().collect(Collectors.groupingBy(PmsProducePlan::getMaterialId));
            Set<String> codes = Sets.newHashSet();
            codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
            codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
//        codes.add(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT);
            Map<String, SysDictVO> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByCodes(codes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Long> ids = Sets.newHashSet();
            Map<Long, MaterialInventoryDTO> inventoryDTOMap = FeignOptional.of(()->remoteWmsInventoryFeignApi.materialInventory(Sets.newHashSet(dto.getMaterialIds())),FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            MaterialInventoryDTO mesMaterialInventoryDTO = new MaterialInventoryDTO();
            mesMaterialInventoryDTO.setAvailableQuantity(BigDecimal.ZERO);
            //获取独立需求类型id
            Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
            //获取订单需求类型id
            Long mrpRequirmentId = FeignOptional.of(()->remoteDictFeignApi.getByCode(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, PmManagerConstant.MRP_TYPE),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId();
            dictMap.forEach((key, value) -> ids.add(value.getId()));
            List<PmsMrpInfo> infoList = Lists.newArrayList();
            collect.forEach((key, value) -> {
                //1.1 清除物料的独立需求计划以及计划订单的mrp数据
                pmsMrpInfoService.remove(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key).in(PmsMrpInfo::getMrpTypeId, ids));
                //1.2 获取物料已有的mrp计划
                List<PmsMrpInfo> mrpInfoList = pmsMrpInfoService.list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key));
                //1.3 获取物料的库存可用信息
                BigDecimal availableQuantity = inventoryDTOMap.getOrDefault(key, mesMaterialInventoryDTO).getAvailableQuantity();
                //1.4 对物料计划数据生成 mrp需求数据
                for (PmsProducePlan pmsProducePlan : value) {
                    infoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(), BigDecimal.ZERO, pmsProducePlan.getMaterialId(),
                            pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode(), null));
                }
                //1.5 合并物料的mrp计划数据进行mrp计算
                infoList.addAll(mrpInfoList);
                List<PmsMrpInfo> sortedList = infoList.stream().sorted(Comparator.comparing(PmsMrpInfo::getMrpDate)).collect(Collectors.toList());
                LinkedHashMap<LocalDate, List<PmsMrpInfo>> dateMap = sortedList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMrpDate, LinkedHashMap::new, Collectors.toList()));
                //1.6 进行mrp计算并生成计划订单
                for (Map.Entry<LocalDate, List<PmsMrpInfo>> entry : dateMap.entrySet()) {
                    BigDecimal demandQuantity = BigDecimal.ZERO;
                    BigDecimal supplyQuantity = BigDecimal.ZERO;
                    for (PmsMrpInfo pmsMrpInfo : entry.getValue()) {
                        if (pmsMrpInfo.getMrpTypeId().equals(mrpDemandId) || pmsMrpInfo.getMrpTypeId().equals(mrpRequirmentId)) {
                            demandQuantity = demandQuantity.add(pmsMrpInfo.getQuantity());
                        } else {
                            supplyQuantity = supplyQuantity.add(pmsMrpInfo.getQuantity());
                        }
                    }
                    //如果供给小于需求，则考虑库存
                    if (supplyQuantity.compareTo(demandQuantity) < 0) {
                        //如果库存小于需求减去供给,则创建计划订单
                        if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) < 0) {
                            // 计划订单数量 库存+供给-需求
                            //计算生成计划订单的数量
                            BigDecimal negate = availableQuantity.add(supplyQuantity).subtract(demandQuantity).negate();
                            infoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PLANNED_ORDER, negate, BigDecimal.ZERO,
                                    key, entry.getKey(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode(), null));
                            availableQuantity = BigDecimal.ZERO;
                        } else if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) > 0) {
                            //如果库存大于需求减去供给
                            availableQuantity = availableQuantity.subtract(demandQuantity.subtract(supplyQuantity));
                        } else {
                            availableQuantity = BigDecimal.ZERO;
                        }
                    } else if (supplyQuantity.compareTo(demandQuantity) > 0) {
                        //计算库存数量  库存 +（供给-需求）
                        availableQuantity = availableQuantity.add(supplyQuantity.subtract(demandQuantity));
                    }
                }
            });
            return pmsMrpInfoService.saveOrUpdateBatch(infoList);
        } finally {
            lock.unlock();
        }
    }


//    @Override
//    public void turnProduceOrder(PmsProduceOrderDTO model) {
//        List<PmsMrpInfo> infoList = Lists.newArrayList();
//        // 获取转订单的mrp计划订单数据
//        PmsMrpInfo planInfo = pmsMrpInfoService.getById(model.getMrpId());
//        Set<String> codes = Sets.newHashSet();
//        codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
//        codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
//        Map<String, SysDictVO> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByCodes(codes)).get();
//        Set<Long> ids = Sets.newHashSet();
//        ids.add(dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId());
//        ids.add(dictMap.get(PmManagerConstant.MRP_PLANNED_ORDER).getId());
//        //获取独立需求类型id
//        Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
//        // 清除物料的独立需求计划以及计划订单的mrp数据
//        pmsMrpInfoService.remove(new LambdaQueryWrapper<PmsMrpInfo>()
//                .eq(PmsMrpInfo::getMaterialId, model.getMaterialId())
//                .in(PmsMrpInfo::getMrpTypeId, ids));
//        // 获取物料的库存可用信息
//        Set<Long> materialIds = Sets.newHashSet();
//        materialIds.add(model.getMaterialId());
//        Map<Long, MesMaterialInventoryDTO> inventoryMap = remoteMessFeignApi.materialInventory(materialIds);
//        BigDecimal availableQuantity = inventoryMap.get(model.getMaterialId()).getAvailableQuantity();
//        LocalDate mrpDate = planInfo.getMrpDate();
//        // 获取物料已有的mrp计划
//        List<PmsMrpInfo> mrpInfoList = pmsMrpInfoService.list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, model.getMaterialId()));
//        // 对当前订单生成生产订单mrp计划数据
//        infoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PRODUCE_ORDER, model.getQuantity(), BigDecimal.ZERO, model.getMaterialId(),
//                planInfo.getMrpDate(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//        // 获取当月第一天
//        LocalDate firstDay = mrpDate.with(TemporalAdjusters.firstDayOfMonth());
//        // 获取当前月的最后一天
//        LocalDate lastDay = mrpDate.with(TemporalAdjusters.lastDayOfMonth());
//        //1.4 获取物料计划数据生成 mrp需求数据
//        List<PmsProducePlan> list = list(new LambdaQueryWrapper<PmsProducePlan>()
//                .eq(PmsProducePlan::getMaterialId, model.getMaterialId())
//                .between(PmsProducePlan::getPlanDate, firstDay, lastDay));
//        list.forEach(pmsProducePlan -> {
//            infoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(), BigDecimal.ZERO, pmsProducePlan.getMaterialId(),
//                    pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//        });
//        //1.5 合并物料的mrp计划数据进行mrp计算
//        infoList.addAll(mrpInfoList);
//        List<PmsMrpInfo> sortedList = infoList.stream().sorted(Comparator.comparing(PmsMrpInfo::getMrpDate)).collect(Collectors.toList());
//        LinkedHashMap<LocalDate, List<PmsMrpInfo>> dateMap = sortedList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMrpDate, LinkedHashMap::new, Collectors.toList()));
//        //1.6 进行mrp计算并生成计划订单
//        for (Map.Entry<LocalDate, List<PmsMrpInfo>> entry : dateMap.entrySet()) {
//            BigDecimal demandQuantity = BigDecimal.ZERO;
//            BigDecimal supplyQuantity = BigDecimal.ZERO;
//            for (PmsMrpInfo pmsMrpInfo : entry.getValue()) {
//                if (pmsMrpInfo.getMrpTypeId().equals(mrpDemandId)) {
//                    demandQuantity = demandQuantity.add(pmsMrpInfo.getQuantity());
//                } else {
//                    supplyQuantity = supplyQuantity.add(pmsMrpInfo.getQuantity());
//                }
//            }
//            //如果供给小于需求，则考虑库存
//            if (supplyQuantity.compareTo(demandQuantity) < 0) {
//                //如果库存小于需求减去供给,则创建计划订单
//                if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) < 0) {
//                    // 计划订单数量 库存+供给-需求
//                    //计算生成计划订单的数量
//                    BigDecimal negate = availableQuantity.add(supplyQuantity).subtract(demandQuantity).negate();
//                    infoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PLANNED_ORDER, negate, BigDecimal.ZERO,
//                            model.getMaterialId(), entry.getKey(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//                    availableQuantity = BigDecimal.ZERO;
//                } else if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) > 0) {
//                    //如果库存大于需求减去供给
//                    availableQuantity = availableQuantity.subtract(demandQuantity.subtract(supplyQuantity));
//                } else {
//                    availableQuantity = BigDecimal.ZERO;
//                }
//            } else if (supplyQuantity.compareTo(demandQuantity) > 0) {
//                //计算库存数量  库存 +（供给-需求）
//                availableQuantity = availableQuantity.add(supplyQuantity.subtract(demandQuantity));
//            }
//        }
//        pmsMrpInfoService.saveOrUpdateBatch(infoList);
//    }

    @Override
    public void turnProduceOrder(PmsProduceOrderDTO model) {
        List<PmsMrpInfo> infoList = Lists.newArrayList();
        List<Long> materialIds = Lists.newArrayList();
        materialIds.add(model.getMaterialId());
        // 获取转订单的mrp计划订单数据
        PmsMrpInfo planInfo = pmsMrpInfoService.getById(model.getMrpId());
        //获取当月第一天
        LocalDate firstDay = planInfo.getMrpDate().with(TemporalAdjusters.firstDayOfMonth());
        // 获取当前月的最后一天
        LocalDate lastDay = planInfo.getMrpDate().with(TemporalAdjusters.lastDayOfMonth());
        List<PmsMrpInfo> moduleInfoList = Lists.newArrayList();
        // 对当前订单主物料生成生产订单mrp计划数据
        moduleInfoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PRODUCE_ORDER, model.getQuantity(), BigDecimal.ZERO, model.getMaterialId(),
                planInfo.getMrpDate(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode(), model.getCode()));
        // 获取当前的子物料生成订单需求
        model.getMaterialList().forEach(material -> {
            materialIds.add(material.getId());
            moduleInfoList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, material.getQuantity(), BigDecimal.ZERO, material.getModuleId(),
                    planInfo.getMrpDate(), planInfo.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode(), model.getCode()));
        });
        Map<Long, List<PmsMrpInfo>> moduleMap = moduleInfoList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMaterialId));
        Set<String> codes = Sets.newHashSet();
        codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
        codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
        Map<String, SysDictVO> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByCodes(codes),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Long> ids = Sets.newHashSet();
        Map<Long, MaterialInventoryDTO> inventoryDTOMap = FeignOptional.of(()->remoteWmsInventoryFeignApi.materialInventory(Sets.newHashSet(materialIds)),FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //获取独立需求类型id
        Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
        //获取订单需求类型id
        Long mrpRequirmentId = FeignOptional.of(()->remoteDictFeignApi.getByCode(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, PmManagerConstant.MRP_TYPE),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId();
        dictMap.forEach((key, value) -> ids.add(value.getId()));
        MaterialInventoryDTO inventoryDTO = new MaterialInventoryDTO();
        inventoryDTO.setAvailableQuantity(BigDecimal.ZERO);
        moduleMap.forEach((key, value) -> {
            List<PmsMrpInfo> mrpList = Lists.newArrayList();
            mrpList.addAll(value);
            // 获取物料的计划数据
            List<PmsProducePlan> list = list(new LambdaQueryWrapper<PmsProducePlan>()
                    .eq(PmsProducePlan::getMaterialId, key)
                    .between(PmsProducePlan::getPlanDate, firstDay, lastDay));
            //1.1 清除物料的独立需求计划以及计划订单的mrp数据
            pmsMrpInfoService.remove(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key).in(PmsMrpInfo::getMrpTypeId, ids));
            //1.2 获取物料已有的mrp计划
            List<PmsMrpInfo> mrpInfoList = pmsMrpInfoService.list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key));
            //1.3 获取物料的库存可用信息
            MaterialInventoryDTO inventory = inventoryDTOMap.getOrDefault(key, inventoryDTO);
            BigDecimal availableQuantity = inventory.getAvailableQuantity();
            //1.4 对物料计划数据生成 mrp需求数据
            if (CollUtil.isNotEmpty(list)) {
                for (PmsProducePlan pmsProducePlan : list) {
                    mrpList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(), BigDecimal.ZERO, pmsProducePlan.getMaterialId(),
                            pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode(), null));
                }
            }
            mrpList.addAll(mrpInfoList);
            List<PmsMrpInfo> sortedList = mrpList.stream().sorted(Comparator.comparing(PmsMrpInfo::getMrpDate)).collect(Collectors.toList());
            LinkedHashMap<LocalDate, List<PmsMrpInfo>> dateMap = sortedList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMrpDate, LinkedHashMap::new, Collectors.toList()));
            //1.6 进行mrp计算并生成计划订单
            for (Map.Entry<LocalDate, List<PmsMrpInfo>> entry : dateMap.entrySet()) {
                BigDecimal demandQuantity = BigDecimal.ZERO;
                BigDecimal supplyQuantity = BigDecimal.ZERO;
                for (PmsMrpInfo pmsMrpInfo : entry.getValue()) {
                    if (pmsMrpInfo.getMrpTypeId().equals(mrpDemandId) || pmsMrpInfo.getMrpTypeId().equals(mrpRequirmentId)) {
                        demandQuantity = demandQuantity.add(pmsMrpInfo.getQuantity());
                    } else {
                        supplyQuantity = supplyQuantity.add(pmsMrpInfo.getQuantity());
                    }
                }
                //如果供给小于需求，则考虑库存
                if (supplyQuantity.compareTo(demandQuantity) < 0) {
                    //如果库存小于需求减去供给,则创建计划订单
                    if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) < 0) {
                        // 计划订单数量 库存+供给-需求
                        //计算生成计划订单的数量
                        BigDecimal negate = availableQuantity.add(supplyQuantity).subtract(demandQuantity).negate();
                        mrpList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PLANNED_ORDER, negate, BigDecimal.ZERO,
                                key, entry.getKey(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode(), null));
                        availableQuantity = BigDecimal.ZERO;
                    } else if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) > 0) {
                        //如果库存大于需求减去供给
                        availableQuantity = availableQuantity.subtract(demandQuantity.subtract(supplyQuantity));
                    } else {
                        availableQuantity = BigDecimal.ZERO;
                    }
                } else if (supplyQuantity.compareTo(demandQuantity) > 0) {
                    //计算库存数量  库存 +（供给-需求）
                    availableQuantity = availableQuantity.add(supplyQuantity.subtract(demandQuantity));
                }
            }
            infoList.addAll(mrpList);
        });
        pmsMrpInfoService.saveOrUpdateBatch(infoList);
//        // 获取需要下达的物料计划数据
//        List<PmsProducePlan> list = list(new LambdaQueryWrapper<PmsProducePlan>()
//                .in(PmsProducePlan::getMaterialId, materialIds)
//                .between(PmsProducePlan::getPlanDate, firstDay, lastDay));
//        Map<Long, List<PmsProducePlan>> collect = list.stream().collect(Collectors.groupingBy(PmsProducePlan::getMaterialId));
//
//        collect.forEach((key, value) -> {
//            List<PmsMrpInfo> mrpList = Lists.newArrayList();
//            moduleMap.forEach((modelId, modelList) -> {
//                if (key.equals(modelId)) {
//                    mrpList.addAll(modelList);
//                }
//            });
//            //1.1 清除物料的独立需求计划以及计划订单的mrp数据
//            pmsMrpInfoService.remove(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key).in(PmsMrpInfo::getMrpTypeId, ids));
//            //1.2 获取物料已有的mrp计划
//            List<PmsMrpInfo> mrpInfoList = pmsMrpInfoService.list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key));
//            //1.3 获取物料的库存可用信息
//            BigDecimal availableQuantity = inventoryDTOMap.get(key).getAvailableQuantity();
//            //1.4 对物料计划数据生成 mrp需求数据
//            for (PmsProducePlan pmsProducePlan : value) {
//                mrpList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(), BigDecimal.ZERO, pmsProducePlan.getMaterialId(),
//                        pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//            }
//            //1.5 合并物料的mrp计划数据进行mrp计算
//            mrpList.addAll(mrpInfoList);
//            List<PmsMrpInfo> sortedList = mrpList.stream().sorted(Comparator.comparing(PmsMrpInfo::getMrpDate)).collect(Collectors.toList());
//            LinkedHashMap<LocalDate, List<PmsMrpInfo>> dateMap = sortedList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMrpDate, LinkedHashMap::new, Collectors.toList()));
//            //1.6 进行mrp计算并生成计划订单
//            for (Map.Entry<LocalDate, List<PmsMrpInfo>> entry : dateMap.entrySet()) {
//                BigDecimal demandQuantity = BigDecimal.ZERO;
//                BigDecimal supplyQuantity = BigDecimal.ZERO;
//                for (PmsMrpInfo pmsMrpInfo : entry.getValue()) {
//                    if (pmsMrpInfo.getMrpTypeId().equals(mrpDemandId) || pmsMrpInfo.getMrpTypeId().equals(mrpRequirmentId)) {
//                        demandQuantity = demandQuantity.add(pmsMrpInfo.getQuantity());
//                    } else {
//                        supplyQuantity = supplyQuantity.add(pmsMrpInfo.getQuantity());
//                    }
//                }
//                //如果供给小于需求，则考虑库存
//                if (supplyQuantity.compareTo(demandQuantity) < 0) {
//                    //如果库存小于需求减去供给,则创建计划订单
//                    if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) < 0) {
//                        // 计划订单数量 库存+供给-需求
//                        //计算生成计划订单的数量
//                        BigDecimal negate = availableQuantity.add(supplyQuantity).subtract(demandQuantity).negate();
//                        mrpList.add(pmsMrpInfoService.create(PmManagerConstant.MRP_PLANNED_ORDER, negate, BigDecimal.ZERO,
//                                key, entry.getKey(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//                        availableQuantity = BigDecimal.ZERO;
//                    } else if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) > 0) {
//                        //如果库存大于需求减去供给
//                        availableQuantity = availableQuantity.subtract(demandQuantity.subtract(supplyQuantity));
//                    } else {
//                        availableQuantity = BigDecimal.ZERO;
//                    }
//                } else if (supplyQuantity.compareTo(demandQuantity) > 0) {
//                    //计算库存数量  库存 +（供给-需求）
//                    availableQuantity = availableQuantity.add(supplyQuantity.subtract(demandQuantity));
//                }
//            }
//            infoList.addAll(mrpList);
//        });

    }


//    @Override
//    public Boolean order(PmsProducePlanOrderDTO dto) {
//        //1.查询需要下达的物料计划数据
//        //处理时间格式
//        int year = Integer.parseInt(dto.getPlanDate().substring(0, 4));
//        int mouth = Integer.parseInt(dto.getPlanDate().substring(5));
//        LocalDate date = LocalDate.of(year, mouth, 01);
//        // 获取当前月的第一天
//        dto.setFirstDay(date.with(TemporalAdjusters.firstDayOfMonth()));
//        // 获取当前月的最后一天
//        dto.setLastDay(date.with(TemporalAdjusters.lastDayOfMonth()));
//        List<PmsProducePlan> list = list(new QueryWrapper<PmsProducePlan>()
//                .select("material_id,sum(quantity) as quantity")
//                .lambda().in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
//                .between(PmsProducePlan::getPlanDate, dto.getFirstDay(), dto.getLastDay())
//                .groupBy(PmsProducePlan::getMaterialId));
//        Assert.isTrue(CollUtil.isNotEmpty(list), "需要下达的物料数据不存在");
//        //2.修改下达后物料的状态
//        Set<Long> materialIdList = Sets.newHashSet();
//        list.forEach(pmsProducePlan -> {
//            pmsProducePlan.setStatus(PmManagerConstant.OrderStateEnum.ONE.getCode());
//            materialIdList.add(pmsProducePlan.getMaterialId());
//        });
//        //3.自定义sql 批量修改状态
//        Boolean aBoolean = this.baseMapper.updateBatchStatus(materialIdList, dto.getFirstDay(), dto.getLastDay(), PmManagerConstant.OrderStateEnum.ONE.getCode());
//        Assert.isTrue(aBoolean,
//                "修改物料下达状态失败");
//        //3.对物料列表进行MRP计算
//        return pmsMrpInfoService.mrpAdd(dto);
//    }

    private List<ProducePlanMsgVO> checkPlanExcelDTO(List<PmsProducePlanNewExcelDTO> dtoList, Integer updateFlag, Map<String, EqpSpareVO> map, List<ProducePlanVO> producePlanVOList,Map<String,SysOfficeVO> customerCodeMap) {

        List<ProducePlanMsgVO> planMsgVOList = new ArrayList<>();

        for (PmsProducePlanNewExcelDTO excelDTO : dtoList) {
            ProducePlanMsgVO planMsgVO = new ProducePlanMsgVO();
            List<String> errorMsgList = new ArrayList<>();
            if (Objects.isNull(excelDTO.getMonth())) {
                errorMsgList.add("月份不能为空");
            } else {
                if (!LocalDateUtil.checkYears(excelDTO.getMonth())) {
                    errorMsgList.add("计划日期格式错误格式应为“1970-01”");
                } else {
                    //校验数据是否存在
                    if (updateFlag.equals(PmManagerConstant.OrderStateEnum.ZERO.getCode())) {
                        LocalDate date = LocalDate.parse(excelDTO.getMonth() + PmsCodeUtil.SHORT_LINE  + PmReportConstant.date_01, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        List<LocalDate> currentMonthDateAll = LocalDateUtil.getCurrentMonthDateAll(date);
                        for (LocalDate localDate : currentMonthDateAll) {
                            for (ProducePlanVO producePlanVO : producePlanVOList) {
                                if (excelDTO.getMaterialCode().equals(producePlanVO.getMaterialCode()) && localDate.equals(producePlanVO.getPlanDate())) {
                                    errorMsgList.add(localDate +"物料计划数据已存在");
                                }
                            }
                        }
                    }
                }
            }
            if (Objects.isNull(map.get(excelDTO.getMaterialCode()))) {
                errorMsgList.add("物料信息不存在");
            }

            if (Objects.isNull(customerCodeMap.get(excelDTO.getCustomerMaterialCode()))) {
                errorMsgList.add("客户信息不存在");
            }


            //获取当前行的错误信息
            planMsgVO.setErrorMsgList(StringUtils.join(errorMsgList, ","));
            //如果错误信息不为空获取行号
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                planMsgVO.setRowIndex(excelDTO.getRowIndex());
                planMsgVOList.add(planMsgVO);
            }
        }

        return planMsgVOList;
    }

//    private List<ProducePlanMsgVO> checkPlanExcelDTO(List<PmsProducePlanExcelDTO> dtoList, Integer updateFlag, Map<String, EqpSpareVO> map, List<ProducePlanVO> producePlanVOList) {
//
//        List<ProducePlanMsgVO> planMsgVOList = new ArrayList<>();
//
//        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
//            ProducePlanMsgVO planMsgVO = new ProducePlanMsgVO();
//            List<String> errorMsgList = new ArrayList<>();
//            if (Objects.isNull(excelDTO.getPlanDate())) {
//                errorMsgList.add("计划日期不能为空");
//            } else {
//                if (!LocalDateUtil.checkLocalDate(excelDTO.getPlanDate())) {
//                    errorMsgList.add("计划日期格式错误格式应为“1970-01-01”");
//                } else {
//                    LocalDate planDate = LocalDate.parse(excelDTO.getPlanDate(), ISO_DATE);
//                    if (planDate.isBefore(LocalDate.now())) {
//                        errorMsgList.add("日期小于当前日期");
//                    }
//                    Optional<ProducePlanVO> optional = producePlanVOList.stream().filter(producePlanVO -> planDate.equals(producePlanVO.getPlanDate())
//                            && Objects.equals(excelDTO.getMaterialCode(), producePlanVO.getMaterialCode())).findAny();
//                    if (optional.isPresent() && updateFlag.equals(0)) {
//                        errorMsgList.add("物料计划数据库已存在");
//                    }
//                }
//            }
//            if (Objects.isNull(excelDTO.getQuantity())) {
//                errorMsgList.add("数量不能为空");
//            }
//            if (Objects.isNull(map.get(excelDTO.getMaterialCode()))) {
//                errorMsgList.add("物料信息不存在");
//            }
//
//
//            //过滤出同一个月编码重复的数据
//            //过滤出本行以外的重复数据
//            List<PmsProducePlanExcelDTO> pmsProducePlanExcelDTOList = dtoList
//                    .stream()
//                    .filter(dto -> Objects.equals(dto.getMaterialCode(), excelDTO.getMaterialCode()) && Objects.equals(dto.getPlanDate(), excelDTO.getPlanDate()))
//                    .filter(dto -> !dto.getRowIndex().equals(excelDTO.getRowIndex())).collect(Collectors.toList());
//            String rows = StringUtils.join(pmsProducePlanExcelDTOList.stream().map(PmsProducePlanExcelDTO::getRowIndex).collect(Collectors.toList()), ",");
//            if (updateFlag.equals(0) && pmsProducePlanExcelDTOList.size() > 0) {
//                errorMsgList.add("和第" + rows + "行物料编码重复");
//            }
//
//            //获取当前行的错误信息
//            planMsgVO.setErrorMsgList(StringUtils.join(errorMsgList, ","));
//            //如果错误信息不为空获取行号
//            if (CollectionUtils.isNotEmpty(errorMsgList)) {
//                planMsgVO.setRowIndex(excelDTO.getRowIndex());
//                planMsgVOList.add(planMsgVO);
//            }
//        }
//
//        return planMsgVOList;
//    }


    private List<ProducePlanVO> getUpdateMap(List<PmsProducePlanNewExcelDTO> dtoList, List<String> codeList) {
        List<LocalDate> localDateList = Lists.newArrayList();
        //处理时间格式
        for (PmsProducePlanNewExcelDTO planExcelDTO : dtoList) {
            //获取当前月的天数
            LocalDate date = LocalDate.parse(planExcelDTO.getMonth() + PmsCodeUtil.SHORT_LINE + PmReportConstant.date_01, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<LocalDate> currentMonthDateAll = LocalDateUtil.getCurrentMonthDateAll(date);
            localDateList.addAll(currentMonthDateAll);
        }
        List<ProducePlanVO> producePlanVOList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(localDateList)) {
            //根据导入的日期查询数据
            List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().in(PmsProducePlan::getPlanDate, localDateList));
            //根据物料code查询物料信息
            Map<Long, EqpSpareVO> spareMap = remoteSpareFeignApi.mapByCode(codeList).getData();

            //获取所有日期查询数据的物料code
            for (PmsProducePlan pmsProducePlan : producePlanList) {
                ProducePlanVO producePlanVO = new ProducePlanVO();
                EqpSpareVO spareVO = spareMap.get(pmsProducePlan.getMaterialId());
                if (!Objects.isNull(spareVO)) {
                    BeanUtils.copyProperties(pmsProducePlan, producePlanVO);
                    producePlanVO.setMaterialCode(spareVO.getCode());
                    producePlanVOList.add(producePlanVO);
                }
            }
        }
        return producePlanVOList;
    }


//    private List<ProducePlanVO> getUpdateMap(List<PmsProducePlanExcelDTO> dtoList, List<String> codeList) {
//        List<LocalDate> localDateList = new ArrayList<>();
//        for (PmsProducePlanExcelDTO planExcelDTO : dtoList) {
//            LocalDate localDate = null;
//            if (LocalDateUtil.checkLocalDate(planExcelDTO.getPlanDate())) {
//                localDate = LocalDate.parse(planExcelDTO.getPlanDate(), ISO_DATE);
//            } else {
//                continue;
//            }
//            localDateList.add(localDate);
//        }
//
//        List<ProducePlanVO> producePlanVOList = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(localDateList)) {
//            //根据导入的日期查询数据
//            List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().in(PmsProducePlan::getPlanDate, localDateList));
//            //根据物料code查询物料信息
//            Map<Long, EqpSpareVO> spareMap = remoteSpareFeignApi.mapByCode(codeList).getData();
//
//            //获取所有日期查询数据的物料code
//            for (PmsProducePlan pmsProducePlan : producePlanList) {
//                ProducePlanVO producePlanVO = new ProducePlanVO();
//                EqpSpareVO spareVO = spareMap.get(pmsProducePlan.getMaterialId());
//                if (!Objects.isNull(spareVO)) {
//                    BeanUtils.copyProperties(pmsProducePlan, producePlanVO);
//                    producePlanVO.setMaterialCode(spareVO.getCode());
//                    producePlanVOList.add(producePlanVO);
//                }
//            }
//        }
//        return producePlanVOList;
//    }

//    @Override
//    public Boolean order(PmsProducePlanOrderDTO dto) {
//        //1.查询需要下达的物料计划数据
//        //处理时间格式
//        int year = Integer.parseInt(dto.getPlanDate().substring(0, 4));
//        int mouth = Integer.parseInt(dto.getPlanDate().substring(5));
//        LocalDate date = LocalDate.of(year, mouth, 01);
//        // 获取当前月的第一天
//        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth());
//        // 获取当前月的最后一天
//        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());
//        List<PmsProducePlan> list = list(new QueryWrapper<PmsProducePlan>()
//                .select("material_id,sum(quantity) as quantity")
//                .lambda().in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
//                .between(PmsProducePlan::getPlanDate, firstDay, lastDay)
//                .groupBy(PmsProducePlan::getMaterialId));
//        Assert.notNull(list, "需要下达的物料数据不存在");
//        //2.修改下达后物料的状态
//        List<PmsMrp> pmsMrpList = BeanUtil.copyToList(list, PmsMrp.class);
//        List<Long> materialIdList = Lists.newArrayList();
//        pmsMrpList.forEach(pmsMrp -> {
//            pmsMrp.setStatus(PmManagerConstant.OrderStateEnum.ONE.getCode());
//            materialIdList.add(pmsMrp.getMaterialId());
//        });
//        //3.自定义sql 批量修改状态
//        Boolean aBoolean = this.baseMapper.updateBatchStatus(materialIdList, firstDay, lastDay, PmManagerConstant.OrderStateEnum.ONE.getCode());
//        Assert.isTrue(aBoolean,
//                "修改物料下达状态失败");
//        //3.对物料列表进行MRP计算
//        return pmsMrpService.add(pmsMrpList);
//    }

    @SneakyThrows
    @Override
    public void download(HttpServletResponse response) {
        List<PmsProducePlan> producePlanList = new ArrayList<>();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("生产月计划模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PmsProducePlanNewExcelDTO.class).sheet("模板").doWrite(producePlanList);
    }

    private BigDecimal getQuantity(PmsProducePlanNewExcelDTO excelDTO, String day) {
        BigDecimal quantity = BigDecimal.ZERO;
        if (day.equals(PmManagerConstant.NUM_1)) {
            quantity = excelDTO.getOne();
        }
        if (day.equals(PmManagerConstant.NUM_2)) {
            quantity = excelDTO.getTwo();
        }
        if (day.equals(PmManagerConstant.NUM_3)) {
            quantity = excelDTO.getThree();
        }
        if (day.equals(PmManagerConstant.NUM_4)) {
            quantity = excelDTO.getFour();
        }
        if (day.equals(PmManagerConstant.NUM_5)) {
            quantity = excelDTO.getFive();
        }
        if (day.equals(PmManagerConstant.NUM_6)) {
            quantity = excelDTO.getSix();
        }
        if (day.equals(PmManagerConstant.NUM_7)) {
            quantity = excelDTO.getSeven();
        }
        if (day.equals(PmManagerConstant.NUM_8)) {
            quantity = excelDTO.getEight();
        }
        if (day.equals(PmManagerConstant.NUM_9)) {
            quantity = excelDTO.getNine();
        }
        if (day.equals(PmManagerConstant.NUM_10)) {
            quantity = excelDTO.getTen();
        }
        if (day.equals(PmManagerConstant.NUM_11)) {
            quantity = excelDTO.getEleven();
        }
        if (day.equals(PmManagerConstant.NUM_12)) {
            quantity = excelDTO.getTwelve();
        }
        if (day.equals(PmManagerConstant.NUM_13)) {
            quantity = excelDTO.getThirteen();
        }
        if (day.equals(PmManagerConstant.NUM_14)) {
            quantity = excelDTO.getFourteen();
        }
        if (day.equals(PmManagerConstant.NUM_15)) {
            quantity = excelDTO.getFifteen();
        }
        if (day.equals(PmManagerConstant.NUM_16)) {
            quantity = excelDTO.getSixteen();
        }
        if (day.equals(PmManagerConstant.NUM_17)) {
            quantity = excelDTO.getSeventeen();
        }
        if (day.equals(PmManagerConstant.NUM_18)) {
            quantity = excelDTO.getEighteen();
        }
        if (day.equals(PmManagerConstant.NUM_19)) {
            quantity = excelDTO.getNineteen();
        }
        if (day.equals(PmManagerConstant.NUM_20)) {
            quantity = excelDTO.getTwenty();
        }
        if (day.equals(PmManagerConstant.NUM_21)) {
            quantity = excelDTO.getTwentyOne();
        }
        if (day.equals(PmManagerConstant.NUM_22)) {
            quantity = excelDTO.getTwentyTwo();
        }
        if (day.equals(PmManagerConstant.NUM_23)) {
            quantity = excelDTO.getTwentyThree();
        }
        if (day.equals(PmManagerConstant.NUM_24)) {
            quantity = excelDTO.getTwentyFour();
        }
        if (day.equals(PmManagerConstant.NUM_25)) {
            quantity = excelDTO.getTwentyFive();
        }
        if (day.equals(PmManagerConstant.NUM_26)) {
            quantity = excelDTO.getTwentySix();
        }
        if (day.equals(PmManagerConstant.NUM_27)) {
            quantity = excelDTO.getTwentySeven();
        }
        if (day.equals(PmManagerConstant.NUM_28)) {
            quantity = excelDTO.getTwentyEight();
        }
        if (day.equals(PmManagerConstant.NUM_29)) {
            quantity = excelDTO.getTwentyNine();
        }
        if (day.equals(PmManagerConstant.NUM_30)) {
            quantity = excelDTO.getThirty();
        }
        if (day.equals(PmManagerConstant.NUM_31)) {
            quantity = excelDTO.getThirty0ne();
        }
        return quantity;
    }

}




