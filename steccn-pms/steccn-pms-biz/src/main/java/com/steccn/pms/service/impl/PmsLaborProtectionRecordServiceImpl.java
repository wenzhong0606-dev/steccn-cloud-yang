package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.exception.SteccnRuntimeException;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.mes.feign.RemoteMesFeignApi;
import com.steccn.mes.vo.MesUserInfoVO;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.oa.dto.OaUserInfo;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsLaborProtectionRecord;
import com.steccn.pms.domain.PmsLaborProtectionStandard;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.domain.PmsMaterialRequisition;
import com.steccn.pms.dto.*;
import com.steccn.pms.listener.LaborListener;
import com.steccn.pms.mapper.PmsLaborProtectionRecordMapper;
import com.steccn.pms.service.PmsLaborProtectionRecordService;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.vo.LaborExcelImportDataVO;
import com.steccn.pms.vo.PmsLaborRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【pms_labor_protection_record(劳保记录表)】的数据库操作Service实现
* @createDate 2024-04-15 10:59:25
*/
@Service
@RequiredArgsConstructor
public class PmsLaborProtectionRecordServiceImpl extends ServiceImpl<PmsLaborProtectionRecordMapper, PmsLaborProtectionRecord>
    implements PmsLaborProtectionRecordService {
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsMaterialRequisitionService pmsMaterialRequisitionService;
    private final OaInfoService oaInfoService;
    private final RemoteMesFeignApi remoteMesFeignApi;
    private final PmsLaborProtectionStandardDetailService pmsLaborProtectionStandardDetailService;
    private final PmsLaborProtectionRecordMapper pmsLaborProtectionRecordMapper;
    private final PmsLaborProtectionStandardServiceImpl pmsLaborProtectionStandardService;
    private final RemoteFileFeignApi remoteFileFeignApi;

    @Override
    public Page<PmsLaborRecordVO> pageList(PmsLaborRecordPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmsLaborProtectionRecord> condition = new LambdaQueryWrapper<PmsLaborProtectionRecord>()
                .in(CollUtil.isNotEmpty(dto.getOfficeId()), PmsLaborProtectionRecord::getDepCode, dto.getOfficeId())
                .in(CollUtil.isNotEmpty(dto.getPostId()), PmsLaborProtectionRecord::getPostCode, dto.getPostId())
                .in(CollUtil.isNotEmpty(dto.getMaterialId()), PmsLaborProtectionRecord::getMaterialId, dto.getMaterialId())
                .like(StrUtil.isNotBlank(dto.getUserName()), PmsLaborProtectionRecord::getLoginName, dto.getUserName());
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<PmsLaborProtectionRecord> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<PmsLaborRecordVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            //物料和部门map
            Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(resPage.getRecords().stream().map(PmsLaborProtectionRecord::getMaterialId).collect(Collectors.toList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<String> officeCode = Sets.newHashSet();
            officeCode.addAll(resPage.getRecords().stream().map(PmsLaborProtectionRecord::getDepCode).collect(Collectors.toSet()));
            officeCode.addAll(resPage.getRecords().stream().map(PmsLaborProtectionRecord::getPostCode).collect(Collectors.toSet()));
            officeCode.addAll(resPage.getRecords().stream().map(PmsLaborProtectionRecord::getReceiptWarehouse).collect(Collectors.toSet()));
            Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(officeCode), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            for (PmsLaborProtectionRecord tmp : resPage.getRecords()) {
                PmsLaborRecordVO vo = new PmsLaborRecordVO();
                BeanUtils.copyProperties(tmp, vo);
                vo.setMaterialCode(spareMap.get(tmp.getMaterialId()).getCode());
                vo.setMaterialName(spareMap.get(tmp.getMaterialId()).getName());
                vo.setDepName(officeMap.get(tmp.getDepCode()).getOfficeName());
                vo.setPostName(officeMap.get(tmp.getPostCode()).getOfficeName());
                vo.setReceiptWarehouseName(officeMap.get(tmp.getReceiptWarehouse()).getOfficeName());
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateReq(PmsLaborGerReqDTO dto) {
        List<PmsLaborGerReqDetailDTO> data = Lists.newArrayList();
        //处理前端传的数据
        for (Map<String, String> tmp : dto.getDetails()) {
            String str = tmp.get("materialList");
            str = tmp.get("materialList").substring(1, str.length() - 1).replaceAll("\\s+", "");
            List<String> materialList = Arrays.asList(str.split(","));
            materialList.forEach(material -> {
                PmsLaborGerReqDetailDTO dataTmp = new PmsLaborGerReqDetailDTO();
                dataTmp.setMaterialId(Long.parseLong(material));
                dataTmp.setDepCode(tmp.get("depCode"));
                dataTmp.setLoginCode(tmp.get("loginCode"));
                if (StrUtil.isNotBlank(tmp.get(material + "_claimNum"))) {
                    dataTmp.setNum(new BigDecimal(tmp.get(material + "_claimNum")));
                    data.add(dataTmp);
                }
            });
        }
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(data
                .stream().map(PmsLaborGerReqDetailDTO::getDepCode).collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(data.stream().map(PmsLaborGerReqDetailDTO::getMaterialId).collect(Collectors.toList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<MesUserInfoVO> mesUserInfoVOS = FeignOptional.of(() -> remoteMesFeignApi.getEmpDepPost(new TupleDTO<Set<String>, Integer>(data.stream().map(PmsLaborGerReqDetailDTO::getLoginCode).collect(Collectors.toSet()), null)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, LocalDate> employeeTime = FeignOptional.of(() -> oaInfoService.getCompanyStartTime(data.stream().map(PmsLaborGerReqDetailDTO::getLoginCode).collect(Collectors.toSet())), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        Map<String, List<PmsLaborGerReqDetailDTO>> depMap = data.stream().collect(Collectors.groupingBy(PmsLaborGerReqDetailDTO::getDepCode));
        for (Map.Entry<String, List<PmsLaborGerReqDetailDTO>> tmp : depMap.entrySet()) {
            MaterialRequisitionDTO materialRequisitionDTO = new MaterialRequisitionDTO();
            SysOfficeVO office = officeMap.get(tmp.getKey());
            materialRequisitionDTO.setDepartmentId(office.getId());
            //部门领料
            materialRequisitionDTO.setTypeId(dto.getTypeId());
            //备品备件库
            materialRequisitionDTO.setWarehouseId(440L);
            materialRequisitionDTO.setFileId(dto.getFileId());
            //详情
            List<MaterialRequisitionInfoDTO> detailList = Lists.newArrayList();
            //劳保领用记录
            List<PmsLaborProtectionRecord> modelList = Lists.newArrayList();
            for (PmsLaborGerReqDetailDTO detailTmp : tmp.getValue()) {
                //处理领用单
                MaterialRequisitionInfoDTO detailModel = new MaterialRequisitionInfoDTO();
                EqpSpareVO spare = spareMap.get(detailTmp.getMaterialId());
                detailModel.setMaterialCode(spare.getCode());
                detailModel.setMaterialName(spare.getName());
                detailModel.setMaterialId(spare.getId());
                detailModel.setUnitId(spare.getUnitId().toString());
                detailModel.setUnitName(spare.getUnit());
                if (detailTmp.getNum().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                detailModel.setQuantity(detailTmp.getNum());
                detailModel.setReceiveQuantity(BigDecimal.ZERO);
                detailModel.setRequireDate(LocalDate.now());
                OaUserInfo oaUserInfo = FeignOptional.of(() -> oaInfoService.getOaUserInfo(detailTmp.getLoginCode()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                List<MaterialRequisitionDemanderDTO> demanderList = Lists.newArrayList(new MaterialRequisitionDemanderDTO(oaUserInfo.getOaUserId().longValue(), oaUserInfo.getName()));
                detailList.add(detailModel);
                detailModel.setDemanderList(demanderList);
                //保存领用记录
                PmsLaborProtectionRecord recordModel = new PmsLaborProtectionRecord();
                MesUserInfoVO mesUserInfoVO = mesUserInfoVOS.stream().filter(v -> v.getLoginCode().equals(detailTmp.getLoginCode())).findFirst().orElse(null);
                recordModel.setDepCode(mesUserInfoVO.getDepCode());
                recordModel.setPostCode(mesUserInfoVO.getPostCode());
                recordModel.setLoginCode(mesUserInfoVO.getLoginCode());
                recordModel.setLoginName(mesUserInfoVO.getName());
                recordModel.setStartTime(employeeTime.get(mesUserInfoVO.getLoginCode()));
                recordModel.setMaterialId(spare.getId());
                recordModel.setReceiptTime(LocalDateTime.now());
                recordModel.setReceiptNum(detailTmp.getNum());
                recordModel.setReceiptWarehouse("WH002");
                recordModel.setReceiptPersonCode(SecurityUtils.getUser().getUsername());
                recordModel.setReceiptPersonName(SecurityUtils.getUser().getName());
                modelList.add(recordModel);
            }
            if (CollUtil.isEmpty(detailList)) {
                throw new BusinessException("物料数都为0");
            }
            materialRequisitionDTO.setList(detailList);
            Long id = pmsMaterialRequisitionService.add(materialRequisitionDTO);
            PmsMaterialRequisition byId = pmsMaterialRequisitionService.getById(id);
            modelList.forEach(tmp2 -> {
                tmp2.setReceiptId(id);
                tmp2.setReceiptCode(byId.getCode());
            });
            saveBatch(modelList);
        }
        return true;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("劳保用品", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //获取表头数据
        List<PmsLaborProtectionStandardDetail> detailData = pmsLaborProtectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .orderByDesc(PmsLaborProtectionStandardDetail::getStandardId));
        List<Long> materialIdList = detailData.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<List<String>> head = new ArrayList<List<String>>();
        head.add(Arrays.asList("填写须知：\n" +
                "\n" +
                "1. 导入文件的表头必须和模板一致\n" +
                "\n" +
                "2. 导入列表不能为空，至少填写一个人员\n" +
                "3. 每个人员的姓名必须完全正确\n" +
                "4. 对于系统内有重名的人员，暂不支持导入，建议页面上单独添加\n" +
                "5. 无需领用的劳保用品可不填写待领数量\n" +
                "\n" +
                "6. 出现以下错误时请相应更正后重新上传：\n" +
                "\n" +
                "1）姓名不能为空\n" +
                "\n" +
                "2）待领数量必须为数字\n" +
                "\n" +
                "3）待领数量必须大于0\n" +
                "\n" +
                "4）该人员在系统中不存在\n" +
                "\n" +
                "5）系统中存在重名的人员\n" +
                "6）每个人员只能填写一行","姓名"));
        materialIdList.forEach(tmp -> {

            EqpSpareVO spareVO = spareMap.get(tmp);
            StringBuilder materialName = new StringBuilder();
            materialName.append(spareVO.getName());
            if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())) {
                materialName.append("/");
                materialName.append(spareVO.getCode());
            }
            if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())) {
                materialName.append("/");
                materialName.append(spareVO.getSpecifications());
            }

            head.add(Arrays.asList("填写须知：\n" +
                    "\n" +
                    "1. 导入文件的表头必须和模板一致\n" +
                    "\n" +
                    "2. 导入列表不能为空，至少填写一个人员\n" +
                    "3. 每个人员的姓名必须完全正确\n" +
                    "4. 对于系统内有重名的人员，暂不支持导入，建议页面上单独添加\n" +
                    "5. 无需领用的劳保用品可不填写待领数量\n" +
                    "\n" +
                    "6. 出现以下错误时请相应更正后重新上传：\n" +
                    "\n" +
                    "1）姓名不能为空\n" +
                    "\n" +
                    "2）待领数量必须为数字\n" +
                    "\n" +
                    "3）待领数量必须大于0\n" +
                    "\n" +
                    "4）该人员在系统中不存在\n" +
                    "\n" +
                    "5）系统中存在重名的人员\n" +
                    "6）每个人员只能填写一行",materialName.toString()));
        });
        EasyExcel.write(response.getOutputStream()).sheet("sheet1").head( head).doWrite(new ArrayList<>());

    }


    @Override
    public LaborExcelImportDataVO excelImportData(PmExcelDTO dto) throws IOException {

        LaborExcelImportDataVO laborExcelImportDataVO = new LaborExcelImportDataVO();
        //文件类型校验
        if (Objects.nonNull(dto.getFile())) {
            //获取文件拓展名
            String fileExtension = Objects.requireNonNull(dto.getFile().getOriginalFilename()).substring(dto.getFile().getOriginalFilename().lastIndexOf("."));
            if (!fileExtension.equals(PmManagerConstant.FILE_EXTENSION_XLSX)) {
                throw new BusinessException("文件类型不支持,请使用模板进行导入");
            }
        }
        LaborListener laborListener = new LaborListener(pmsLaborProtectionStandardDetailService, remoteSpareFeignApi, remoteFileFeignApi, dto.getFile().getName());
        EasyExcel.read(dto.getFile().getInputStream(), laborListener).excelType(ExcelTypeEnum.XLSX).sheet().headRowNumber(1).doRead();
        Map<String, List<Map<String, String>>> stringListMap = laborListener.saveData();
        laborExcelImportDataVO.setSize(stringListMap.size());

        if (laborListener.getErrorSize() > 0) {
            laborExcelImportDataVO.setSuccessFlag(Boolean.FALSE);
            laborExcelImportDataVO.setSize(laborListener.getErrorSize());
        }else if (stringListMap.size() >0){
            laborExcelImportDataVO.setSize(stringListMap.size());
            //处理数据
            //需要拿到所有配置的数据
            List<PmsLaborProtectionStandard> standardList = pmsLaborProtectionStandardService.list();
            //拿到所有详情
            List<PmsLaborProtectionStandardDetail> standardDetailList = pmsLaborProtectionStandardDetailService.list();

            //处理部门和岗位
            //拿到标准中的所有部门岗位
            List<PmsLaborProtectionStandard> finalStandardList = standardList;
            Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(finalStandardList.stream().flatMap(eq -> eq.getOfficeId().stream()).collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<String> availPost = Sets.newHashSet();
            Set<String> availDep = Sets.newHashSet();
            //区分岗位和部门
            for (Map.Entry<String, SysOfficeVO> entry : officeMap.entrySet()) {
                SysOfficeVO tmp = entry.getValue();
                if (tmp.getOfficeType().equals("6")) {
                    availPost.add(tmp.getOfficeCode());
                } else if (tmp.getOfficeType().equals("15")) {
                    //若是部门类型，则拿到其岗位数据
                    availDep.add(tmp.getOfficeCode());
                }
            }
            if (CollUtil.isNotEmpty(availDep)) {
                Map<String, List<String>> depPostMap = FeignOptional.of(() -> remoteOfficeFeign.depPostMap(availDep), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                availPost.addAll(depPostMap.values().stream().flatMap(List::stream).collect(Collectors.toSet()));
            }
            //拿到所有岗位下的人员和岗位所属机构
            Map<String, List<String>> employee = FeignOptional.of(() -> remoteMesFeignApi.getPostUser(availPost), FeignConstant.STECCN_MES_SERVICE_NAME).get();
            //拿到所有人员的入职时间
            Map<String, LocalDate> employeeTime = FeignOptional.of(() -> oaInfoService.getCompanyStartTime(employee.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(str -> {
                        int index = str.indexOf('_');
                        return index != -1 ? str.substring(0, index) : str;
                    }).collect(Collectors.toSet())), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            //拿到所有入职超过1个月的人员
            Set<String> employeeId = employeeTime.entrySet().stream().filter(v -> v.getValue().plusMonths(1).isBefore(LocalDate.now()))
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
            //过滤名称
//        if(cn.hutool.core.util.StrUtil.isNotBlank(dto.getUserName())){
//            employeeId =employeeId.stream().filter(v->v.contains(dto.getUserName())).collect(Collectors.toSet());
//        }
//        Set<String> finalEmployeeId = employeeId;
            List<MesUserInfoVO> userInfos = FeignOptional.of(() -> remoteMesFeignApi.getEmpDepPost(new TupleDTO<>(employeeId, null)), FeignConstant.STECCN_MES_SERVICE_NAME).get();
            //开始组装数据
            //表头
            SteccnTableDataDTO<Map<String, String>> result = new SteccnTableDataDTO<>();
            //先组表头
            List<SteccnTableColumn> columns = Lists.newArrayList();
            columns.add(new SteccnTableColumn("depName", "部门", null));
            columns.add(new SteccnTableColumn("postName", "岗位", null));
            columns.add(new SteccnTableColumn("userName", "人员", null));
            columns.add(new SteccnTableColumn("enterTime", "入职时间", null));
            //动态部分
            List<Long> materialIdList = standardDetailList.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
            Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            materialIdList.forEach(tmp -> {
                EqpSpareVO spareVO = spareMap.get(tmp);
                StringBuilder materialName = new StringBuilder();
                materialName.append(spareVO.getName());
                if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())) {
                    materialName.append("/");
                    materialName.append(spareVO.getCode());
                }
                if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())) {
                    materialName.append("/");
                    materialName.append(spareVO.getSpecifications());
                }
                columns.add(new SteccnTableColumn(tmp.toString(), materialName.toString(),
                        Lists.newArrayList(new SteccnTableColumn(tmp + "_" + "receiptTime", "上次领用时间", null),
                                new SteccnTableColumn(tmp + "_claimNum", "待领数量", null),
                                new SteccnTableColumn(tmp + "_availNum", "可领数量", null))));
            });
            List<Map<String, String>> tableList = Lists.newArrayList();
            //处理数据，遍历人员
            for (MesUserInfoVO tmp : userInfos) {
                Map<String, String> column = Maps.newHashMap();
                //获取岗位的劳保标准
                PmsLaborProtectionStandard standardTmp;
                standardTmp = standardList.stream().filter(v -> v.getOfficeId().contains(tmp.getPostCode())).findFirst().orElse(null);
                if (Objects.isNull(standardTmp)) {
                    //获取用户的部门标准
                    standardTmp = standardList.stream().filter(v -> v.getOfficeId().contains(tmp.getDepCode())).findFirst().orElse(null);
                }
                if (Objects.isNull(standardTmp)) {
                    continue;
                }
                column.put("depName", tmp.getDepName());
                column.put("depCode", tmp.getDepCode());
                column.put("postName", tmp.getPostName());
                column.put("postCode", tmp.getPostCode());
                column.put("loginCode", tmp.getLoginCode());
                column.put("userName", tmp.getName());
                column.put("enterTime", employeeTime.get(tmp.getLoginCode()).toString());
                column.put("materialList", materialIdList.toString());
                //获取部门详情
                PmsLaborProtectionStandard finalStandardTmp = standardTmp;
                List<PmsLaborProtectionStandardDetail> standardDetailTmp = standardDetailList.stream().filter(v -> v.getStandardId().equals(finalStandardTmp.getId())).collect(Collectors.toList());
                standardDetailTmp.forEach(detailTmp -> {
                    //查看用户的领用记录
                    PmsLaborMaterialReceiptDTO pmsLaborMaterialReceiptDTO = pmsLaborProtectionRecordMapper.selectReceiptNum(tmp.getLoginCode(),
                            LocalDateTime.now().minusMonths(detailTmp.getPeriod().longValue())
                            , LocalDateTime.now()
                            , detailTmp.getMaterialId());
                    if (Objects.nonNull(pmsLaborMaterialReceiptDTO)) {
                        //可领数量
                        BigDecimal availNum = BigDecimalUtil.subtract(new BigDecimal(detailTmp.getAmount()), pmsLaborMaterialReceiptDTO.getReceiptNum(), 2);
                        if (availNum.compareTo(BigDecimal.ZERO) == 1) {
                            column.put("materialId", detailTmp.getMaterialId().toString());
                            column.put(detailTmp.getMaterialId() + "_claimNum", availNum.toString());
                            column.put(detailTmp.getMaterialId() + "_availNum", availNum.toString());
                            column.put(detailTmp.getMaterialId() + "_receiptTime", pmsLaborMaterialReceiptDTO.getReceiptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        } else {
                            column.put("materialId", detailTmp.getMaterialId().toString());
                            column.put(detailTmp.getMaterialId() + "_claimNum", BigDecimal.ZERO.toString());
                            column.put(detailTmp.getMaterialId() + "_availNum", BigDecimal.ZERO.toString());
                            column.put(detailTmp.getMaterialId() + "_receiptTime", pmsLaborMaterialReceiptDTO.getReceiptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        }
                    } else {
                        column.put("materialId", detailTmp.getMaterialId().toString());
                        column.put(detailTmp.getMaterialId() + "_claimNum", detailTmp.getAmount().toString());
                        column.put(detailTmp.getMaterialId() + "_availNum", detailTmp.getAmount().toString());
                    }
                });
                tableList.add(column);
            }
            result.setColumns(columns);
            //过滤掉excel 中不存在的数据
            Set<String> names = stringListMap.keySet();
            List<Map<String, String>> filteredTableList = tableList.stream()
                    .filter(column -> names.contains(column.get("userName")))
                    .collect(Collectors.toList());
            filteredTableList.stream().forEach(column -> {
                String userName = column.get("userName");
                List<Map<String, String>> list = stringListMap.get(userName);
                if (Objects.nonNull(list)) {
                    list.forEach(column::putAll);
                }
            });
            result.setTableList(filteredTableList);
            laborExcelImportDataVO.setTableDataDTO(result);
        }
        return laborExcelImportDataVO;
    }

    @Override
    public ResponseEntity<FileSystemResource> excelImportDataError(PmExcelDTO dto) throws IOException {
        LaborListener laborListener = new LaborListener(pmsLaborProtectionStandardDetailService, remoteSpareFeignApi, remoteFileFeignApi, dto.getFile().getOriginalFilename());
        EasyExcel.read(dto.getFile().getInputStream(), laborListener).excelType(ExcelTypeEnum.XLSX).sheet().headRowNumber(1).doRead();

        if (laborListener.getErrorSize() > 0) {
            File errorFile = laborListener.writeErrorFile(laborListener.getErrorRows());
            if (errorFile != null) {
                try {
                    // 返回错误文件给前端
                    FileSystemResource resource = new FileSystemResource(errorFile);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + errorFile.getName());
                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(errorFile.length())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                } catch (Exception e) {
                    throw new SteccnRuntimeException("文件处理失败");
                } finally {
                    errorFile.delete(); // 删除临时文件
                }
            }
        }

        return null;
    }
}




