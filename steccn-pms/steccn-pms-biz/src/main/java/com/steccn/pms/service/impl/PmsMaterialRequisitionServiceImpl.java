package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMaterialRequisition;
import com.steccn.pms.domain.PmsMaterialRequisitionInfo;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.dto.MaterialRequisitionDTO;
import com.steccn.pms.dto.MaterialRequisitionPageDTO;
import com.steccn.pms.service.PmsMaterialRequisitionInfoService;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.mapper.PmsMaterialRequisitionMapper;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.MaterialRequisitionInfoVO;
import com.steccn.pms.vo.MaterialRequisitionVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionServiceImpl extends ServiceImpl<PmsMaterialRequisitionMapper, PmsMaterialRequisition>
        implements PmsMaterialRequisitionService {

    private final PmsMaterialRequisitionInfoService pmsMaterialRequisitionInfoService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

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
            page.getRecords().forEach(materialRequisitionVO -> {
                dictIdSet.add(materialRequisitionVO.getStatus());
                dictIdSet.add(materialRequisitionVO.getTypeId());
                officeIdSet.add(materialRequisitionVO.getDepartmentId());
            });
            //3.3 获取字典以及部门数据
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
            page.getRecords().forEach(materialRequisitionVO -> {
                materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
                materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
                materialRequisitionVO.setDepartmentName(officeMap.get(materialRequisitionVO.getDepartmentId()));
            });
            remoteOfficeFeign.listByIdCh(officeIdSet);
        }
        return page;
    }

    @Override
    public Boolean add(MaterialRequisitionDTO materialRequisitionDTO) {
        //处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //获取创建状态在字典中的id
        Long status = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED,
                PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId();
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
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(MaterialRequisitionDTO materialRequisitionDTO) {
        //1.校验领料单状态
        PmsMaterialRequisition materialRequisition = getById(materialRequisitionDTO.getId());
        Assert.notNull(materialRequisition, "领料单不存在");
        List<Long> idSet = Lists.newArrayList();
        idSet.add(materialRequisition.getStatus());
        Map<Long, SysDictVO> dictVOMap = FeignOptional.of(remoteDictFeignApi.listById(idSet)).get();
        Assert.notNull(dictVOMap, "订单状态异常");
        String code = dictVOMap.get(materialRequisition.getStatus()).getCode();
        Assert.isTrue(code.equals(PmManagerConstant.MRBS_CREATED), "领料单不是可编辑状态");
        //2.处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        Boolean aBoolean = updateById(model);
        //3.编辑数据
        if (aBoolean) {
            if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
                aBoolean = pmsMaterialRequisitionInfoService.editList(materialRequisitionDTO);
            }
        }
        return aBoolean;
    }

    @Override
    public Boolean delete(Long id) {
        boolean b = removeById(id);
        if (b) {
            pmsMaterialRequisitionInfoService.remove(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                    .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
        }
        return b;
    }

    @Override
    public MaterialRequisitionVO get(Long id) {
        //1.根据id 获取领料单信息
        PmsMaterialRequisition pmsMaterialRequisition = getById(id);
        Assert.notNull(pmsMaterialRequisition, "领料单信息不存在");
        //2.获取领料单详情信息
        List<PmsMaterialRequisitionInfo> list = pmsMaterialRequisitionInfoService.list(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
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
        officeIdSet.add(materialRequisitionVO.getDepartmentId());
        officeIdSet.add(materialRequisitionVO.getWarehouseId());
        List<MaterialRequisitionInfoVO> infoList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(info -> {
                MaterialRequisitionInfoVO model = new MaterialRequisitionInfoVO();
                BeanUtil.copyProperties(info, model);
                dictIdSet.add(model.getUnitId());
                officeIdSet.add(model.getShelfId());
                materialIdSet.add(model.getMaterialId());
                if (CollUtil.isNotEmpty(model.getOrganization())) {
                    model.getOrganization().forEach(officeId -> {
                                officeIdSet.add(officeId);
                            }
                    );
                }
                infoList.add(model);
            });
        }
        //3.3 获取字典、物料以及部门数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIdSet)).get();
        EqpSpareVO eqpSpareVO = new EqpSpareVO();
        if (CollUtil.isNotEmpty(infoList)) {
            infoList.forEach(
                    infoVO -> {
                        EqpSpareVO orDefault = materialMap.getOrDefault(infoVO.getMaterialId(), eqpSpareVO);
                        infoVO.setMaterialName(orDefault.getName());
                        infoVO.setShelfName(officeMap.get(infoVO.getShelfId()));
                        infoVO.setUnitName(dictMap.get(infoVO.getUnitId()));
                        //3.3.1处理机构数据
                        List<String> organizationList = Lists.newArrayList();
                        infoVO.getOrganization().forEach(
                                integer -> {
                                    organizationList.add(officeMap.get(integer));
                                }
                        );
                        infoVO.setOrganizationName(organizationList);
                    }
            );
        }
        materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
        materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
        materialRequisitionVO.setDepartmentName(officeMap.get(materialRequisitionVO.getDepartmentId()));
        materialRequisitionVO.setWarehouseName(officeMap.get(materialRequisitionVO.getWarehouseId()));
        materialRequisitionVO.setList(infoList);
        return materialRequisitionVO;
    }

    @Override
    public Boolean approval(MaterialRequisitionApprovalDTO dto) {
        //1.根据id获取领料单
        PmsMaterialRequisition pmsMaterialRequisition = getById(dto.getId());
        //2.校验领料单是否存在
        Assert.notNull(pmsMaterialRequisition, "该领料单不存在");
        //3.根据审批状态修改审批单
        if (dto.getApprovalFlag().equals(PmsCodeUtil.DEFAULT_PADDING)) {
            pmsMaterialRequisition.setStatus(FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_PASS,
                    PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId());
        } else {
            pmsMaterialRequisition.setStatus(FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_FAIL,
                    PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId());
        }
        return updateById(pmsMaterialRequisition);
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
        PmsMaterialRequisition maxCodeModel = getOne(new LambdaQueryWrapper<PmsMaterialRequisition>()
                .between(PmsMaterialRequisition::getCreateTime, min, max)
                .orderByDesc(PmsMaterialRequisition::getCode)
                .last(MpConstant.STR_LIMIT_1));
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




