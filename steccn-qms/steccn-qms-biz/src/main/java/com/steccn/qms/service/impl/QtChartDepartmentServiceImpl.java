package com.steccn.qms.service.impl;

import cn.hutool.core.date.DatePattern;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.pms.feign.RemotePassRateFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtChartTargetValue;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.service.QtChartTargetValueService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.PassRateDateVo;
import com.steccn.qms.vo.PassRateOverViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 王志雄
 * 部门分析驾驶舱概览
 */
@Service
@RequiredArgsConstructor
public class QtChartDepartmentServiceImpl implements QtChartDepartmentService {

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemotePassRateFeignApi remotePassRateFeignApi;

    private final QtCheckTaskService qtCheckTaskService;

    private final QtChartTargetValueService qtChartTargetValueService;


    @Override
    public PassRateOverViewVO passRateOverview(Integer type, LocalDate selectDate) {

        //最近7天完整工作日
        List<PassRateDateVo> passRateDateVoList = new ArrayList<>();
        List<LocalDate> dxData = new ArrayList<>();

        //获取横坐标和最近7天完整工作日
        getPassRateDateVoList(selectDate, dxData, passRateDateVoList);

        PassRateOverViewVO passRateOverViewVO = new PassRateOverViewVO();
        List<BigDecimal> datePassList = new ArrayList<>();
        //获取横坐标
        passRateOverViewVO.setXData(dxData);
        //获取最近7天和合格率
        passRateOverViewVO.setYData(datePassList);

        List<QtCheckTask> qtCheckTaskList = new ArrayList<>();
        if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type) || QtManagerConstant.QtChatTargetValue.SJGQX.getCode().equals(type)) {
            qtCheckTaskList = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                    .ge(QtCheckTask::getDefectStartTime, passRateDateVoList.get(0).getStartDateTime())
                    .le(QtCheckTask::getDefectEndTime, passRateDateVoList.get(passRateDateVoList.size() - 1).getEndDateTime()).eq(QtCheckTask::getStatus, 2));
        } else {
            qtCheckTaskList = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                    .ge(QtCheckTask::getFinishedTime, passRateDateVoList.get(0).getStartDateTime())
                    .le(QtCheckTask::getFinishedTime, passRateDateVoList.get(passRateDateVoList.size() - 1).getEndDateTime()).eq(QtCheckTask::getStatus, 2));
        }

        for (PassRateDateVo passRateDateVo : passRateDateVoList) {
            List<QtCheckTask> checkTaskList = new ArrayList<>();
            if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)) {
                //获取来料检数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(qtCheckTask.getTaskType())
                        && (qtCheckTask.getFinishedTime().isAfter(passRateDateVo.getStartDateTime()) || qtCheckTask.getFinishedTime().isEqual(passRateDateVo.getStartDateTime()))
                        && qtCheckTask.getFinishedTime().isBefore(passRateDateVo.getEndDateTime())).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
                //获取基片寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && (qtCheckTask.getFinishedTime().isAfter(passRateDateVo.getStartDateTime()) || qtCheckTask.getFinishedTime().isEqual(passRateDateVo.getStartDateTime()))
                        && qtCheckTask.getFinishedTime().isBefore(passRateDateVo.getEndDateTime())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.SJGXJHG.getCode().equals(type)) {
                //获取深加工寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(qtCheckTask.getTaskType())
                        && (qtCheckTask.getFinishedTime().isAfter(passRateDateVo.getStartDateTime()) || qtCheckTask.getFinishedTime().isEqual(passRateDateVo.getStartDateTime()))
                        && qtCheckTask.getFinishedTime().isBefore(passRateDateVo.getEndDateTime())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)) {
                //获取基片缺陷总数
                BigDecimal defectJp = getDefectJp(qtCheckTaskList, passRateDateVo);
                datePassList.add(defectJp);
                continue;
            } else {
                //获取深加工缺陷总数
                BigDecimal defectSjg = getDefectSjg(qtCheckTaskList, passRateDateVo);
                datePassList.add(defectSjg);
                continue;
            }
            int goodNumSum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getGoodNum()) ? 0 : qtCheckTask.getGoodNum()).sum();
            Integer checkNumSum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getCheckNum()) ? 0 : qtCheckTask.getCheckNum()).sum();
            BigDecimal passRate;

            if (checkNumSum.equals(0)) {
                passRate = BigDecimal.ZERO;
            } else {
                passRate = BigDecimal.valueOf(goodNumSum).divide(BigDecimal.valueOf(checkNumSum), 4, BigDecimal.ROUND_DOWN);
            }
            datePassList.add(passRate);
        }
        //获取合格率、日环比、周同比
        getPassRateOverViewVO(passRateOverViewVO, datePassList, type);

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

    private BigDecimal getDefectJp(List<QtCheckTask> qtCheckTaskList, PassRateDateVo passRateDateVo) {
        int badNum = 0;
        List<QtCheckTask> checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode().equals(qtCheckTask.getTaskType())
                && (qtCheckTask.getDefectStartTime().isAfter(passRateDateVo.getStartDateTime()) || passRateDateVo.getStartDateTime().isEqual(passRateDateVo.getStartDateTime()))
                && qtCheckTask.getDefectEndTime().isBefore(passRateDateVo.getEndDateTime())
                && Objects.nonNull(qtCheckTask.getProductWorkshop())
                && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
        badNum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getDefectNum()) ? 0 : qtCheckTask.getDefectNum()).sum();
        return BigDecimal.valueOf(badNum);
    }

    private BigDecimal getDefectSjg(List<QtCheckTask> qtCheckTaskList, PassRateDateVo passRateDateVo) {
        //初始化缺陷数量
        int badNum = 0;
        List<QtCheckTask> checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode().equals(qtCheckTask.getTaskType())
                && (qtCheckTask.getDefectStartTime().isAfter(passRateDateVo.getStartDateTime()) || passRateDateVo.getStartDateTime().isEqual(passRateDateVo.getStartDateTime()))
                && qtCheckTask.getDefectEndTime().isBefore(passRateDateVo.getEndDateTime())
                && Objects.nonNull(qtCheckTask.getProductWorkshop())
                && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
        if (!checkTaskList.isEmpty()) {
            badNum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getDefectNum()) ? 0 : qtCheckTask.getDefectNum()).sum();
        }
        return BigDecimal.valueOf(badNum);
    }

    private void getPassRateOverViewVO(PassRateOverViewVO passRateOverViewVO, List<BigDecimal> datePassList, Integer type) {
        if (CollectionUtils.isNotEmpty(datePassList)) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            //最后一天的合格率
            BigDecimal endDatePassRate = datePassList.get(datePassList.size() - 1);
            //获取第一天的合格率
            BigDecimal startDatePassRate = datePassList.get(0);

            //获取基片缺陷总数、深加工缺陷总数的日环比和周同比
            if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type) || QtManagerConstant.QtChatTargetValue.SJGQX.getCode().equals(type)) {
                //获取合格率
                passRateOverViewVO.setValue(endDatePassRate.toString());
                BigDecimal value = endDatePassRate.subtract(datePassList.get(datePassList.size() - 2));
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    passRateOverViewVO.setDayFlag(0);
                    value = datePassList.get(datePassList.size() - 2).subtract(endDatePassRate);
                } else if (value.compareTo(BigDecimal.ZERO) > 0) {
                    passRateOverViewVO.setDayFlag(1);
                    value = endDatePassRate.subtract(datePassList.get(datePassList.size() - 2));
                } else {
                    passRateOverViewVO.setDayFlag(2);
                }
                //获取日环比
                if (datePassList.get(datePassList.size() - 2).equals(BigDecimal.ZERO)) {
                    passRateOverViewVO.setDayValue("-");
                } else {
                    BigDecimal dayValue = BigDecimalUtil.divide(value, datePassList.get(datePassList.size() - 2), 4);
                    passRateOverViewVO.setDayValue(decimalFormat.format(dayValue));
                }
            } else {
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
            }
            //获取周同比
            BigDecimal weekValue = BigDecimal.ZERO;
            if (startDatePassRate.equals(BigDecimal.ZERO)) {
                passRateOverViewVO.setWeekFlag(2);
                passRateOverViewVO.setWeekValue("-");
            } else {
                BigDecimal subtract = BigDecimalUtil.subtract(endDatePassRate, startDatePassRate);
                weekValue = BigDecimalUtil.divide(subtract, startDatePassRate, 4);
                if (weekValue.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal subtract1 = BigDecimalUtil.subtract(startDatePassRate, endDatePassRate);
                    weekValue = BigDecimalUtil.divide(subtract1, startDatePassRate, 4);
                    passRateOverViewVO.setWeekFlag(0);
                } else if (weekValue.compareTo(BigDecimal.ZERO) == 0) {
                    passRateOverViewVO.setWeekFlag(2);
                } else {
                    passRateOverViewVO.setWeekFlag(1);
                }
            }
            passRateOverViewVO.setWeekValue(decimalFormat.format(weekValue));

            String tooltip;
            LocalDate endDate = passRateOverViewVO.getXData().get(passRateOverViewVO.getXData().size() - 1);
            if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)) {
                tooltip = String.format("%s所有来料检的平均合格率", LocalDateUtil.localDateFormat(passRateOverViewVO.getXData().get(passRateOverViewVO.getXData().size() - 1), DatePattern.NORM_DATE_PATTERN));
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
                tooltip = String.format("%s白班、夜班所有%s巡检的平均合格率",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN),
                        QtManagerConstant.SUB_OFFICE_NAME);
            } else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)) {
                tooltip = String.format("%s白班、夜班记录的基片车间缺陷数加总",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN));
            } else {
                tooltip = String.format("%s白班、夜班记录的深加工车间缺陷数加总",
                        LocalDateUtil.localDateFormat(endDate, DatePattern.NORM_DATE_PATTERN));
            }
            passRateOverViewVO.setTooltip(tooltip);
        }
    }

    private void getPassRateDateVoList(LocalDate selectDate, List<LocalDate> dxData, List<PassRateDateVo> passRateDateVoList) {
        LocalDate startLocalDate;
        LocalDate endLocalDate;
        LocalTime localTime = LocalTime.of(8, 0, 0);

        endLocalDate = selectDate.plusDays(1);
        startLocalDate = selectDate.minusDays(6);
        dxData.addAll(LocalDateUtil.getLocalDateByStartEndDate(startLocalDate, selectDate));

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

