package com.steccn.pms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.*;
import com.steccn.pms.listener.ExcelListener;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.mapper.PmsProducePlanMapper;
import com.steccn.pms.vo.PmsProducePlanVO;
import com.steccn.pms.vo.ProducePlanImportVO;
import com.steccn.pms.vo.ProducePlanMsgVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    private final PmMaterialService pmMaterialService;


    @Override
    public List<PmsProducePlanDTO> list(String monthDate) {
        //根据月份查询数据
        List<PmsProducePlanVO> producePlanVOList = this.baseMapper.selectAllByPlanDate(monthDate);

        Map<String, List<PmsProducePlanVO>> groupBy = producePlanVOList
                .stream()
                .collect(Collectors.groupingBy(pmsProducePlanVO -> pmsProducePlanVO.getMaterialName() + "," + pmsProducePlanVO.getMaterialName() + "," + pmsProducePlanVO.getMaterialId()));

        List<PmsProducePlanDTO> pmsProducePlanDTOList = new ArrayList<>();

        for (Map.Entry<String, List<PmsProducePlanVO>> entry : groupBy.entrySet()) {
            PmsProducePlanDTO pmsProducePlanDTO = new PmsProducePlanDTO();
            String[] materials = entry.getKey().split(",");
            BigDecimal total = entry.getValue().stream().map(PmsProducePlanVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            pmsProducePlanDTO.setMaterialName(materials[0]);
            pmsProducePlanDTO.setMaterialCode(materials[1]);
            pmsProducePlanDTO.setMaterialId(Long.parseLong(materials[2]));
            pmsProducePlanDTO.setTotal(total);

            List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList = new ArrayList<>();
            for (PmsProducePlanVO pmsProducePlanVO : entry.getValue()) {
                PmsProducePlanDetailsDTO planDetailsDTO = new PmsProducePlanDetailsDTO();
                BeanUtils.copyProperties(pmsProducePlanVO, planDetailsDTO);
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
        List<ProducePlanMsgVO> planMsgVOList = checkPlanExcelDTO(dtoList);

        //如果错误信息不为空组装数据返回
        ProducePlanImportVO planImportVO = new ProducePlanImportVO();
        if (CollectionUtils.isNotEmpty(planMsgVOList)) {
            planImportVO.setImportFlag(false);
            planImportVO.setProducePlanMsgVOList(planMsgVOList);
            return planImportVO;
        }

        planImportVO.setImportFlag(true);

        List<String> codeList=dtoList.stream().map(PmsProducePlanExcelDTO::getMaterialCode).collect(Collectors.toList());
        Map<String, Long> map = pmMaterialService.selectIdByCode(codeList);
        List<PmsProducePlan> producePlanList = new ArrayList<>();
        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
            PmsProducePlan pmsProducePlan = new PmsProducePlan();
            BeanUtils.copyProperties(excelDTO,pmsProducePlan);
            pmsProducePlan.setMaterialId(map.get(excelDTO.getMaterialCode()));
            pmsProducePlan.setPlanDate(LocalDate.parse(excelDTO.getPlanDate(),ISO_DATE));
            producePlanList.add(pmsProducePlan);
        }
        saveBatch(producePlanList);

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
            BeanUtils.copyProperties(pmsProducePlanVO,planExcelDTO);
            excelDTOList.add(planExcelDTO);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("生产月计划", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PmsBillExcelDTO.class).sheet("模板").doWrite(excelDTOList);
    }

    private List<ProducePlanMsgVO> checkPlanExcelDTO(List<PmsProducePlanExcelDTO> dtoList) {

        List<ProducePlanMsgVO> planMsgVOList = new ArrayList<>();
        List<String> codeList=dtoList.stream().map(PmsProducePlanExcelDTO::getMaterialCode).collect(Collectors.toList());

        Map<String, Long> map = pmMaterialService.selectIdByCode(codeList);
        for (PmsProducePlanExcelDTO excelDTO : dtoList) {
            ProducePlanMsgVO planMsgVO = new ProducePlanMsgVO();
            List<String> errorMsgList = new ArrayList<>();

            if (StringUtils.isBlank(excelDTO.getMaterialCode())) {
                errorMsgList.add("物料编码不能为空");
            }
            if (StringUtils.isBlank(excelDTO.getMaterialName())) {
                errorMsgList.add("物料名称不能为空");
            }
            if (Objects.isNull(excelDTO.getPlanDate())) {
                errorMsgList.add("计划日期");
            }
            if (Objects.isNull(excelDTO.getQuantity())) {
                errorMsgList.add("数量");
            }
            if (Objects.isNull(map.get(excelDTO.getMaterialCode()))) {
                errorMsgList.add("物料信息不存在");
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
}




