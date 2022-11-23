package com.steccn.qms.service.impl;

import cn.hutool.core.date.DatePattern;
import com.alibaba.nacos.common.utils.CollectionUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.feign.RemotePassRateFeignApi;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtChartTargetValue;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.service.QtChartTargetValueService;
import com.steccn.qms.vo.PassRateDateVo;
import com.steccn.qms.vo.PassRateOverViewVO;
import com.steccn.qms.vo.QtDefectChartVO;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.*;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
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

    private final RemotePassRateFeignApi remotePassRateFeignApi;

    private final QtCheckTaskService qtCheckTaskService;

    private final QtChartTargetValueService qtChartTargetValueService;


    @Override
    public PassRateOverViewVO passRateOverview(Integer type,LocalDateTime selectDateTime) {

        //最近7天完整工作日
        List<PassRateDateVo> passRateDateVoList = new ArrayList<>();
        List<LocalDate> dxData = new ArrayList<>();


        //获取横坐标和最近7天完整工作日
        getPassRateDateVoList(selectDateTime,dxData,passRateDateVoList);

        PassRateOverViewVO passRateOverViewVO = new PassRateOverViewVO();
        List<BigDecimal> datePassList = new ArrayList<>();
        //获取横坐标
        passRateOverViewVO.setXData(dxData);
        //获取最近7天和合格率
        passRateOverViewVO.setYData(datePassList);
        //初始化缺陷数量
        int badNum = 0;
        List<BigDecimal> badNumS = Lists.newArrayList();
        for (PassRateDateVo passRateDateVo : passRateDateVoList) {
            List<QtCheckTask> checkTaskList;
            List<QtCheckTask> qtCheckTaskList = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                    .ge(QtCheckTask::getCreateTime, passRateDateVo.getStartDateTime())
                    .le(QtCheckTask::getCreateTime, passRateDateVo.getEndDateTime()));
            if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)) {
                //获取来料检数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(qtCheckTask.getTaskType())).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
                //获取基片寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.SJGXJHG.getCode().equals(type)){
                //获取深加工寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)) {
                //获取基片缺陷总数
                List<String> defectJPList = Lists.newArrayList();
                defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ1.getWorkShopName());
                defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ2.getWorkShopName());
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
                badNum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getBadNum()) ? 0 : qtCheckTask.getBadNum()).sum();
                Integer checkNumSum2 = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getCheckNum()) ? 0 : qtCheckTask.getCheckNum()).sum();
                BigDecimal passRate = BigDecimal.ZERO;
                datePassList.add(passRate);
                badNumS.add(BigDecimal.valueOf(badNum));
            } else {
                //获取深加工缺陷总数
                List<String> defectSJGList = Lists.newArrayList();
                defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ1.getWorkShopName());
                defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ2.getWorkShopName());
                defectSJGList.add(QtManagerConstant.WORKSHOP.SJGCJ3.getWorkShopName());
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
                BigDecimal passRate = BigDecimal.ZERO;
                if (!checkTaskList.isEmpty()) {
                    badNum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getBadNum()) ? 0 : qtCheckTask.getBadNum()).sum();
                }
                datePassList.add(passRate);
                badNumS.add(BigDecimal.valueOf(badNum));
            }
            int goodNumSum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getGoodNum()) ? 0 : qtCheckTask.getGoodNum()).sum();
            Integer checkNumSum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getCheckNum()) ? 0 : qtCheckTask.getCheckNum()).sum();
            BigDecimal passRate;

            if (checkNumSum.equals(0)) {
                passRate = BigDecimal.ZERO;
            } else {
                passRate = BigDecimal.valueOf(goodNumSum).divide(BigDecimal.valueOf(checkNumSum),4,BigDecimal.ROUND_DOWN);
            }
            datePassList.add(passRate);
        }
        if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type) || QtManagerConstant.QtChatTargetValue.SJGQX.getCode().equals(type)){
            //获取合格率、日环比、周同比
            getPassRateOverViewVO(passRateOverViewVO, datePassList, type);
            passRateOverViewVO.setYData(badNumS);
        }else {
            //获取合格率、日环比、周同比
            getPassRateOverViewVO(passRateOverViewVO, datePassList, type);
        }

        QtChartTargetValue chartTargetValue = qtChartTargetValueService.getOne(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget, type));
        BigDecimal targetValue = BigDecimal.ZERO;
        if (Objects.nonNull(chartTargetValue)) {
            targetValue = chartTargetValue.getValue();
        }
        //获取目标值
        passRateOverViewVO.setTargetValue(targetValue);
        passRateOverViewVO.setKey(type);
        passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.getNameByCode(type));


        return passRateOverViewVO;
    }

    private void getPassRateOverViewVO(PassRateOverViewVO passRateOverViewVO, List<BigDecimal> datePassList, Integer type){
        if (CollectionUtils.isNotEmpty(datePassList)) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            //最后一天的合格率
            BigDecimal endDatePassRate = datePassList.get(datePassList.size() - 1);
            //获取第一天的合格率
            BigDecimal startDatePassRate = datePassList.get(0);
            //获取合格率
            passRateOverViewVO.setValue(decimalFormat.format(endDatePassRate));
            //获取日环比
            if (endDatePassRate.subtract(datePassList.get(datePassList.size() - 2)).compareTo(BigDecimal.ZERO) < 0) {
                passRateOverViewVO.setDayFlag(0);
                passRateOverViewVO.setDayValue(decimalFormat.format(datePassList.get(datePassList.size() - 2).subtract(endDatePassRate)));
            } else if (endDatePassRate.subtract(datePassList.get(datePassList.size() - 2)).compareTo(BigDecimal.ZERO) == 0) {
                passRateOverViewVO.setDayFlag(2);
            } else {
                passRateOverViewVO.setDayFlag(1);
                passRateOverViewVO.setDayValue(decimalFormat.format(endDatePassRate.subtract(datePassList.get(datePassList.size() - 2))));
            }
            //获取周同比
            BigDecimal weekValue = BigDecimal.ZERO;
            if (startDatePassRate.equals(BigDecimal.ZERO)) {
                passRateOverViewVO.setWeekFlag(2);
            } else {
                weekValue = endDatePassRate.subtract(startDatePassRate).divide(startDatePassRate,4,BigDecimal.ROUND_DOWN);
                if (weekValue.compareTo(BigDecimal.ZERO) < 0) {
                    weekValue = startDatePassRate.subtract(endDatePassRate).divide(startDatePassRate,4,BigDecimal.ROUND_DOWN);
                    passRateOverViewVO.setWeekFlag(0);
                } else if (weekValue.compareTo(BigDecimal.ZERO) == 0){
                    passRateOverViewVO.setWeekFlag(2);
                }else {
                    passRateOverViewVO.setWeekFlag(1);
                }
            }
            passRateOverViewVO.setWeekValue(decimalFormat.format(weekValue));

            String tooltip;
            LocalDate endDate = passRateOverViewVO.getXData().get(passRateOverViewVO.getXData().size() -1);
            if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)) {
                tooltip = String.format("%s所有来料检的平均合格率",LocalDateUtil.localDateFormat(passRateOverViewVO.getXData().get(passRateOverViewVO.getXData().size() -1), DatePattern.NORM_DATE_PATTERN));
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)){
                tooltip = String.format("%s白班、夜班所有%s巡检的平均合格率",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN),
                        QtManagerConstant.SUB_OFFICE_NAME);
            } else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)){
                tooltip = String.format("%s白班、夜班记录的基片车间缺陷数加总",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN));
            }else {
                tooltip = String.format("%s白班、夜班记录的深加工车间缺陷数加总",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN));
            }
            passRateOverViewVO.setTooltip(tooltip);
        }
    }

    private void getPassRateDateVoList(LocalDateTime selectDateTime, List<LocalDate> dxData, List<PassRateDateVo> passRateDateVoList) {
        LocalDate startLocalDate;
        LocalDate endLocalDate;
        LocalTime localTime = LocalTime.of(8,0,0);

        LocalDate selectDate = selectDateTime.toLocalDate();
        //获取最近七天开始日期和结束日期
        if (selectDateTime.getHour() < QtManagerConstant.beforeHour) {
            startLocalDate = selectDate.minusDays(8);
            endLocalDate = selectDate.minusDays(1);
            dxData.addAll(LocalDateUtil.getLocalDateByStartEndDate(selectDate.minusDays(8), selectDate.minusDays(2)));
        } else {
            startLocalDate = selectDate.minusDays(7);
            endLocalDate = selectDate;
            dxData.addAll(LocalDateUtil.getLocalDateByStartEndDate(selectDate.minusDays(7), selectDate.minusDays(1)));
        }

        //获取最近7天的完整工作时间
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startLocalDate, endLocalDate);
        while (!CollectionUtils.isEmpty(localDateList)) {
            PassRateDateVo rateDateVo = new PassRateDateVo();
            LocalDate endDate = localDateList.get(localDateList.size() - 1);
            LocalDate startDate = localDateList.get(localDateList.size() - 2);

            rateDateVo.setStartDateTime(LocalDateTime.of(startDate, localTime));
            rateDateVo.setEndDateTime(LocalDateTime.of(endDate, localTime));
            localDateList.remove(endDate);
            if (localDateList.size() == 1) {
                localDateList.remove(startDate);
            }
            passRateDateVoList.add(rateDateVo);
        }
        //日期升序排
        passRateDateVoList.sort(Comparator.comparing(PassRateDateVo::getStartDateTime));
    }


}

