package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.vo.QtDefectChartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * @author 王志雄
 * 部门分析驾驶舱概览
 */
@Service
@RequiredArgsConstructor
public class QtChartDepartmentServiceImpl  implements QtChartDepartmentService {

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public QtDefectChartVO getChartDepartmentInfo(QtChatDepartmentDTO dto) {

        QtDefectChartVO qtDefectChartVO = new QtDefectChartVO();
        if (dto.getKeys().isEmpty()){
            throw new BusinessException("部门分析驾驶舱概览模块不能为空");
        }
        //基片缺陷模块
        if (dto.getKeys().contains(QtManagerConstant.QtChatTargetValue.JPQX.getCode())){
            List<String> defectJPList = Lists.newArrayList();
            //基片车间
            defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ1.getWorkShopName());
            defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ2.getWorkShopName());
            /*defectList.add(QtManagerConstant.WORKSHOP.SJGCJ1.getWorkShopName());
            defectList.add(QtManagerConstant.WORKSHOP.SJGCJ2.getWorkShopName());
            defectList.add(QtManagerConstant.WORKSHOP.SJGCJ3.getWorkShopName());*/
            //判断当前时间是否小于早上八点，若小于，则取前一天
            if (LocalDateTime.now().getHour() < QtManagerConstant.beforeHour || LocalDateTime.now().getHour() > QtManagerConstant.afterHour){
                LocalDateTime now = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
                List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                        .in(QtCheckTask::getProductWorkshopName, defectJPList)
                        .eq(QtCheckTask::getFinishedTime,now));
                int goodSum = 0;
                int badSum = 0;
                int sum = 0;
                if (!qtCheckTasks.isEmpty()){
                   for (QtCheckTask qtCheckTask:qtCheckTasks) {
                     if (qtCheckTask.getGoodNum()!=null){
                        goodSum += qtCheckTask.getGoodNum();
                     }
                     if (qtCheckTask.getBadNum()!=null){
                        badSum += qtCheckTask.getBadNum();
                     }
                     sum = goodSum + badSum;
                   }
                    //设置缺陷总数
                    qtDefectChartVO.setValue(String.valueOf(badSum));
                }
                if (goodSum != 0 && badSum != 0){
                    qtDefectChartVO.setDayValue(new BigDecimal(badSum/sum).setScale(2).toString());
                }
            }
            LocalDateTime last6Day = LocalDateTime.of(LocalDate.now().plusDays(6),LocalTime.MIN);
            List<LocalDate> localDateByStartEndDate = LocalDateUtil.getLocalDateByStartEndDate(LocalDate.now(), last6Day.toLocalDate());
            List<Object> xData = Lists.newArrayList();
            List<Integer> yData = Lists.newArrayList();
            for (LocalDate localdate: localDateByStartEndDate) {
                LocalDateTime dateTimeMin = LocalDateTime.of(localdate, LocalTime.MIN);
                LocalDateTime dateTimeMax = LocalDateTime.of(localdate, LocalTime.MAX);
                List<QtCheckTask> checkTasks = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                        .ge(QtCheckTask::getFinishedTime, dateTimeMin)
                        .le(QtCheckTask::getFinishedTime, dateTimeMax));
                int goodSum = 0;
                int badSum = 0;
                int sum = 0;
                if (!checkTasks.isEmpty()){
                    for (QtCheckTask qtCheckTask:checkTasks) {
                        if (qtCheckTask.getGoodNum()!=null){
                            goodSum += qtCheckTask.getGoodNum();
                        }
                        if (qtCheckTask.getBadNum()!=null){
                            badSum += qtCheckTask.getBadNum();
                        }
                        sum = goodSum + badSum;
                    }
                    }
                xData.add(localdate);
                yData.add(sum);
                }
            qtDefectChartVO.setXData(xData);
            qtDefectChartVO.setYData(yData);
            qtDefectChartVO.setTitle("基片缺陷总数");
            qtDefectChartVO.setKey(QtManagerConstant.QtChatTargetValue.JPQX.getCode());
            }
        return qtDefectChartVO;
    }

    @Override
    public QtDefectChartVO getChartDepartmentInfoSJG(QtChatDepartmentDTO dto) {

        QtDefectChartVO qtDefectChartVO = new QtDefectChartVO();
        if (dto.getKeys().isEmpty()){
            throw new BusinessException("部门分析驾驶舱概览模块入参不能为空");
        }
        //深加工缺陷模块
        if (dto.getKeys().contains(QtManagerConstant.QtChatTargetValue.SJGQX.getCode())){
            List<String> defectSJGList = Lists.newArrayList();
            defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ1.getWorkShopName());
            defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ2.getWorkShopName());
            defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ3.getWorkShopName());
            //判断当前时间是否小于早上八点，若小于，则取前一天
            if (LocalDateTime.now().getHour() < QtManagerConstant.beforeHour || LocalDateTime.now().getHour() > QtManagerConstant.afterHour){
                LocalDateTime now = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
                List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                        .in(QtCheckTask::getProductWorkshopName, defectSJGList)
                        .eq(QtCheckTask::getFinishedTime,now));
                int goodSum = 0;
                int badSum = 0;
                int sum = 0;
                if (!qtCheckTasks.isEmpty()){
                    for (QtCheckTask qtCheckTask:qtCheckTasks) {
                        if (qtCheckTask.getGoodNum()!=null){
                            goodSum += qtCheckTask.getGoodNum();
                        }
                        if (qtCheckTask.getBadNum()!=null){
                            badSum += qtCheckTask.getBadNum();
                        }
                        sum = goodSum + badSum;
                    }
                    //设置缺陷总数
                    qtDefectChartVO.setValue(String.valueOf(badSum));
                }
                if (goodSum != 0 && badSum != 0){
                    qtDefectChartVO.setDayValue(new BigDecimal(badSum/sum).setScale(2).toString());
                }
            }
            LocalDateTime last6Day = LocalDateTime.of(LocalDate.now().plusDays(6),LocalTime.MIN);
            List<LocalDate> localDateByStartEndDate = LocalDateUtil.getLocalDateByStartEndDate(LocalDate.now(), last6Day.toLocalDate());
            List<Object> xData = Lists.newArrayList();
            List<Integer> yData = Lists.newArrayList();
            for (LocalDate localdate: localDateByStartEndDate) {
                LocalDateTime dateTimeMin = LocalDateTime.of(localdate, LocalTime.MIN);
                LocalDateTime dateTimeMax = LocalDateTime.of(localdate, LocalTime.MAX);
                List<QtCheckTask> checkTasks = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                        .ge(QtCheckTask::getFinishedTime, dateTimeMin)
                        .le(QtCheckTask::getFinishedTime, dateTimeMax));
                int goodSum = 0;
                int badSum = 0;
                int sum = 0;
                if (!checkTasks.isEmpty()){
                    for (QtCheckTask qtCheckTask:checkTasks) {
                        if (qtCheckTask.getGoodNum()!=null){
                            goodSum += qtCheckTask.getGoodNum();
                        }
                        if (qtCheckTask.getBadNum()!=null){
                            badSum += qtCheckTask.getBadNum();
                        }
                        sum = goodSum + badSum;
                    }
                }
                xData.add(localdate);
                yData.add(sum);
            }
            qtDefectChartVO.setXData(xData);
            qtDefectChartVO.setYData(yData);
            qtDefectChartVO.setTitle("深加工缺陷总数");
            qtDefectChartVO.setKey(QtManagerConstant.QtChatTargetValue.SJGQX.getCode());
        }
        return qtDefectChartVO;
    }

}

