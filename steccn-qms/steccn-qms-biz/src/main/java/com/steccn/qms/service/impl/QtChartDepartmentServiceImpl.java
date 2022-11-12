package com.steccn.qms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
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
import com.steccn.qms.vo.PassRateDateVo;
import com.steccn.qms.vo.PassRateOverViewVO;
import com.steccn.qms.vo.QtDefectChartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    public PassRateOverViewVO passRateOverview(Integer type) {
        LocalDate startLocalDate;
        LocalDate endLocalDate;
        LocalTime localTime = LocalTime.of(8,0,0);
        List<PassRateDateVo> passRateDateVoList = new ArrayList<>();

        //获取最近七天开始日期和结束日期
        if (LocalDateTime.now().getHour() < QtManagerConstant.beforeHour) {
            startLocalDate = LocalDate.now().minusDays(8);
            endLocalDate = LocalDate.now().minusDays(1);
        } else {
            startLocalDate = LocalDate.now().minusDays(7);
            endLocalDate = LocalDate.now();
        }

        //获取最近7天的完整工作时间
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startLocalDate, endLocalDate);
        while (!CollectionUtils.isEmpty(localDateList)) {
            PassRateDateVo rateDateVo = new PassRateDateVo();
            LocalDate endDate = localDateList.get(localDateList.size() - 1);
            LocalDate startDate = localDateList.get(localDateList.size() - 2);

            rateDateVo.setStartDateTime(LocalDateTime.of(startDate, localTime));
            rateDateVo.setEndDateTime(LocalDateTime.of(endDate, localTime));
            if (localDateList.size() == 1) {
                localDateList.remove(startDate);
            }
            localDateList.remove(endDate);
            passRateDateVoList.add(rateDateVo);
        }

        PassRateOverViewVO passRateOverViewVO = new PassRateOverViewVO();
        List<BigDecimal> datePassList = new ArrayList<>();
        //获取最近7天和合格率
        passRateOverViewVO.setDatePassList(datePassList);
        for (PassRateDateVo passRateDateVo : passRateDateVoList) {
            List<QtCheckTask> checkTaskList;
            List<QtCheckTask> qtCheckTaskList = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                    .ge(QtCheckTask::getCreateTime, passRateDateVo.getStartDateTime())
                    .le(QtCheckTask::getCreateTime, passRateDateVo.getEndDateTime()));
            if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)) {
                //获取来料检数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode())).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
                //获取基片寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                        && qtCheckTask.getMaterialWorkshopName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
            } else {
                //获取深加工寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                        && qtCheckTask.getMaterialWorkshopName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
            }

            Integer goodNumSum = checkTaskList.stream().mapToInt(QtCheckTask::getGoodNum).sum();
            Integer checkNumSum = checkTaskList.stream().mapToInt(QtCheckTask::getCheckNum).sum();
            BigDecimal passRate = new BigDecimal(goodNumSum / checkNumSum).setScale(4, BigDecimal.ROUND_DOWN);
            datePassList.add(passRate);
        }

        if (CollectionUtils.isEmpty(datePassList)) {
            //最后一天的合格率
            BigDecimal endDatePassRate = datePassList.get(0);
            //获取第一天的合格率
            BigDecimal startDatePassRate = datePassList.get(datePassList.size() - 1);
            //获取合格率
            passRateOverViewVO.setPassRate(endDatePassRate.toString());
            //获取日环比
            passRateOverViewVO.setDayValue(endDatePassRate.subtract(datePassList.get(1)).toString());
            //获取周同比
            passRateOverViewVO.setWeekValue(endDatePassRate.subtract(startDatePassRate).divide(startDatePassRate,4,BigDecimal.ROUND_DOWN).toString());
        }

        return passRateOverViewVO;
    }

}

