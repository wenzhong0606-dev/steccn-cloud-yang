package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.listener.ExcelListener;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.service.PmsMrpService;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.mapper.PmsProducePlanMapper;
import com.steccn.pms.vo.PmsProducePlanVO;
import com.steccn.pms.vo.PmsPullForceVO;
import com.steccn.pms.vo.ProducePlanImportVO;
import com.steccn.pms.vo.ProducePlanMsgVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
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


    @Override
    public List<PmsProducePlanDTO> list(String monthDate) {
        //根据月份查询数据
        List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().like(PmsProducePlan::getPlanDate, monthDate + "%"));
        List<PmsProducePlanVO> producePlanVOList = new ArrayList<>();
        List<Long> materialId = producePlanList.stream().map(PmsProducePlan::getMaterialId).collect(Collectors.toList());

        Map<Long, EqpSpareVO> map = remoteSpareFeignApi.listById(materialId).getData();
        for (PmsProducePlan pmsProducePlan : producePlanList) {
            EqpSpareVO spareVO = map.get(pmsProducePlan.getMaterialId());
            PmsProducePlanVO producePlanVO = new PmsProducePlanVO();
            BeanUtils.copyProperties(pmsProducePlan, producePlanVO);
            producePlanVO.setMaterialCode(spareVO.getCode());
            producePlanVO.setMaterialName(spareVO.getName());
            producePlanVOList.add(producePlanVO);
        }

        Map<String, List<PmsProducePlanVO>> groupBy = producePlanVOList
                .stream()
                .collect(Collectors.groupingBy(pmsProducePlanVO -> pmsProducePlanVO.getMaterialName() + "," + pmsProducePlanVO.getMaterialCode() + "," + pmsProducePlanVO.getMaterialId()));

        List<PmsProducePlanDTO> pmsProducePlanDTOList = new ArrayList<>();
        //获取指定月份所有天数
        List<LocalDate> localDateList = LocalDateUtil.getCurrentMonthDateAll(LocalDate.parse(monthDate + "-01", ISO_DATE));

        for (Map.Entry<String, List<PmsProducePlanVO>> entry : groupBy.entrySet()) {
            PmsProducePlanDTO pmsProducePlanDTO = new PmsProducePlanDTO();
            String[] materials = entry.getKey().split(",");
            BigDecimal total = entry.getValue().stream().map(PmsProducePlanVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            pmsProducePlanDTO.setMaterialName(materials[0]);
            pmsProducePlanDTO.setMaterialCode(materials[1]);
            pmsProducePlanDTO.setMaterialId(Long.parseLong(materials[2]));
            pmsProducePlanDTO.setTotal(total);

            List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList = new ArrayList<>();
            for (LocalDate localDate : localDateList) {
                PmsProducePlanDetailsDTO planDetailsDTO = new PmsProducePlanDetailsDTO();
                Optional<PmsProducePlanVO> present = entry.getValue().stream().filter(producePlanVO -> producePlanVO.getPlanDate().equals(localDate)).findAny();
                if (present.isPresent()) {
                    BeanUtils.copyProperties(present.get(), planDetailsDTO);
                } else {
                    planDetailsDTO.setPlanDate(localDate);
                }
                producePlanDetailsDTOList.add(planDetailsDTO);

            }
            pmsProducePlanDTO.setProducePlanDetailsDTOList(producePlanDetailsDTOList);
            pmsProducePlanDTOList.add(pmsProducePlanDTO);
        }

        return pmsProducePlanDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrEditBatch(PmsProducePlanDTO dto) {
        //需要新增的list
        List<PmsProducePlan> addProducePlanList = new ArrayList<>();
        //需要修改的list
        List<PmsProducePlan> updateProducePlanList = new ArrayList<>();

        for (PmsProducePlanDetailsDTO planDetailsDTO : dto.getProducePlanDetailsDTOList()) {
            PmsProducePlan pmsProducePlan = new PmsProducePlan();
            pmsProducePlan.setMaterialId(dto.getMaterialId());
            pmsProducePlan.setPlanDate(planDetailsDTO.getPlanDate());
            pmsProducePlan.setQuantity(planDetailsDTO.getQuantity());

            //如果有id就修改，没有就新增
            if (Objects.isNull(planDetailsDTO.getId())) {
                pmsProducePlan.setStatus(0);
                addProducePlanList.add(pmsProducePlan);
            } else {
                pmsProducePlan.setId(planDetailsDTO.getId());
                updateProducePlanList.add(pmsProducePlan);
            }
        }
        if (CollectionUtils.isNotEmpty(addProducePlanList)) {
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

        List<PmsProducePlanExcelDTO> dtoList = new ArrayList<>();
        EasyExcel.read(dto.getFile().getInputStream(), PmsProducePlanExcelDTO.class, new ExcelListener(dtoList)).sheet().doRead();

        List<String> codeList = dtoList.stream().map(PmsProducePlanExcelDTO::getMaterialCode).collect(Collectors.toList());
        Map<String, EqpSpareVO> spareCodeMap = remoteSpareFeignApi.listByCode(codeList).getData();
        //获取需要修改的物料信息
        HashMap<String, Long> updateMap = getUpdateMap(dtoList, codeList);

        List<ProducePlanMsgVO> planMsgVOList = checkPlanExcelDTO(dtoList, updateFlag, spareCodeMap, updateMap);

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


        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
            if (updateFlag.equals(0)) {
                PmsProducePlan pmsProducePlan = new PmsProducePlan();
                BeanUtils.copyProperties(excelDTO, pmsProducePlan);
                pmsProducePlan.setMaterialId(spareCodeMap.get(excelDTO.getMaterialCode()).getId());
                pmsProducePlan.setPlanDate(LocalDate.parse(excelDTO.getPlanDate(), ISO_DATE));
                pmsProducePlan.setStatus(0);
                addList.add(pmsProducePlan);
            } else {
                //根据code获取id
                Long materialId = updateMap.get(excelDTO.getMaterialCode());
                PmsProducePlan producePlan = new PmsProducePlan();
                producePlan.setMaterialId(spareCodeMap.get(excelDTO.getMaterialCode()).getId());
                producePlan.setQuantity(excelDTO.getQuantity());
                producePlan.setPlanDate(LocalDate.parse(excelDTO.getPlanDate(), ISO_DATE));
                producePlan.setStatus(0);
                //如果物料id为空表示数据库无数据，新增
                if (Objects.isNull(materialId)) {
                    addList.add(producePlan);
                } else {
                    producePlan.setMaterialId(materialId);
                    updateList.add(producePlan);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(addList)) {
            //新增list去重
            addList = addList.stream().collect(
                    Collectors.collectingAndThen(
                            Collectors.toCollection(
                                    () -> new TreeSet<PmsProducePlan>(Comparator.comparing(p -> p.getPlanDate() + "," + p.getMaterialId()))), ArrayList::new));
            if (!saveBatch(addList)) {
                throw new BusinessException("生产月计划导入新增失败");
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            if (!updateBatchById(updateList)) {
                throw new BusinessException("生产月计划修改失败");
            }
        }

        return planImportVO;
    }

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
                .between(PmsProducePlan::getPlanDate, dto.getFirstDay(), dto.getLastDay()));
        Map<Long, List<PmsProducePlan>> collect = list.stream().collect(Collectors.groupingBy(PmsProducePlan::getMaterialId));
        Set<String> codes = Sets.newHashSet();
        codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
        codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
//        codes.add(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT);
        Map<String, SysDictVO> dictMap = FeignOptional.of(remoteDictFeignApi.listByCodes(codes)).get();
        Set<Long> ids = Sets.newHashSet();
        Map<Long, MesMaterialInventoryDTO> inventoryDTOMap = remoteMessFeignApi.materialInventory(Sets.newHashSet(dto.getMaterialIds()));
        MesMaterialInventoryDTO mesMaterialInventoryDTO = new MesMaterialInventoryDTO();
        mesMaterialInventoryDTO.setAvailableQuantity(BigDecimal.ZERO);
        //获取独立需求类型id
        Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
        //获取订单需求类型id
        Long mrpRequirmentId = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, PmManagerConstant.MRP_TYPE)).get().getId();
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
    }


