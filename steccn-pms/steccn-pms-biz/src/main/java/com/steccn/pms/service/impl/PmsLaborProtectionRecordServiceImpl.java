package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsLaborProtectionRecord;
import com.steccn.pms.dto.PmsLaborRecordPageDTO;
import com.steccn.pms.mapper.PmsLaborProtectionRecordMapper;
import com.steccn.pms.service.PmsLaborProtectionRecordService;
import com.steccn.pms.vo.PmsLaborRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Override
    public Page<PmsLaborRecordVO> pageList(PmsLaborRecordPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmsLaborProtectionRecord> condition = new LambdaQueryWrapper<PmsLaborProtectionRecord>()
                .eq(Objects.nonNull(dto.getDepCode()),PmsLaborProtectionRecord::getDepCode, dto.getDepCode())
                .eq(Objects.nonNull(dto.getPostCode()),PmsLaborProtectionRecord::getPostCode, dto.getPostCode())
                .like(Objects.nonNull(dto.getUserName()), PmsLaborProtectionRecord::getLoginName, dto.getUserName());
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
}




