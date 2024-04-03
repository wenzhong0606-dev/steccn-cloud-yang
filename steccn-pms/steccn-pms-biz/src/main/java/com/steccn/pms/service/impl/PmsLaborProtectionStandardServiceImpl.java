package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsLaborProtectionStandard;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.dto.PmsLaborStandardDTO;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import com.steccn.pms.service.PmsLaborProtectionStandardService;
import com.steccn.pms.mapper.PmsLaborProtectionStandardMapper;
import com.steccn.pms.vo.PmsLaborStandardDetailVO;
import com.steccn.pms.vo.PmsLaborStandardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【pms_labor_protection_standard(劳保标准表)】的数据库操作Service实现
* @createDate 2024-04-03 10:21:46
*/
@Service
@RequiredArgsConstructor
public class PmsLaborProtectionStandardServiceImpl extends ServiceImpl<PmsLaborProtectionStandardMapper, PmsLaborProtectionStandard>
    implements PmsLaborProtectionStandardService{
    private final PmsLaborProtectionStandardDetailService pmsLaborProtectionStandardDetailService;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(PmsLaborStandardDTO dto) {
        //保存主表
        PmsLaborProtectionStandard model = new PmsLaborProtectionStandard();
        BeanUtil.copyProperties(dto,model);
        save(model);
        //保存详情表
        List<PmsLaborProtectionStandardDetail> models = Lists.newArrayList();
        dto.getDetails().forEach(tmp->{
            PmsLaborProtectionStandardDetail detail = new PmsLaborProtectionStandardDetail();
            BeanUtil.copyProperties(tmp,detail);
            detail.setStandardId(model.getId());
            models.add(detail);
        });
        pmsLaborProtectionStandardDetailService.saveBatch(models);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(PmsLaborStandardDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("id不能为空");
        }
        //修改主表
        PmsLaborProtectionStandard model = new PmsLaborProtectionStandard();
        BeanUtil.copyProperties(dto,model);
        updateById(model);
        //修改从表
        pmsLaborProtectionStandardDetailService.remove(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .eq(PmsLaborProtectionStandardDetail::getStandardId,dto.getId()));
        List<PmsLaborProtectionStandardDetail> models = Lists.newArrayList();
        dto.getDetails().forEach(tmp->{
            PmsLaborProtectionStandardDetail detail = new PmsLaborProtectionStandardDetail();
            BeanUtil.copyProperties(tmp,detail);
            detail.setStandardId(dto.getId());
            models.add(detail);
        });
        pmsLaborProtectionStandardDetailService.saveBatch(models);
        return true;
    }

    @Override
    public PmsLaborStandardVO info(Integer id) {
        PmsLaborStandardVO vo = new PmsLaborStandardVO();
        PmsLaborProtectionStandard standard = getById(id);
        BeanUtil.copyProperties(standard,vo);
        List<PmsLaborProtectionStandardDetail> standardDetail = pmsLaborProtectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .eq(PmsLaborProtectionStandardDetail::getStandardId, id));
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(standardDetail.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).collect(Collectors.toList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<PmsLaborStandardDetailVO> details = Lists.newArrayList();
        standardDetail.forEach(tmp->{
            PmsLaborStandardDetailVO detail = new PmsLaborStandardDetailVO();
            BeanUtil.copyProperties(tmp,detail);
            detail.setMaterialName(materialMap.get(tmp.getMaterialId()).getName());
            details.add(detail);
        });
        vo.setDetails(details);
        return vo;
    }
}




