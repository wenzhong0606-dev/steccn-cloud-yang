package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.dto.*;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaDepartmentVO;
import com.steccn.oa.vo.OaEmpVO;
import com.steccn.oa.vo.OaOrgDetailVO;
import com.steccn.oa.vo.OaWorkflowStatusVO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsMaterialRequisitionMapper;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.wms.dto.*;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionServiceImpl extends ServiceImpl<PmsMaterialRequisitionMapper, PmsMaterialRequisition> implements PmsMaterialRequisitionService {

    private final PmsMaterialRequisitionInfoService pmsMaterialRequisitionInfoService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsProduceOrderService produceOrderService;
    private final PmsWorkOrderBomService pmsWorkOrderBomService;
    private final RemoteMessFeignApi remoteMessFeignApi;
    private final PmsCompleteDeliveryInfoService pmsCompleteDeliveryInfoService;
    private final RemoteStationWarehouseFeignApi remoteStationWarehouseFeignApi;
    private final RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    private final PmsMaterialRequisitionWorkflowService pmsMaterialRequisitionWorkflowService;
    private final OaInfoService oaInfoService;
    private final PmsMaterialRequisitionDemanderService pmsMaterialRequisitionDemanderService;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    @Override
    public Page<MaterialRequisitionVO> pageList(MaterialRequisitionPageDTO dto) {
        //1.处理时间
        //计划开始时间-开始
        if (Objects.nonNull(dto.getStartTime())) {
            dto.setStartCreateTime(LocalDateTime.of(dto.getStartTime(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getEndTime())) {
            dto.setEndCreateTime(LocalDateTime.of(dto.getEndTime(), LocalTime.MAX));
        }
        //2.自定义sql获取数据
        Page<MaterialRequisitionVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        if (CollUtil.isNotEmpty(page.getRecords())) {
            //3.1 处理部门数据
            Set<Integer> officeIdSet = Sets.newHashSet();
            //3.2 处理字典数据
            Set<Long> dictIdSet = Sets.newHashSet();
            Set<Long> ids = Sets.newHashSet();
            page.getRecords().forEach(materialRequisitionVO -> {
                dictIdSet.add(materialRequisitionVO.getStatus());
                dictIdSet.add(materialRequisitionVO.getTypeId());
//                officeIdSet.add(materialRequisitionVO.getDepartmentId());
                officeIdSet.add(materialRequisitionVO.getWarehouseId());
                ids.add(materialRequisitionVO.getId());
            });
            //3.3 获取字典以及部门数据
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(materialRequisitionVO -> {
                materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
                materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
                materialRequisitionVO.setDepartmentName(FeignOptional.of(() -> oaInfoService.getDepartmentInfoById(materialRequisitionVO.getDepartmentId()), FeignConstant.STECCN_OA_SERVICE_NAME).get().getDepartmentName());
                materialRequisitionVO.setWarehouseName(officeMap.get(materialRequisitionVO.getWarehouseId()));
            });
            //3.4获取总数量
            Map<Long, BigDecimal> quantityMap = pmsMaterialRequisitionInfoService.getTotalQuantity(ids);
            BigDecimal zero = BigDecimal.ZERO;
            page.getRecords().forEach(materialRequisitionVO -> {
                BigDecimal quantity = quantityMap.getOrDefault(materialRequisitionVO.getId(), zero);
                materialRequisitionVO.setQuantity(quantity);
            });
            //3.5查看是否有oa流程信息
            page.getRecords().forEach(materialRequisitionVO -> {
                PmsMaterialRequisitionWorkflow one = pmsMaterialRequisitionWorkflowService.getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequisitionId, materialRequisitionVO.getId()).orderByDesc(PmsMaterialRequisitionWorkflow::getCreateTime), false);
                if (Objects.nonNull(one)) {
                    materialRequisitionVO.setRequestId(one.getRequestId());
                }
            });
        }
        return page;
    }

    @Override
    public Long add(MaterialRequisitionDTO materialRequisitionDTO) {
        //处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //获取创建状态在字典中的id
        Long status = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId();
        model.setStatus(status);
        //获取当前创建用户所属部门
        OaEmpVO oaEmpVO = FeignOptional.of(() -> oaInfoService.getUserInfoById(SecurityUtils.getUser().getUserId().intValue()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //如果前端获取的部门是用户所属部门,那么就是oa中的值
        if (model.getDepartmentId().equals(oaEmpVO.getDepartmentid())) {
            model.setDepartmentId(oaEmpVO.getDepartmentid());
        }else {
            //找到mes对应的oa部门Id
            SysConfigurationDTO conf = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByContentAndType(new TupleDTO<>("MES_OA_DEPT", materialRequisitionDTO.getDepartmentId().toString())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if(Objects.isNull(conf.getRefContent())){
                throw new BusinessException("OA中没有对应的部门");
            }
            model.setDepartmentId(Integer.parseInt(conf.getRefContent()));
        }
        save(model);
        if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
            List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
            List<PmsMaterialRequisitionDemander> demanderList = Lists.newArrayList();
            materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
                if(Objects.isNull(materialRequisitionInfoDTO.getReceiveQuantity())){
                    materialRequisitionInfoDTO.setReceiveQuantity(BigDecimal.ZERO);
                }
                PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
                BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
                materialRequisitionInfo.setMaterialRequisitionId(model.getId());
                materialRequisitionInfo.setId(IdUtil.getSnowflake().nextId());
                list.add(materialRequisitionInfo);
                if (CollUtil.isNotEmpty(materialRequisitionInfoDTO.getDemanderList())) {
                    materialRequisitionInfoDTO.getDemanderList().forEach(demander -> {
                        PmsMaterialRequisitionDemander pmsMaterialRequisitionDemander = new PmsMaterialRequisitionDemander();
                        BeanUtil.copyProperties(demander, pmsMaterialRequisitionDemander);
                        pmsMaterialRequisitionDemander.setMaterialRequisitionId(model.getId());
                        pmsMaterialRequisitionDemander.setRequisitionInfoId(materialRequisitionInfo.getId());
                        demanderList.add(pmsMaterialRequisitionDemander);
                    });
                }
            });
            pmsMaterialRequisitionInfoService.saveBatch(list);
            pmsMaterialRequisitionDemanderService.saveBatch(demanderList);
        }
        return model.getId();
    }

    @Override
    public Boolean edit(MaterialRequisitionDTO materialRequisitionDTO) {
        //1.校验领料单状态
        PmsMaterialRequisition materialRequisition = getById(materialRequisitionDTO.getId());
        Assert.notNull(materialRequisition, "领料单不存在");
        List<Long> idSet = Lists.newArrayList();
        idSet.add(materialRequisition.getStatus());
        Map<Long, SysDictVO> dictVOMap = FeignOptional.of(() -> remoteDictFeignApi.listById(idSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(dictVOMap, "订单状态异常");
        String code = dictVOMap.get(materialRequisition.getStatus()).getCode();
        //只有审批失败和已创建状态才可编辑
        if (code.equals(PmManagerConstant.MRBS_CREATED) || code.equals(PmManagerConstant.MRBS_APPROVAL_FAIL)) {
            //2.处理数据
            PmsMaterialRequisition model = new PmsMaterialRequisition();
            BeanUtil.copyProperties(materialRequisitionDTO, model);
            //处理编辑的部门
            if(Objects.nonNull(model.getDepartmentId())&&!model.getDepartmentId().equals(materialRequisition.getDepartmentId())){
                //找到mes对应的oa部门Id
                SysConfigurationDTO conf = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByContentAndType(new TupleDTO<>("MES_OA_DEPT", model.getDepartmentId().toString())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if(Objects.isNull(conf.getRefContent())){
                    throw new BusinessException("OA中没有对应的部门");
                }
                model.setDepartmentId(Integer.parseInt(conf.getRefContent()));
            }
            Boolean aBoolean = updateById(model);
            //3.编辑数据
            if (aBoolean) {
                if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
                    aBoolean = pmsMaterialRequisitionInfoService.editList(materialRequisitionDTO);
                }
            }
            return aBoolean;
        } else {
            throw new BusinessException("订单不是可编辑状态");
        }
    }

    @Override
    public Boolean delete(Long id) {
        //1.校验领料单状态
        PmsMaterialRequisition materialRequisition = getById(id);
        Assert.notNull(materialRequisition, "领料单不存在");
        List<Long> idSet = Lists.newArrayList();
        idSet.add(materialRequisition.getStatus());
        Map<Long, SysDictVO> dictVOMap = FeignOptional.of(() -> remoteDictFeignApi.listById(idSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(dictVOMap, "订单状态异常");
        String code = dictVOMap.get(materialRequisition.getStatus()).getCode();
        //只有审批失败和已创建状态才可删除
        if (code.equals(PmManagerConstant.MRBS_CREATED) || code.equals(PmManagerConstant.MRBS_APPROVAL_FAIL)) {
            boolean b = removeById(id);
            if (b) {
                pmsMaterialRequisitionInfoService.remove(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>().eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
            }
            return b;
        } else {
            throw new BusinessException("订单不是可取消状态");
        }
    }

    @Override
    public MaterialRequisitionVO get(Long id) {
        //1.根据id 获取领料单信息
        PmsMaterialRequisition pmsMaterialRequisition = getById(id);
        Assert.notNull(pmsMaterialRequisition, "领料单信息不存在");
        //2.获取领料单详情信息
        List<PmsMaterialRequisitionInfo> list = pmsMaterialRequisitionInfoService.list(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>().eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
        //3.处理数据
        MaterialRequisitionVO materialRequisitionVO = BeanUtil.copyProperties(pmsMaterialRequisition, MaterialRequisitionVO.class);
        //3.1 处理部门数据
        Set<Integer> officeIdSet = Sets.newHashSet();
        //3.2 处理字典数据
        Set<Long> dictIdSet = Sets.newHashSet();
        //3.3 处理物料数据
        List<Long> materialIdSet = Lists.newArrayList();
        dictIdSet.add(materialRequisitionVO.getStatus());
        dictIdSet.add(materialRequisitionVO.getTypeId());
//        officeIdSet.add(materialRequisitionVO.getDepartmentId());
        officeIdSet.add(materialRequisitionVO.getWarehouseId());
        List<MaterialRequisitionInfoVO> infoList = Lists.newArrayList();
        Set<Long> infoIds = Sets.newHashSet();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(info -> {
                MaterialRequisitionInfoVO model = new MaterialRequisitionInfoVO();
                BeanUtil.copyProperties(info, model);
                dictIdSet.add(model.getUnitId());
                officeIdSet.add(model.getShelfId());
                materialIdSet.add(model.getMaterialId());
                infoIds.add(model.getId());
                if (CollUtil.isNotEmpty(model.getOrganization())) {
                    model.getOrganization().forEach(officeId -> {
                        officeIdSet.add(officeId);
                    });
                }
                infoList.add(model);
            });
        }
        //3.3 获取字典、物料以及部门数据
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EqpSpareVO eqpSpareVO = new EqpSpareVO();
        Map<Long, List<PmsMaterialRequisitionDemander>> demanderMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(infoIds)) {
            List<PmsMaterialRequisitionDemander> demanders = pmsMaterialRequisitionDemanderService.list(new LambdaQueryWrapper<PmsMaterialRequisitionDemander>().eq(PmsMaterialRequisitionDemander::getMaterialRequisitionId, pmsMaterialRequisition.getId()).in(PmsMaterialRequisitionDemander::getRequisitionInfoId, infoIds));
            demanderMap = demanders.stream().collect(Collectors.groupingBy(PmsMaterialRequisitionDemander::getRequisitionInfoId));
        }
        if (CollUtil.isNotEmpty(infoList)) {
            for (MaterialRequisitionInfoVO infoVO : infoList) {
                EqpSpareVO orDefault = materialMap.getOrDefault(infoVO.getMaterialId(), eqpSpareVO);
                infoVO.setMaterialName(orDefault.getName());
                infoVO.setShelfName(officeMap.get(infoVO.getShelfId()).getOfficeName());
                infoVO.setShelfCode(officeMap.get(infoVO.getShelfId()).getOfficeCode());
//                infoVO.setUnitName(dictMap.get(infoVO.getUnitId()));
                infoVO.setUnitName(materialMap.get(infoVO.getMaterialId()).getUnit());
                //3.3.1处理机构数据
                List<String> organizationList = Lists.newArrayList();
                if(CollUtil.isNotEmpty(infoVO.getOrganization())){
                    infoVO.getOrganization().forEach(integer -> {
                        organizationList.add(officeMap.get(integer).getOfficeName());
                    });
                }
                infoVO.setOrganizationName(organizationList);
                if (CollUtil.isNotEmpty(demanderMap)) {
                    infoVO.setDemanderList(BeanUtil.copyToList(demanderMap.get(infoVO.getId()), MaterialRequisitionInfoDemanderVO.class));
                }
            }

        }
        materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
        materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
        materialRequisitionVO.setDepartmentName(FeignOptional.of(() -> oaInfoService.getDepartmentInfoById(materialRequisitionVO.getDepartmentId()), FeignConstant.STECCN_OA_SERVICE_NAME).get().getDepartmentName());
        materialRequisitionVO.setWarehouseName(officeMap.get(materialRequisitionVO.getWarehouseId()).getOfficeName());
        materialRequisitionVO.setList(infoList);
        return materialRequisitionVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approval(MaterialRequisitionApprovalDTO dto) {
        //1.根据id获取领料单
        PmsMaterialRequisition pmsMaterialRequisition = getById(dto.getId());
        //2.校验领料单是否存在
        Assert.notNull(pmsMaterialRequisition, "该领料单不存在");
        //3.根据审批状态修改审批单
        //如果传0则是审批中
        if (dto.getApprovalFlag().equals(PmsCodeUtil.DEFAULT_PADDING)) {
            //调用oa接口发起流程
            //是已创建和拒绝状态才能发起流程
            if (pmsMaterialRequisition.getStatus().equals(FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId()) || pmsMaterialRequisition.getStatus().equals(FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_FAIL, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId())) {
                //调用oa接口调用数据
                OaWorkFlowParamDTO workflowDTO = new OaWorkFlowParamDTO();
                workflowDTO.setUserId(pmsMaterialRequisition.getCreateBy());
                workflowDTO.setWorkflowId("131");

                //1、主要数据
                List<OaWorkFlowNameValueDTO> mainDataList = Lists.newArrayList();
                //申请人
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQR, SecurityUtils.getUser().getUserId().toString()));
                //获取申请人岗位
//                OaEmpStatusDTO emp = new OaEmpStatusDTO();
//                emp.setLoginid(pmsMaterialRequisition.getCreateName());
//                List<OaEmpVO> oaEmpVOS = FeignOptional.of(() -> oaInfoService.getUserByCondition(emp), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                OaEmpVO oaEmpVO = FeignOptional.of(() -> oaInfoService.getUserInfoById(pmsMaterialRequisition.getCreateBy().intValue()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQRGW, oaEmpVO.getJobtitle()));
                //申请日期
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQRQ, pmsMaterialRequisition.getCreateTime().toLocalDate().toString()));
                //申请编号
                String requestCode = RandomUtil.randomString(4);
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQBH, requestCode));
                //总数量
                Set<Long> id = new HashSet<>();
                id.add(dto.getId());
                Map<Long, BigDecimal> quantityMap = pmsMaterialRequisitionInfoService.getTotalQuantity(id);
                BigDecimal quantity = quantityMap.getOrDefault(dto.getId(), BigDecimal.ZERO);
                if(Objects.isNull(quantity)){
                    quantity = BigDecimal.ZERO;
                }
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_ZSL, quantity.toString()));
                //领料单编码
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_LLDBM, pmsMaterialRequisition.getCode()));
                //申请部门
                Set<Integer> officeIdSet = Sets.newHashSet();
                officeIdSet.add(pmsMaterialRequisition.getDepartmentId());
                Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //查到oa中部门
                OaOrgDetailDTO oaOrg = new OaOrgDetailDTO();
                oaOrg.setId(pmsMaterialRequisition.getDepartmentId().toString());
                OaOrgDetailParamDTO oaOrgParam = new OaOrgDetailParamDTO();
                oaOrgParam.setParams(oaOrg);
                List<OaDepartmentVO> oaDepartmentVOS = FeignOptional.of(() -> oaInfoService.getOrgDetailByConditionV3(oaOrgParam), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                if (CollUtil.isNotEmpty(oaDepartmentVOS)) {
                    //申请分部
                    mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQBM, oaDepartmentVOS.get(0).getId().toString()));
                    mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_SQFB, oaDepartmentVOS.get(0).getSubCompanyId().toString()));
                }
                //类型
                Set<Long> dictIdSet = Sets.newHashSet();
                dictIdSet.add(pmsMaterialRequisition.getTypeId());
                Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Integer typeCode = PmManagerConstant.PmRequisitionWorkFlow.getCodeByName(dictMap.get(pmsMaterialRequisition.getTypeId()));
                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_LX, typeCode.toString()));
