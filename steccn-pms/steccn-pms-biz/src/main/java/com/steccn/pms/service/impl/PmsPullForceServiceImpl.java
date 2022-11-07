package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.mapper.PmsPullForceMapper;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@RequiredArgsConstructor
@Service
public class PmsPullForceServiceImpl extends ServiceImpl<PmsPullForceMapper, PmsPullForce>
        implements PmsPullForceService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public List<PmsPullForceVO> list(Integer workshopId, String monthDate) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getWorkshopId, workshopId)
                .like(PmsPullForce::getPullForceDate, monthDate));

        //根据日期分组
        Map<LocalDate, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getPullForceDate));
        //获取白班和夜班的id集合
        Set<Long> classIdSet = pmsPullForceList.stream().map(PmsPullForce::getClassesId).collect(Collectors.toSet());
        Map<Long, SysDictVO> classIdMap = remoteDictFeignApi.listById(new ArrayList<>(classIdSet)).getData();



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
                String classCode = classIdMap.get(classEntry.getKey()).getCode();

                if (SysDictConstant.CODE_0.equals(classCode)) {
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
    public List<PmsPullForceDetailsVO> details(Integer workshopId, String pullForceDate) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getWorkshopId, workshopId)
                .eq(PmsPullForce::getPullForceDate, pullForceDate));

        //根据产线分组
        Map<Integer, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        Set<Integer> productLineIdSet = pmsPullForceList.stream().map(PmsPullForce::getProductLineId).collect(Collectors.toSet());
        Set<Long> classIdSet = pmsPullForceList.stream().map(PmsPullForce::getClassesId).collect(Collectors.toSet());

        Map<Integer, SysOfficeVO> map = remoteOfficeFeign.listById(productLineIdSet).getData();
        Map<Long, SysDictVO> classIdMap = remoteDictFeignApi.listById(new ArrayList<>(classIdSet)).getData();

        List<PmsPullForceDetailsVO> pullForceDetailsVOList = new ArrayList<>();
        for (Map.Entry<Integer, List<PmsPullForce>> entry : groupBy.entrySet()) {
            BigDecimal total =entry.getValue().stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            PmsPullForceDetailsVO pullForceDetailsVO = new PmsPullForceDetailsVO();
            pullForceDetailsVO.setProductLineId(entry.getKey());
            pullForceDetailsVO.setProductLineName(map.get(entry.getKey()).getOfficeName());
            pullForceDetailsVO.setId(entry.getValue().get(0).getId());
            pullForceDetailsVO.setTotal(total);

            //获取白班夜班数量
            for (PmsPullForce pmsPullForce : entry.getValue()) {
                String classCode = classIdMap.get(pmsPullForce.getClassesId()).getCode();
                if (SysDictConstant.CODE_0.equals(classCode)) {
                    pullForceDetailsVO.setDayClassQuantity(pmsPullForce.getQuantity());
                } else {
                    pullForceDetailsVO.setNightClassQuantity(pmsPullForce.getQuantity());
                }
            }

            pullForceDetailsVOList.add(pullForceDetailsVO);
        }

        return pullForceDetailsVOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean editBatch(List<PmsPullForceEditDTO> dtoList) {
        List<PmsPullForce> addList = new ArrayList<>();
        List<PmsPullForce> updateList = new ArrayList<>();

        for (PmsPullForceEditDTO pmsPullForceEditDTO : dtoList) {
            PmsPullForce pmsPullForce = new PmsPullForce();
            BeanUtils.copyProperties(pmsPullForceEditDTO,pmsPullForce);

            if (Objects.isNull(pmsPullForceEditDTO.getId())) {
                addList.add(pmsPullForce);
            } else {
                updateList.add(pmsPullForce);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            if (!saveBatch(addList)) {
                throw new BusinessException("拉引量新增失败");
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            if (!updateBatchById(updateList)) {
                throw new BusinessException("拉引量编辑失败");
            }
        }
        return true;
    }
}




