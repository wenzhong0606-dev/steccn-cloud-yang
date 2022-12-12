package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.ems.domain.EpEnergyPricePlan;
import com.steccn.ems.domain.EpEnergyPricePlanDetail;
import com.steccn.ems.dto.EpEnergyPriceDTO;
import com.steccn.ems.service.EpEnergyPricePlanDetailService;
import com.steccn.ems.service.EpEnergyPricePlanService;
import com.steccn.ems.mapper.EpEnergyPricePlanMapper;
import com.steccn.ems.vo.EpEnergyPriceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_energy_price_plan(能源价格方案)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
@Slf4j
public class EpEnergyPricePlanServiceImpl extends ServiceImpl<EpEnergyPricePlanMapper, EpEnergyPricePlan>
    implements EpEnergyPricePlanService{

    @Autowired
    private EpEnergyPricePlanDetailService energyPricePlanDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(EpEnergyPriceDTO dto) {
        long size = count(new LambdaQueryWrapper<EpEnergyPricePlan>()
                .lt(EpEnergyPricePlan::getValidStart, dto.getValidEnd())
                .gt(EpEnergyPricePlan::getValidEnd, dto.getValidStart()));
        //根据有效期查询数据库现有保存的能源价格方案
        if(Objects.isNull(dto.getId())){
            Assert.isTrue(size==0,"与其他方案的有效期有重叠，无法保存");
        }else if (Objects.nonNull(dto.getId())){
            Assert.isTrue(size<=1,"与其他方案的有效期有重叠”，无法保存");
        }
        //创建domain实体类对象
        EpEnergyPricePlan epEnergyPricePlan = new EpEnergyPricePlan();
        BeanUtils.copyProperties(dto,epEnergyPricePlan);
        //设置单位
        epEnergyPricePlan.setUnit(1587999091661488128L);
        if (Objects.isNull(dto.getId())){
            //新建的时候设置价格方案名称
            long sum = count();
            epEnergyPricePlan.setName("价格方案"+(sum+1));
        }
        //保存或更新能源价格方案
        if (Objects.isNull(dto.getId())){
            save(epEnergyPricePlan);
            dto.setId(epEnergyPricePlan.getId());
        }else if (Objects.nonNull(dto.getId())){
            //去数据库中根据id先查找 如果没有返回错误
            EpEnergyPricePlan energyPricePlan = getById(dto.getId());
            Assert.notNull(energyPricePlan,"该能源价格方案不存在,无法编辑");
            updateById(epEnergyPricePlan);
        }
        //获取前端传过来的单元格
        List<Integer> pricePlanUnit = dto.getPricePlanType();
        //过滤单元格数据 结果为1,2,3的
        pricePlanUnit = pricePlanUnit.stream()
                .filter(s -> 3 == s || 2 == s || 1 == s).collect(Collectors.toList());
        //集合数量不等于48则报错
        if (pricePlanUnit.size()!=48){
            throw new BusinessException("时段设置有误,请重新设置");
        }
        //批量插入能源价格明细表的集合
       List<EpEnergyPricePlanDetail> planDetails = Lists.newArrayList();
        //峰谷平电
        int start = 0;
        int i = 0 ;
        for (i = 0; i < pricePlanUnit.size(); i++) {
            if (pricePlanUnit.get(i) != pricePlanUnit.get(start)) {
                //说明电的类型变化了
                //结算start - i 这一段区间 电的类型是arr[start]
                cal(pricePlanUnit.get(start),start,i,planDetails,dto.getId());
                start = i;
            }
        }
        //加上最后一个数据
        cal(pricePlanUnit.get(start),start,i,planDetails,dto.getId());
        //根据planid先lou出所有详情数据,编辑的时候需要先删除原来插入数据库数据再插入
        List<Integer> planDetailIds = energyPricePlanDetailService
                .list(new LambdaQueryWrapper<EpEnergyPricePlanDetail>()
                        .eq(EpEnergyPricePlanDetail::getPlanId, dto.getId()))
                .stream().map(EpEnergyPricePlanDetail::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(planDetailIds)){
            energyPricePlanDetailService.removeByIds(planDetailIds);
        }
        energyPricePlanDetailService.saveOrUpdateBatch(planDetails);
        return Boolean.TRUE;
    }

    @Override
    public List<EpEnergyPriceVO> getEpEnergyPriceVOList() {
        List<EpEnergyPricePlan> energyPricePlans = list();
        //设置返回数据集合对象
        List<EpEnergyPriceVO> epEnergyPriceVOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(energyPricePlans)){
            Set<Long> planIdsSet = energyPricePlans.stream().map(EpEnergyPricePlan::getId).collect(Collectors.toSet());
            Map<Long, List<Integer>> planDetails = energyPricePlanDetailService.getEpEnergyPricePlanDetailByPlanId(planIdsSet);
            energyPricePlans.forEach(s->{
                EpEnergyPriceVO epEnergyPriceVO = new EpEnergyPriceVO();
                BeanUtils.copyProperties(s,epEnergyPriceVO);
                if (CollUtil.isNotEmpty(planDetails)){
                    List<Integer> planType = planDetails.get(s.getId());
                    if (CollUtil.isNotEmpty(planType)){
                        epEnergyPriceVO.setPricePlanType(planType);
                    }
                }

                epEnergyPriceVOS.add(epEnergyPriceVO);
            });
        }
        return epEnergyPriceVOS;
    }

    @Override
    public EpEnergyPriceVO getEpEnergyPriceVO(Long id) {
        EpEnergyPricePlan energyPricePlan = getById(id);
        if (Objects.isNull(energyPricePlan)){
            return null;
        }
        EpEnergyPriceVO epEnergyPriceVO = new EpEnergyPriceVO();
        BeanUtils.copyProperties(energyPricePlan,epEnergyPriceVO);
        //根据能源价格方案id去查询能源价格方案详情
        Set<Long> planIds = new HashSet<>();
        planIds.add(energyPricePlan.getId());
        Map<Long, List<Integer>> planTypes = energyPricePlanDetailService.getEpEnergyPricePlanDetailByPlanId(planIds);
        if (CollUtil.isNotEmpty(planTypes)){
            epEnergyPriceVO.setPricePlanType(planTypes.get(energyPricePlan.getId()));
        }
        return epEnergyPriceVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        EpEnergyPricePlan energyPricePlan = getById(id);
        Assert.notNull(energyPricePlan,"该能源价格方案不存在或已经删除");
        List<Integer> planDetailIds = energyPricePlanDetailService.list(new LambdaQueryWrapper<EpEnergyPricePlanDetail>()
                        .eq(EpEnergyPricePlanDetail::getPlanId, id)).stream()
                .map(EpEnergyPricePlanDetail::getId).collect(Collectors.toList());
        //先删除明细再删除能源价格方案
        if (energyPricePlanDetailService.removeByIds(planDetailIds)&&removeById(energyPricePlan)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;


    }

//    public static void main(String[] args) {
//        int[] arr = new int[]{
//                1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
//                2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
//                2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
//                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1
//        };
//        //峰谷平电
//        int start = 0;
//        int i = 0 ;
//        for (i = 0; i < arr.length; i++) {
//            if (arr[i] != arr[start]) {
//                //说明电的类型变化了
//                //结算start - i 这一段区间 电的类型是arr[start]
//                cal(arr[start],start,i);
//                start = i;
//            }
//        }
//        //加上最后一个数据
//        cal(arr[start],start,i);
//    }

    private static void cal(Integer electricType ,Integer start , Integer end,List<EpEnergyPricePlanDetail> planDetails,Long planId){
        Integer startInteger = start * 30;
        Integer endInteger = end * 30;

        LocalTime startLdt = LocalTime.of(startInteger / 60, startInteger % 60);
        LocalTime endLdt;
        if (endInteger == 24 * 60) {
            endLdt = LocalTime.of(0, 0);
        } else {
            endLdt = LocalTime.of(endInteger / 60, endInteger % 60);
        }
//        log.info("类型{}:  {} - {}",electricType, startLdt, endLdt);
        EpEnergyPricePlanDetail epEnergyPricePlanDetail = new EpEnergyPricePlanDetail();
        epEnergyPricePlanDetail.setPlanId(planId);
        epEnergyPricePlanDetail.setType(electricType);
        epEnergyPricePlanDetail.setStart(startLdt);
        epEnergyPricePlanDetail.setEnd(endLdt);
        planDetails.add(epEnergyPricePlanDetail);
    }

}




