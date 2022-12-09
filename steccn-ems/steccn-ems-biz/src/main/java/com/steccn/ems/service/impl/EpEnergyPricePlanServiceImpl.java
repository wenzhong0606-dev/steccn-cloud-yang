package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyPricePlan;
import com.steccn.ems.domain.EpEnergyPricePlanDetail;
import com.steccn.ems.dto.EpEnergyPriceDTO;
import com.steccn.ems.service.EpEnergyPricePlanDetailService;
import com.steccn.ems.service.EpEnergyPricePlanService;
import com.steccn.ems.mapper.EpEnergyPricePlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

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
        //创建domain实体类对象
        EpEnergyPricePlan epEnergyPricePlan = new EpEnergyPricePlan();
        BeanUtils.copyProperties(dto,epEnergyPricePlan);
        if (Objects.isNull(dto.getId())){
            //新建的时候设置价格方案名称
            long sum = count();
            epEnergyPricePlan.setName("价格方案"+sum+1);
        }
        //获取前端传过来的单元格
        List<Integer> pricePlanUnit = dto.getPricePlanUnit();
        //获取时间分片
        List<LocalTime> timeList = getTimeList("H");
        if (pricePlanUnit.size()!=timeList.size()){
            throw new BusinessException("时段设置有误,请重新设置");
        }
        //遍历单元格设置的时间,把它储存到map中一一对应
        Map<LocalTime,Integer> pricePlanMap = Maps.newHashMap();
        for (int i = 0; i < pricePlanUnit.size(); i++) {
            pricePlanMap.put(timeList.get(i),pricePlanUnit.get(i));
        }
        //能源价格为峰时的时间片集合
        List<LocalTime> typeTime1 = new ArrayList<>();
        //能源价格为平时的时间片集合
        List<LocalTime> typeTime2 = new ArrayList<>();
        //能源价格为谷时的时间片集合
        List<LocalTime> typeTime3 = new ArrayList<>();
        //遍历map
        for (Map.Entry<LocalTime, Integer> timeIntegerEntry : pricePlanMap.entrySet()) {
            if ( EpManagerConstant.EnergyPriceType.PEAK_PERIOD.getCode()==timeIntegerEntry.getValue()){
                typeTime1.add(timeIntegerEntry.getKey());
            }else if (EpManagerConstant.EnergyPriceType.USUALLY_PERIOD.getCode()==timeIntegerEntry.getValue()){
                typeTime2.add(timeIntegerEntry.getKey());
            }else if (EpManagerConstant.EnergyPriceType.VALLEY_PERIOD.getCode()==timeIntegerEntry.getValue()){
                typeTime3.add(timeIntegerEntry.getKey());
            }
        }

        //批量插入能源价格明细表的集合
        List<EpEnergyPricePlanDetail> planDetails = Lists.newArrayList();
        //遍历时间片集合设置价格明细表中的开始时间,结束时间
        if (CollUtil.isNotEmpty(typeTime1)){
            LocalTime start = LocalTime.of(0,0,0);
            for (LocalTime time : typeTime1) {
                EpEnergyPricePlanDetail planDetail = new EpEnergyPricePlanDetail();
               //todo
            }
        }

        return null;
    }


    /**
     * 按半小时获取一天的时间分片
     * @param dateType
     * @return
     */
    public static List<LocalTime> getTimeList( String dateType){
        LocalTime start = LocalTime.of(0,0,0);
        LocalTime end = LocalTime.of(23,30,0);
      List<LocalTime> timeList = new ArrayList<>();

      while (end.isAfter(start)){
          switch (dateType){
              case "H":
                  timeList.add(start);
                  start=start.plusMinutes(30);
                  if (!end.isAfter(start)){
                      timeList.add(end);
                  }
                  break;
          }
      }
      return timeList;
    }

//    public static void main(String[] args) {
//        System.out.println(getTimeList("H"));
//        System.out.println(getTimeList("H").size());
//    }

}




