package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsPullForceDTO;
import com.steccn.pms.dto.PmsPullForceDetailsDTO;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.mapper.PmsPullForceMapper;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
public class PmsPullForceServiceImpl extends ServiceImpl<PmsPullForceMapper, PmsPullForce>
        implements PmsPullForceService {

    @Override
    public List<PmsPullForceVO> list(PmsPullForceDTO dto) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getWorkshopId, dto.getWorkshopId())
                .like(PmsPullForce::getPullForceDate, dto.getMonthDate() + "%"));

        //根据日期分组
        Map<LocalDate, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getPullForceDate));

        List<PmsPullForceVO> pmsPullForceVOList = new ArrayList<>();
        //组装返回参数
        for (Map.Entry<LocalDate, List<PmsPullForce>> entry : groupBy.entrySet()) {
            PmsPullForceVO pullForceVO = new PmsPullForceVO();
            pullForceVO.setPullForceDate(entry.getKey());
            pullForceVO.setWorkshopId(entry.getValue().get(0).getWorkshopId());

            List<PmsPullForce> localDateGroupList = entry.getValue();
            //再按班次分组
            Map<Long, List<PmsPullForce>> classMap = localDateGroupList.stream().collect(Collectors.groupingBy(PmsPullForce::getClassesId));
            for (Map.Entry<Long, List<PmsPullForce>> classEntry : classMap.entrySet()){
                //获取白班夜班总量
                BigDecimal dayClassTotal = classEntry.getValue().stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (classEntry.getKey().equals(0L)) {
                    pullForceVO.setDayClassTotal(dayClassTotal);
                } else {
                    pullForceVO.setNightClassTotal(dayClassTotal);
                }
            }
            //获取总量
            BigDecimal total =localDateGroupList.stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            pullForceVO.setTotal(total);
            pmsPullForceVOList.add(pullForceVO);
        }

        return pmsPullForceVOList;
    }

    @Override
    public PmsPullForceDetailsVO details(PmsPullForceDetailsDTO dto) {
        return null;
    }

    @Override
    public Boolean editBatch(List<PmsPullForceEditDTO> dtoList) {
        return null;
    }
}