//                mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_LX, "3"));
                //备注
                if (Objects.nonNull(pmsMaterialRequisition.getNote())) {
                    mainDataList.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_BZ, pmsMaterialRequisition.getNote()));
                }
                workflowDTO.setMainData(mainDataList);

                //2、详细数据
                List<OaWorkFlowDetailObjDTO> detailDataList = Lists.newArrayList();
                OaWorkFlowDetailObjDTO oaWorkFlowDetailObjDTO = new OaWorkFlowDetailObjDTO();
                oaWorkFlowDetailObjDTO.setTableDBName(PmManagerConstant.WORKFLOW_REQ_TABLE_NAME);
                List<OaWorkFlowDetailDataListDTO> workflowRequestTableRecords = Lists.newArrayList();
                //拿到物料详细数据
                List<PmsMaterialRequisitionInfo> detailList = pmsMaterialRequisitionInfoService.list(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>().eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, dto.getId()));
                if (CollUtil.isNotEmpty(detailList)) {
                    List<Long> materialIdSet = Lists.newArrayList();
                    detailList.forEach(tmp -> {
                        materialIdSet.add(tmp.getMaterialId());
                    });
                    Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    Integer order = 0;
                    for (PmsMaterialRequisitionInfo tmp : detailList) {
                        OaWorkFlowDetailDataListDTO oaWorkFlowDetailDataListDTO = new OaWorkFlowDetailDataListDTO();
                        oaWorkFlowDetailDataListDTO.setRecordOrder(order.toString());
                        order++;
                        //组装详情数据
                        List<OaWorkFlowNameValueDTO> workFlowNameValueDTOS = Lists.newArrayList();
                        EqpSpareVO eqpSpareVO = materialMap.get(tmp.getMaterialId());
                        workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_WLBM, eqpSpareVO.getCode()));
                        workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_WLMC, eqpSpareVO.getName()));
                        if (Objects.nonNull(eqpSpareVO.getSpecifications())) {
                            workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_GGXH, eqpSpareVO.getSpecifications()));
                        }
                        if(Objects.isNull(tmp.getRequireDate())){
                            tmp.setRequireDate(tmp.getUpdateTime().toLocalDate());
                        }
                        workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_XQSJ, tmp.getRequireDate().toString()));
                        if (Objects.isNull(tmp.getReceiveQuantity())) {
                            workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_XQSL, BigDecimal.ZERO.toString()));
                        } else {
                            workFlowNameValueDTOS.add(new OaWorkFlowNameValueDTO(PmManagerConstant.WORKFLOW_XQSL, tmp.getReceiveQuantity().toString()));
                        }
                        oaWorkFlowDetailDataListDTO.setWorkflowRequestTableFields(workFlowNameValueDTOS);
                        workflowRequestTableRecords.add(oaWorkFlowDetailDataListDTO);
                    }
                    oaWorkFlowDetailObjDTO.setWorkflowRequestTableRecords(workflowRequestTableRecords);
                    detailDataList.add(oaWorkFlowDetailObjDTO);
                    workflowDTO.setDetailData(detailDataList);
                }
                workflowDTO.setRequestName("生产领料单审批流程" + requestCode);
                workflowDTO.setUserId(SecurityUtils.getUser().getUserId());
                //调用
                OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.createWorkflow(workflowDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                    throw new BusinessException("流程创建失败," + oaWorkflowStatusVO.getErrMsg());
                }
                //插入关联表
                PmsMaterialRequisitionWorkflow pmsMaterialRequisitionWorkflow = new PmsMaterialRequisitionWorkflow();
                pmsMaterialRequisitionWorkflow.setRequisitionId(dto.getId());
                pmsMaterialRequisitionWorkflow.setRequestId(oaWorkflowStatusVO.getRequestId().longValue());
                pmsMaterialRequisitionWorkflowService.save(pmsMaterialRequisitionWorkflow);
                //变成审批中状态
                pmsMaterialRequisition.setStatus(FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId());
            }
        } else if (dto.getApprovalFlag().equals(PmsCodeUtil.NUM_BIT_1)) {
            //1 表示通过
            pmsMaterialRequisition.setStatus(FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_PASS, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId());
//            pmsMaterialRequisition.setAffirmName(SecurityUtils.getUser().getName());
            //获取oa流程节点的操作人，由于通过是归档的信息，所以审批人是前一个
            PmsMaterialRequisitionWorkflow one = pmsMaterialRequisitionWorkflowService.getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequisitionId, dto.getId()).orderByDesc(PmsMaterialRequisitionWorkflow::getCreateName), false);
            //获取操作人
            OaOperatorParamDTO paramDTO = new OaOperatorParamDTO();
            paramDTO.setNodeId(Integer.parseInt(dto.getNodeId()));
            paramDTO.setRequestId(one.getRequestId().intValue());
            OaWorkFlowOperatorDTO operatorDTO = FeignOptional.of(() -> oaInfoService.getCurrentOperator(paramDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            pmsMaterialRequisition.setAffirmName(operatorDTO.getName());
            pmsMaterialRequisition.setAffirmId(operatorDTO.getId());
            pmsMaterialRequisition.setAffirmTime(LocalDateTime.now());
        } else if (dto.getApprovalFlag().equals(PmsCodeUtil.NUM_BIT_2)) {
            //2 表示失败
            pmsMaterialRequisition.setStatus(FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_FAIL, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId());
            PmsMaterialRequisitionWorkflow one = pmsMaterialRequisitionWorkflowService.getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequisitionId, dto.getId()).orderByDesc(PmsMaterialRequisitionWorkflow::getCreateName), false);
            OaOperatorParamDTO paramDTO = new OaOperatorParamDTO();
            paramDTO.setNodeId(Integer.parseInt(dto.getNodeId()));
            paramDTO.setRequestId(one.getRequestId().intValue());
            OaWorkFlowOperatorDTO operatorDTO = FeignOptional.of(() -> oaInfoService.getCurrentOperator(paramDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            pmsMaterialRequisition.setAffirmName(operatorDTO.getName());
            pmsMaterialRequisition.setAffirmId(operatorDTO.getId());
            pmsMaterialRequisition.setAffirmTime(LocalDateTime.now());
        }
        return updateById(pmsMaterialRequisition);
    }

