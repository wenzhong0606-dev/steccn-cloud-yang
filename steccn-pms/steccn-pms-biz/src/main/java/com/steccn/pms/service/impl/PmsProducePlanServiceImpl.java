package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.*;
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
        List<LocalDate> localDateList = LocalDateUtil.getCurrentMonthDateAll(LocalDate.parse(monthDate+"-01", ISO_DATE));

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
                    producePlanDetailsDTOList.add(planDetailsDTO);
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
        return pmsMrpInfoService.add(dto);
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
        List<ProducePlanMsgVO> planMsgVOList = checkPlanExcelDTO(dtoList, updateFlag, spareCodeMap);

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

        //获取需要修改的物料信息
        HashMap<String, Long> updateMap = getUpdateMap(dtoList, codeList);

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
                BeanUtils.copyProperties(excelDTO, producePlan);
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

        List<PmsProducePlanExcelDTO> excelDTOList = new ArrayList<>();
        List<PmsProducePlanVO> producePlanVOList = this.baseMapper.selectAllByPlanDate(monthDate);
        for (PmsProducePlanVO pmsProducePlanVO : producePlanVOList) {
            PmsProducePlanExcelDTO planExcelDTO = new PmsProducePlanExcelDTO();
            BeanUtils.copyProperties(pmsProducePlanVO, planExcelDTO);
            planExcelDTO.setPlanDate(LocalDateUtil.localDateFormat(pmsProducePlanVO.getPlanDate(), DatePattern.NORM_DATE_PATTERN));
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
        //1.查询需要下达的物料计划数据
        //处理时间格式
        int year = Integer.parseInt(dto.getPlanDate().substring(0, 4));
        int mouth = Integer.parseInt(dto.getPlanDate().substring(5));
        LocalDate date = LocalDate.of(year, mouth, 01);
        // 获取当前月的第一天
        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth());
        // 获取当前月的最后一天
        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());
        List<PmsProducePlan> list = list(new QueryWrapper<PmsProducePlan>()
                .select("material_id,sum(quantity) as quantity")
                .lambda().in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
                .between(PmsProducePlan::getPlanDate, firstDay, lastDay)
                .groupBy(PmsProducePlan::getMaterialId));
        Assert.notNull(list, "需要下达的物料数据不存在");
        //2.修改下达后物料的状态
        Set<Long> materialIdList = Sets.newHashSet();
        list.forEach(pmsProducePlan -> {
            pmsProducePlan.setStatus(PmManagerConstant.OrderStateEnum.ONE.getCode());
            materialIdList.add(pmsProducePlan.getMaterialId());
        });
        //3.自定义sql 批量修改状态
        Boolean aBoolean = this.baseMapper.updateBatchStatus(materialIdList, firstDay, lastDay, PmManagerConstant.OrderStateEnum.ONE.getCode());
        Assert.isTrue(aBoolean,
                "修改物料下达状态失败");
        //3.对物料列表进行MRP计算
        return pmsMrpInfoService.mrpAdd(materialIdList);
    }

    private List<ProducePlanMsgVO> checkPlanExcelDTO(List<PmsProducePlanExcelDTO> dtoList, Integer updateFlag, Map<String, EqpSpareVO> map) {

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




