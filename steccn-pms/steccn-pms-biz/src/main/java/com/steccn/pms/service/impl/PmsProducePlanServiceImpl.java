package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.PmsProducePlanDetailsDTO;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.mapper.PmsProducePlanMapper;
import com.steccn.pms.vo.PmsProducePlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_produce_plan(生产月计划)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@RequiredArgsConstructor
@Service
public class PmsProducePlanServiceImpl extends ServiceImpl<PmsProducePlanMapper, PmsProducePlan>
        implements PmsProducePlanService {

    @Override
    public List<PmsProducePlanDTO> list(String monthDate) {
        //根据月份查询数据
        List<PmsProducePlanVO> producePlanVOList = this.baseMapper.selectAllByPlanDate(monthDate);

        Map<String, List<PmsProducePlanVO>> groupBy = producePlanVOList
                .stream()
                .collect(Collectors.groupingBy(pmsProducePlanVO -> pmsProducePlanVO.getMaterialName()+","+pmsProducePlanVO.getMaterialName()+","+pmsProducePlanVO.getMaterialId()));

        List<PmsProducePlanDTO> pmsProducePlanDTOList = new ArrayList<>();

        for (Map.Entry<String, List<PmsProducePlanVO>> entry: groupBy.entrySet()) {
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
                BeanUtils.copyProperties(pmsProducePlanVO,planDetailsDTO);
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
}