//    @Override
//    public Page<MaterialRequisitionPlanVO> planPageList(MaterialRequisitionPlanDTO dto) {
//        //1.根据工位id获取上级的产线数据
//        Map<Long, SysOfficeVO> officeMap = FeignOptional.of(()->remoteOfficeFeign.getParentOfficeMap(dto.getStation())).get();
//        //2.获取产线id
//        List<Integer> lineIds = Lists.newArrayList();
//        officeMap.values().stream().collect(Collectors.toList()).forEach(officeVO -> {
//            lineIds.add(officeVO.getId());
//        });
//        //3.根据产线获取产线下的订单的信息
//        List<PmsProduceOrderVO> list = produceOrderService.executionOfOrder(Sets.newHashSet(lineIds));
//        //4.获取投料数量
//
//        return null;
//    }

    @Override
    public Page<MaterialRequisitionPlanVO> planPageList(MaterialRequisitionPlanDTO dto) {
        List<MaterialRequisitionPlanVO> res = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getStation())) {
            //1.根据工位id获取上级的产线数据
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.parentInfo(dto.getStation()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO sysOfficeVO = new SysOfficeVO();
            //2.获取产线id
            List<Integer> lineIds = Lists.newArrayList();
            officeMap.values().forEach(officeVO -> {
                lineIds.add(officeVO.getId());
            });
            //3.根据产线获取产线下的订单的信息
            List<PmsProduceOrderVO> list = produceOrderService.executionOfOrder(Sets.newHashSet(lineIds));
            List<MaterialRequisitionPlanVO> resBomList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(list)) {
                List<Long> orderIds = Lists.newArrayList();
                list.forEach(pmsProduceOrderVO -> orderIds.add(pmsProduceOrderVO.getId()));
                //根据订单id 获取订单信息
                Map<Long, PmsProduceOrder> orderMap = produceOrderService.orderList(orderIds);
                PmsProduceOrder order = new PmsProduceOrder();
                //3.1 根据订单id获取订单的物料组件信息
                List<PmsWorkOrderBom> bomList = pmsWorkOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>().in(PmsWorkOrderBom::getWorkProduceId, orderIds));
                //3.2 获取bom 的工序下的工位绑定的线边仓货架编码
                Set<Long> bomIds = Sets.newHashSet();
                bomList.forEach(bom -> bomIds.add(bom.getId()));
                List<BomStationDTO> stationList = pmsWorkOrderBomService.listStation(bomIds);
                //处理 工位信息
                List<MaterialStorageDTO> inventoryDTOList = Lists.newArrayList();
                Map<Long, List<BomStationDTO>> map = stationList.stream().collect(Collectors.groupingBy(BomStationDTO::getMaterialId));
                List<MaterialRequisitionPlanVO> splitBomList = Lists.newArrayList();
                //处理工位与物料
                Map<PmsWorkOrderBom, List<BomStationDTO>> bomMap = Maps.newHashMap();
                map.forEach((materialId, bomStationDTOList) -> {
                    for (PmsWorkOrderBom pmsWorkOrderBom : bomList) {
                        if (materialId.equals(pmsWorkOrderBom.getModuleId())) {
                            bomMap.put(pmsWorkOrderBom, bomStationDTOList);
                        }
                    }
                });
                //拆分数据
                //获取物料的投料信息
                List<FeedQuantityDTO> feedList = Lists.newArrayList();
                bomMap.forEach((bom, stations) -> {
                    FeedQuantityDTO feedQuantityDTO = new FeedQuantityDTO();
                    feedQuantityDTO.setProduceOrderId(bom.getWorkProduceId());
                    feedQuantityDTO.setMaterialId(bom.getModuleId());
                    feedQuantityDTO.setProcessId(bom.getProcessId());
                    feedList.add(feedQuantityDTO);
                });
                Map<FeedQuantityDTO, BigDecimal> feedQuantityMap = pmsCompleteDeliveryInfoService.listQuantity(feedList);
                BigDecimal quantity = BigDecimal.ZERO;
                Set<Long> dictList = Sets.newHashSet();
                Set<Integer> officeId = Sets.newHashSet();
                List<Long> materialIds = Lists.newArrayList();
                Set<Integer> stationIds = Sets.newHashSet();
                bomMap.forEach((bom, stations) -> {
                    int size = stations.size();
                    Set<Integer> stationIdSet = Sets.newHashSet();
                    BigDecimal bigDecimalSize = BigDecimal.valueOf(size);
                    FeedQuantityDTO feedQuantityDTO = new FeedQuantityDTO();
                    feedQuantityDTO.setProduceOrderId(bom.getWorkProduceId());
                    feedQuantityDTO.setMaterialId(bom.getModuleId());
                    feedQuantityDTO.setProcessId(bom.getProcessId());
                    BigDecimal orDefault = feedQuantityMap.getOrDefault(feedQuantityDTO, quantity);
                    PmsProduceOrder orderInfo = orderMap.getOrDefault(bom.getWorkProduceId(), order);
                    for (BomStationDTO station : stations) {
                        SysOfficeVO sysOffice = officeMap.getOrDefault(station.getExecuteStationId(), sysOfficeVO);
                        MaterialRequisitionPlanVO materialRequisitionPlanVO = new MaterialRequisitionPlanVO();
                        materialRequisitionPlanVO.setMaterialId(bom.getModuleId());
                        //四舍五入，保留三位小数的除法
                        BigDecimal divide = BigDecimalUtil.divide(bom.getQuantity(), bigDecimalSize);
                        materialRequisitionPlanVO.setRequireQuantity(divide);
                        materialRequisitionPlanVO.setStationName(station.getExecuteStationName());
                        materialRequisitionPlanVO.setStation(station.getExecuteStationId());
                        materialRequisitionPlanVO.setStationCode(station.getExecuteStationCode());
                        materialRequisitionPlanVO.setOnPassageQuantity(BigDecimal.ZERO);
                        materialRequisitionPlanVO.setProduceOrderId(bom.getWorkProduceId());
                        materialRequisitionPlanVO.setProcessId(bom.getProcessId());
                        materialRequisitionPlanVO.setProductionLine(sysOffice.getId());
                        materialRequisitionPlanVO.setProductionLineName(sysOffice.getOfficeName());
                        materialRequisitionPlanVO.setWorkshop(sysOffice.getParentId());
                        materialRequisitionPlanVO.setWorkshopName(sysOffice.getParentName());
                        materialRequisitionPlanVO.setUnitId(bom.getUnitId());
                        //需求时间
                        LocalDateTime planStartTime = orderInfo.getPlanStartTime();
                        LocalDate date = planStartTime.toLocalDate();
                        materialRequisitionPlanVO.setRequireDate(date);
                        dictList.add(bom.getUnitId());
                        officeId.add(station.getExecuteStationId());
                        materialIds.add(bom.getModuleId());
//                    materialRequisitionPlanVO.setRequireDate();
                        //需求数量（Bom 需求数量 -已投入数量）/ 工位个数
                        BigDecimal requiredQuantity = bom.getQuantity().subtract(orDefault);
                        BigDecimal requireQuantity = BigDecimalUtil.divide(requiredQuantity, bigDecimalSize);
                        materialRequisitionPlanVO.setRequireQuantity(requireQuantity);
                        splitBomList.add(materialRequisitionPlanVO);
                        stationIds.add(materialRequisitionPlanVO.getStation());
                        stationIdSet.add(materialRequisitionPlanVO.getStation());
                    }
                    //处理物料线边库存数据

                    MaterialStorageDTO storageDTO = new MaterialStorageDTO();
                    storageDTO.setMaterialId(bom.getModuleId());
                    storageDTO.setStationId(stationIdSet);
                    inventoryDTOList.add(storageDTO);
                });
                //获取单位数据以及车间产线数据
                Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //获取工位绑定的仓库
                Map<Integer, List<SysStationWarehouseVO>> warehouseMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<SysStationWarehouseVO> warehouseVOList = Lists.newArrayList();
                // 获取物料绑定的线边仓库存数据
                inventoryDTOList.forEach(inventoryDTO -> {
                    List<String> warehouseCodes = Lists.newArrayList();
                    inventoryDTO.getStationId().forEach(station -> {
                        List<SysStationWarehouseVO> warehouseList = warehouseMap.getOrDefault(station, warehouseVOList);
                        warehouseList.forEach(warehouse -> warehouseCodes.add(warehouse.getShelfCode()));
                    });
                    inventoryDTO.setShelfCodes(warehouseCodes);
                });
                List<MaterialStorageDTO> collect = inventoryDTOList.stream().filter(inventoryDTO -> inventoryDTO.getShelfCodes().size() > 0).collect(Collectors.toList());
                Map<Long, BigDecimal> inventoryMap = FeignOptional.of(() -> remoteWmsInventoryFeignApi.listMaterialInventory(collect), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
                // 获取物料的数据
                EqpSpareVO eqpSpareVO = new EqpSpareVO();
                Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                BigDecimal zero = BigDecimal.ZERO;
                splitBomList.forEach(bom -> {
                    EqpSpareVO spareVO = spareMap.getOrDefault(bom.getMaterialId(), eqpSpareVO);
                    bom.setUnitName(dictMap.get(bom.getUnitId()));
                    bom.setMaterialName(spareVO.getName());
                    bom.setMaterialCode(spareVO.getCode());
                    BigDecimal orDefault = inventoryMap.getOrDefault(bom.getMaterialId(), zero);
                    //线边数量
                    bom.setLineEdgeQuantity(orDefault);
                    //批次数量
                    bom.setBatchDeliveryQuantity(BigDecimal.ZERO);
                    if (bom.getRequireQuantity().subtract(orDefault).compareTo(BigDecimal.ZERO) > 0) {
                        resBomList.add(bom);
                    }
                });
            }
            List<Integer> stationList = dto.getStation();
            //过滤数据 只返回在当前工位上的物料信息
            res = resBomList.stream().filter(plan -> stationList.contains(plan.getStation())).collect(Collectors.toList());
        }
        return LogicalPageUtil.page(res, dto.getCurrent(), dto.getSize());
    }

    @Override
    public R<String> create(List<MaterialRequisitionInfoPlanDTO> list) {
        R<String> r = new R<>();
        MaterialRequisitionDTO materialRequisitionDTO = new MaterialRequisitionDTO();
        List<MaterialRequisitionInfoDTO> infoList = Lists.newArrayList();
        Set<Integer> stationIds = Sets.newHashSet();
        list.forEach(info -> stationIds.add(info.getWorkshopId()));
        //获取工位的线边仓
        Map<Integer, List<SysStationWarehouseVO>> warehouseMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysStationWarehouseVO> warehouseVOList = Lists.newArrayList();
        list.forEach(info -> {
            MaterialRequisitionInfoDTO materialRequisitionInfoDTO = new MaterialRequisitionInfoDTO();
            List<SysStationWarehouseVO> orDefault = warehouseMap.getOrDefault(info.getWorkshopId(), warehouseVOList);
            BeanUtil.copyProperties(info, materialRequisitionInfoDTO);
            materialRequisitionInfoDTO.setReceiveQuantity(info.getQuantity());
            if (CollUtil.isNotEmpty(orDefault)) {
                materialRequisitionInfoDTO.setShelfId(orDefault.get(0).getShelfId());
                materialRequisitionInfoDTO.setShelfName(orDefault.get(0).getShelfName());
            }
            infoList.add(materialRequisitionInfoDTO);
        });
        materialRequisitionDTO.setList(infoList);
        //获取部门
        SysOfficeVO sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.parent(list.get(0).getWorkshopId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        materialRequisitionDTO.setDepartmentId(sysOfficeVO.getId());
        //获取类型
        SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.PRODUCE_PICK, PmManagerConstant.MATERIAL_REQUISITION), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        materialRequisitionDTO.setTypeId(dictVO.getId());
        String order = createOrder(materialRequisitionDTO);
        r.setCode(1);
        r.setData(order);
        return r;
    }

    @Override
    public Long getStatusById(Long id) {
        PmsMaterialRequisition byId = getById(id);
        if (Objects.nonNull(byId)) {
            return byId.getStatus();
        }
        return null;
    }

    @Override
    public Boolean scan(ScanDto scanDto) {
        //回填领料单关联的调拨单或者出库单单号 并更新领用数量
        Assert.notNull(scanDto.getList(), "扫描明细不能为空");
        //根据类型生成出库单或者调拨单
        if (PmManagerConstant.OrderEnum.ONE.getCode().equals(scanDto.getType())) {
            //生成出库单
            WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
            SysDictVO dict = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", "outbound_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            wmsDeliveryReceiptDTO.setType(dict.getId());
            wmsDeliveryReceiptDTO.setWarehouseId(scanDto.getList().get(0).getWarehouseId());
            List<WmsDeliveryReceiptInfoDTO> wmsDeliveryReceiptInfoDTOS = BeanUtil.copyToList(scanDto.getList(), WmsDeliveryReceiptInfoDTO.class);
            wmsDeliveryReceiptDTO.setList(wmsDeliveryReceiptInfoDTOS);
            return FeignOptional.of(() -> remoteWmsInventoryFeignApi.createDelivery(wmsDeliveryReceiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        } else {
            //生成调拨单
            WmsTransferSlipDTO wmsTransferSlipDTO = new WmsTransferSlipDTO();
            SysDictVO dict = FeignOptional.of(() -> remoteDictFeignApi.getByCode("other_allocation001", "transfer_slip_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            wmsTransferSlipDTO.setType(dict.getId());
            ScanInfoDto scanInfoDto = scanDto.getList().get(0);
            SysOfficeVO office = FeignOptional.of(() -> remoteOfficeFeign.shelfWarehouse(scanInfoDto.getDemandShelfId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            wmsTransferSlipDTO.setSourceWarehouse(scanInfoDto.getWarehouseId());
            wmsTransferSlipDTO.setTargetWarehouse(office.getId());
            List<WmsTransferSlipInfoDTO> list = new ArrayList<>();
            scanDto.getList().forEach(info -> {
                WmsTransferSlipInfoDTO wmsTransferSlipInfoDTO = new WmsTransferSlipInfoDTO();
                wmsTransferSlipInfoDTO.setMaterialId(info.getMaterialId());
                wmsTransferSlipInfoDTO.setMaterialName(info.getMaterialName());
                wmsTransferSlipInfoDTO.setMaterialCode(info.getMaterialCode());
                wmsTransferSlipInfoDTO.setSourceInventoryId(info.getInventoryId());
                wmsTransferSlipInfoDTO.setSourceShelf(info.getShelfId());
                wmsTransferSlipInfoDTO.setSourceShelfCode(info.getShelfCode());
                wmsTransferSlipInfoDTO.setSourceShelfName(info.getShelfName());
                wmsTransferSlipInfoDTO.setTargetShelf(info.getDemandShelfId());
                wmsTransferSlipInfoDTO.setTargetShelfCode(info.getDemandShelfCode());
                wmsTransferSlipInfoDTO.setTargetShelfName(info.getDemandShelfName());
                wmsTransferSlipInfoDTO.setQuantity(info.getQuantity());
                wmsTransferSlipInfoDTO.setUnit(info.getUnitId());
                wmsTransferSlipInfoDTO.setBatch(info.getBatch());
                wmsTransferSlipInfoDTO.setCaseNo(info.getCaseNo());
                list.add(wmsTransferSlipInfoDTO);
            });
            wmsTransferSlipDTO.setList(list);
            return FeignOptional.of(() -> remoteWmsInventoryFeignApi.createTransferSlip(wmsTransferSlipDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        }
    }

    @Override
    public List<CommonReportDTO> substrate(DpPmsOperateDTO dto) {
        List<CommonReportDTO> resList = Lists.newArrayList();
        //获取当前部门（领用部门）下的部门的领用数据
        List<Integer> officeIds = Lists.newArrayList();
        officeIds.add(dto.getOfficeId());
        SysOfficeParentDTO officeDTO = new SysOfficeParentDTO();
        officeDTO.setId(dto.getOfficeId());
        List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> integers = Lists.newArrayList();
        if (CollUtil.isNotEmpty(officeVOS)) {
            integers.addAll(officeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toList()));
        }
        integers.add(dto.getOfficeId());
        integers.add(dto.getOfficeId());
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<RequisitionInfoDTO> requisitionInfoList = substrateData(granularity,integers, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        List<String> materialNames = Arrays.asList("石英砂（低铁硅砂）","纯碱","方解石","白云石（普通白云石）","氧化铝","复合澄清剂","碎玻璃（外购）");
        Map<String, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listByName(materialNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Long> materialIds =  Lists.newArrayList();
        spareMap.forEach( (key,value)->{
            materialIds.add(value.getId());
        });
        if (CollUtil.isNotEmpty(requisitionInfoList)){
            List<RequisitionInfoDTO> collect = requisitionInfoList.stream().filter(vo -> materialIds.contains(vo.getMaterialId())).collect(Collectors.toList());
            Map<Long, List<RequisitionInfoDTO>> map = collect.stream().collect(Collectors.groupingBy(RequisitionInfoDTO::getMaterialId));
            spareMap.forEach( (k,v)->{
                CommonReportDTO commonReportDTO = new CommonReportDTO();
                Map<String,BigDecimal> valueMap = Maps.newHashMap();
                commonReportDTO.setName(v.getName());
                List<RequisitionInfoDTO> requisitionInfoDTOS = map.get(v.getId());
                if (CollUtil.isNotEmpty(requisitionInfoDTOS)){
                    collect.forEach( ele ->{
                        String convert = MysqlDateKeyUtil.convert(granularity,ele.getRequireDate());
                        MapUtil.add(valueMap,convert,ele.getReceiveQuantity(),(x,y)->BigDecimalUtil.add(x,y));
                    });
                }
                commonReportDTO.setUnit(v.getUnit());
                commonReportDTO.setValueMap(valueMap);
                resList.add(commonReportDTO);
            });
        }else {
            spareMap.forEach( (k,v)->{
                CommonReportDTO commonReportDTO = new CommonReportDTO();
                commonReportDTO.setName(v.getName());
                commonReportDTO.setUnit(v.getUnit());
                resList.add(commonReportDTO);
            });
        }
        return resList;
    }

    public  List<RequisitionInfoDTO> substrateData(QtManagerConstant.QtGranularityEnum granularity,List<Integer> integers, TupleDTO<LocalDateTime,LocalDateTime> dto){
        List<RequisitionInfoDTO> requisitionInfoList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            requisitionInfoList = this.baseMapper.substrateDayData(integers,dto.getField1(),dto.getField2());
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            requisitionInfoList = this.baseMapper.substrateWeekData(integers,dto.getField1(),dto.getField2());
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            requisitionInfoList = this.baseMapper.substrateMonthData(integers,dto.getField1(),dto.getField2());
        } else {
            requisitionInfoList = this.baseMapper.substrateYearData(integers,dto.getField1(),dto.getField2());
        }
        return requisitionInfoList;
    }

    private String createOrder(MaterialRequisitionDTO materialRequisitionDTO) {
        //处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //获取创建状态在字典中的id
        Long status = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED, PmManagerConstant.MATERIAL_REQUISITION_STATUS), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().getId();
        model.setStatus(status);
        save(model);
        if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
            List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
            materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
                PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
                BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
                materialRequisitionInfo.setMaterialRequisitionId(model.getId());
                list.add(materialRequisitionInfo);
            });
            pmsMaterialRequisitionInfoService.saveBatch(list);
        }
        return model.getCode();
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        PmsMaterialRequisition maxCodeModel = getOne(new LambdaQueryWrapper<PmsMaterialRequisition>().between(PmsMaterialRequisition::getCreateTime, min, max).orderByDesc(PmsMaterialRequisition::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION.length() + PmsCodeUtil.NUM_BIT_8 + PmsCodeUtil.NUM_BIT_2);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