//    @Override
//    public void turnProduceOrder(PmsProduceOrderDTO model) {
//        List<PmsMrpInfo> infoList = Lists.newArrayList();
//        // 获取转订单的mrp计划订单数据
//        PmsMrpInfo planInfo = pmsMrpInfoService.getById(model.getMrpId());
//        Set<String> codes = Sets.newHashSet();
//        codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
//        codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
//        Map<String, SysDictVO> dictMap = FeignOptional.of(remoteDictFeignApi.listByCodes(codes)).get();
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
        Map<String, SysDictVO> dictMap = FeignOptional.of(remoteDictFeignApi.listByCodes(codes)).get();
        Set<Long> ids = Sets.newHashSet();
        Map<Long, MesMaterialInventoryDTO> inventoryDTOMap = remoteMessFeignApi.materialInventory(Sets.newHashSet(materialIds));
        //获取独立需求类型id
        Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
        //获取订单需求类型id
        Long mrpRequirmentId = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, PmManagerConstant.MRP_TYPE)).get().getId();
        dictMap.forEach((key, value) -> ids.add(value.getId()));
        MesMaterialInventoryDTO inventoryDTO = new MesMaterialInventoryDTO();
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
            MesMaterialInventoryDTO inventory = inventoryDTOMap.getOrDefault(key, inventoryDTO);
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

    private List<ProducePlanMsgVO> checkPlanExcelDTO(List<PmsProducePlanExcelDTO> dtoList, Integer updateFlag, Map<String, EqpSpareVO> map, HashMap<String, Long> updateMap) {

        List<ProducePlanMsgVO> planMsgVOList = new ArrayList<>();

        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
            ProducePlanMsgVO planMsgVO = new ProducePlanMsgVO();
            List<String> errorMsgList = new ArrayList<>();

            if (Objects.isNull(excelDTO.getPlanDate())) {
                errorMsgList.add("计划日期不能为空");
            }
            if (Objects.isNull(excelDTO.getQuantity())) {
                errorMsgList.add("数量不能为空");
            }
            if (Objects.isNull(map.get(excelDTO.getMaterialCode()))) {
                errorMsgList.add("物料信息不存在");
            }
            if (updateFlag.equals(0) && Objects.nonNull(updateMap.get(excelDTO.getMaterialCode()))) {
                errorMsgList.add("物料计划数据库已存在");
            }
            //过滤出同一个月编码重复的数据
            //过滤出本行以外的重复数据
            List<PmsProducePlanExcelDTO> pmsProducePlanExcelDTOList = dtoList
                    .stream()
                    .filter(dto -> dto.getMaterialCode().equals(excelDTO.getMaterialCode()) && dto.getPlanDate().equals(excelDTO.getPlanDate()))
                    .filter(dto -> !dto.getRowIndex().equals(excelDTO.getRowIndex())).collect(Collectors.toList());
            String rows = StringUtils.join(pmsProducePlanExcelDTOList.stream().map(PmsProducePlanExcelDTO::getMaterialCode).collect(Collectors.toList()), ",");
            if (updateFlag.equals(0) && pmsProducePlanExcelDTOList.size() > 0) {
                errorMsgList.add("和" + rows + "物料编码重复");
            }

            //获取当前行的错误信息
            planMsgVO.setErrorMsgList(errorMsgList);
            //如果错误信息不为空获取行号
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                planMsgVO.setRowIndex(excelDTO.getRowIndex());
                planMsgVOList.add(planMsgVO);
            }
        }

        return planMsgVOList;
    }

    private HashMap<String, Long> getUpdateMap(List<PmsProducePlanExcelDTO> dtoList, List<String> codeList) {
        List<String> planDateList = dtoList.stream().map(PmsProducePlanExcelDTO::getPlanDate).collect(Collectors.toList());
        List<LocalDate> localDateList = new ArrayList<>();
        for (String planDate : planDateList) {
            LocalDate localDate = LocalDate.parse(planDate, ISO_DATE);
            localDateList.add(localDate);
        }

        //根据导入的日期查询数据
        List<PmsProducePlan> producePlanList = list(new LambdaQueryWrapper<PmsProducePlan>().in(PmsProducePlan::getPlanDate, localDateList));
        //根据物料code查询物料信息
        Map<Long, EqpSpareVO> spareMap = remoteSpareFeignApi.mapByCode(codeList).getData();

        HashMap<String, Long> updateMap = Maps.newHashMap();
        //获取所有日期查询数据的物料id和物料code
        for (PmsProducePlan pmsProducePlan : producePlanList) {
            EqpSpareVO spareVO = spareMap.get(pmsProducePlan.getMaterialId());
            if (!Objects.isNull(spareVO)) {
                updateMap.put(spareVO.getCode(), spareVO.getId());
            }
        }

        return updateMap;
    }

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
        EasyExcel.write(response.getOutputStream(), PmsProducePlanExcelDTO.class).sheet("模板").doWrite(producePlanList);
    }

}




