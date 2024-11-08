package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.feign.RemoteMesFeignApi;
import com.steccn.mes.vo.MesUserInfoVO;
import com.steccn.oa.dto.OaUserInfo;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsLaborProtectionRecord;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.domain.PmsMaterialRequisition;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsLaborProtectionRecordMapper;
import com.steccn.pms.service.PmsLaborProtectionRecordService;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import com.steccn.pms.service.PmsLaborProtectionStandardService;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.vo.PmsLaborExcelDTO;
import com.steccn.pms.vo.PmsLaborRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    implements PmsLaborProtectionRecordService{
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsMaterialRequisitionService pmsMaterialRequisitionService;
    private final OaInfoService oaInfoService;
    private final RemoteMesFeignApi remoteMesFeignApi;
    private final PmsLaborProtectionStandardDetailService pmsLaborProtectionStandardDetailService;

    @Override
    public Page<PmsLaborRecordVO> pageList(PmsLaborRecordPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmsLaborProtectionRecord> condition = new LambdaQueryWrapper<PmsLaborProtectionRecord>()
                .in(CollUtil.isNotEmpty(dto.getOfficeId()),PmsLaborProtectionRecord::getDepCode, dto.getOfficeId())
                .in(CollUtil.isNotEmpty(dto.getPostId()),PmsLaborProtectionRecord::getPostCode, dto.getPostId())
                .in(CollUtil.isNotEmpty(dto.getMaterialId()),PmsLaborProtectionRecord::getMaterialId, dto.getMaterialId())
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
        for(Map<String,String> tmp:dto.getDetails()){
            String str = tmp.get("materialList");
            str = tmp.get("materialList").substring(1, str.length() - 1).replaceAll("\\s+", "");
            List<String> materialList = Arrays.asList(str.split(","));
            materialList.forEach(material->{
                PmsLaborGerReqDetailDTO dataTmp = new PmsLaborGerReqDetailDTO();
                dataTmp.setMaterialId(Long.parseLong(material));
                dataTmp.setDepCode(tmp.get("depCode"));
                dataTmp.setLoginCode(tmp.get("loginCode"));
                if(StrUtil.isNotBlank(tmp.get(material+"_claimNum"))){
                    dataTmp.setNum(new BigDecimal(tmp.get(material+"_claimNum")));
                    data.add(dataTmp);
                }
            });
        }
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByCodeSet(data
                .stream().map(PmsLaborGerReqDetailDTO::getDepCode).collect(Collectors.toSet())),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(()->remoteSpareFeignApi.listById(data.stream().map(PmsLaborGerReqDetailDTO::getMaterialId).collect(Collectors.toList())),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<MesUserInfoVO> mesUserInfoVOS = FeignOptional.of(() -> remoteMesFeignApi.getEmpDepPost(new TupleDTO<Set<String>, Integer>(data.stream().map(PmsLaborGerReqDetailDTO::getLoginCode).collect(Collectors.toSet()), null)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, LocalDate> employeeTime = FeignOptional.of(() -> oaInfoService.getCompanyStartTime(data.stream().map(PmsLaborGerReqDetailDTO::getLoginCode).collect(Collectors.toSet())), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        Map<String, List<PmsLaborGerReqDetailDTO>> depMap = data.stream().collect(Collectors.groupingBy(PmsLaborGerReqDetailDTO::getDepCode));
        for(Map.Entry<String,List<PmsLaborGerReqDetailDTO>> tmp:depMap.entrySet()){
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
            for(PmsLaborGerReqDetailDTO detailTmp:tmp.getValue()){
                //处理领用单
                MaterialRequisitionInfoDTO detailModel = new MaterialRequisitionInfoDTO();
                EqpSpareVO spare = spareMap.get(detailTmp.getMaterialId());
                detailModel.setMaterialCode(spare.getCode());
                detailModel.setMaterialName(spare.getName());
                detailModel.setMaterialId(spare.getId());
                detailModel.setUnitId(spare.getUnitId().toString());
                detailModel.setUnitName(spare.getUnit());
                if(detailTmp.getNum().compareTo(BigDecimal.ZERO)==0){
                    continue;
                }
                detailModel.setQuantity(detailTmp.getNum());
                detailModel.setReceiveQuantity(BigDecimal.ZERO);
                detailModel.setRequireDate(LocalDate.now());
                OaUserInfo oaUserInfo = FeignOptional.of(() -> oaInfoService.getOaUserInfo(detailTmp.getLoginCode()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                List<MaterialRequisitionDemanderDTO> demanderList = Lists.newArrayList(new MaterialRequisitionDemanderDTO(oaUserInfo.getOaUserId().longValue(),oaUserInfo.getName()));
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
            if(CollUtil.isEmpty(detailList)){
                throw new BusinessException("物料数都为0");
            }
            materialRequisitionDTO.setList(detailList);
            Long id = pmsMaterialRequisitionService.add(materialRequisitionDTO);
            PmsMaterialRequisition byId = pmsMaterialRequisitionService.getById(id);
            modelList.forEach(tmp2->{
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
        List<String> children = Lists.newArrayList();
        children.add("姓名");
        head.add(children);
        materialIdList.forEach(tmp->{
            List<String> complexColumnChildren = Lists.newArrayList();
            EqpSpareVO spareVO = spareMap.get(tmp);
            StringBuilder materialName = new StringBuilder();
            materialName.append(spareVO.getName());
            if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())){
                materialName.append("/");
                materialName.append(spareVO.getCode());
            }
            if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())){
                materialName.append("/");
                materialName.append(spareVO.getSpecifications());
            }
            complexColumnChildren.add(materialName.toString());
            head.add(complexColumnChildren);
        });

        EasyExcel.write(response.getOutputStream()).sheet(fileName).head(head).doWrite(new ArrayList<>());
    }

}




