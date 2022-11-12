package com.steccn.qms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
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
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.vo.PassRateDateVo;
import com.steccn.qms.vo.PassRateOverViewVO;
import com.steccn.qms.vo.QtDefectChartVO;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.util.CollectorsUtil;
import com.steccn.qms.vo.*;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;
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

    /**
     * 深加工缺陷项分布图
     * @param dto
     * @return
     */
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

    /**
     * 深加工缺陷项分布图
     * @param
     * @return
     *//*
    @Override
    public QtChartDefectVO getPictureJPFB(QtChartParamDTO dto) {
        QtChartBarVO vo = new QtChartBarVO();
        List<String> xAis = new ArrayList<>();
        //折线图
        QtChartBarDecimalDataVO markLine = new QtChartBarDecimalDataVO();
        List<QtChartBarSeriesVO> series = new ArrayList<>();
        List<String> markLineDataList = new ArrayList<>();
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getDateStart());
        pmsDTO.setEndDate(dto.getDateEnd());
        R<List<ProducePassRateVO>> rResult = new R<>();

        //转时间戳
        LocalDateTime startTime = dto.getDateStart().atTime(0,0,1);
        LocalDateTime endTime = dto.getDateEnd().atTime(23, 59,59);
        //根据前端传输的查询条件查询深加工缺陷项类别分布

        MPJLambdaWrapper<QtCheckTask> condition = new MPJLambdaWrapper<QtCheckTask>()
                .selectAll(QtCheckTask.class)
                //关联
                .selectAs(QtDefectiveItem::getNameEn,QtCheckTask::getCreateName)
                .leftJoin(QtDefectiveItem.class, QtDefectiveItem::getCode, QtCheckTask::getCode)
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED)
                //完成时间范围
                .between(QtCheckTask::getFinishedTime,startTime,endTime)
                //班次
                .eq(dto.getWorkOrderId()!=null,QtCheckTask::getClassesId,dto.getWorkOrderId())
                //只要巡检
                .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //车间
                //只查询深加工一二三车间的数据
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                //按照缺陷项类别分组
                .groupBy(QtDefectiveItem::getCategoryId)
                .orderByAsc(QtCheckTask::getFinishedTime);
        QtDefectiveItemService qtDefectiveItemService;
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        QtDefectiveItemMapper qtDefectiveItemMapper;

        QtDefectiveItem qtDefectiveItem = new QtDefectiveItem();
        List<String> xAxisData = Lists.newArrayList();
        List<Integer> yAxisData = Lists.newArrayList();
        if (CollUtil.isNotEmpty(qtCheckTasks)){
            for (QtCheckTask qtCheckTask:qtCheckTasks){
                Integer qtCheckTaskBadNum = qtCheckTask.getBadNum();

                if (qtCheckTaskBadNum != null) {
                    yAxisData.add(qtCheckTaskBadNum);
                    xAxisData.add()
                }
            }
        }
        //根据日期进行分组
        Map<String,List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
        //遍历map
        for(Map.Entry<String,List<QtCheckTask>> entry:groupByDate.entrySet()){
            List<QtCheckTask> checkTaskDateList = entry.getValue();
            //再根据班组进行分组
            Map<Integer,List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
            //遍历班组分组
            for(Map.Entry<Integer,List<QtCheckTask>> entry2:groupByWorkOrder.entrySet()){
                List<QtCheckTask> checkTaskWorkOrderList = entry2.getValue();
                //查看是否是一种物料
                HashSet<Long> materialIdSet = new HashSet<>();
                checkTaskWorkOrderList.forEach(tmp->{
                    materialIdSet.add(tmp.getMaterialId());
                });
                Boolean flag = true;
                if(materialIdSet.size() > 0){
                    flag = false;
                }


        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);
        if(CollUtil.isNotEmpty(taskList)){
            for(QtCheckTask tmp:taskList){
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
                tmp.setDate(df.format(tmp.getFinishedTime()));
                tmp.setProductLineId(tmp.getProductLine().getId());
            }
            //根据日期进行分组
            Map<String,List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
            //遍历map
            for(Map.Entry<String,List<QtCheckTask>> entry:groupByDate.entrySet()){
                List<QtCheckTask> checkTaskDateList = entry.getValue();
                //再根据班组进行分组
                Map<Integer,List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
               )



        //调用数据生成全量数据表
        rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        if(CollUtil.isNotEmpty(rResult.getData())){
            //先过滤数据,只要基片车间
            List<ProducePassRateVO> rList = rResult.getData();
            //过滤基片车间
            rList = rList.stream().filter(s->s.getWorkshopName().contains("基片")).collect(Collectors.toList());
            //根据日期分组
            Map<LocalDate,List<ProducePassRateVO>> groupByDate = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate));
            //根据班次、产线、车间分bar类型
            Map<String,QtChartBarSeriesVO> barMap = MapUtil.newHashMap();
            //若按照班次分则只有两个
            if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
                QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                List<String> bar1Data = Lists.newArrayList();
                bar1.setData(bar1Data);
                bar1.setName(QtManagerConstant.morningWorkName);
                QtChartBarSeriesVO<String> bar2 = new QtChartBarSeriesVO<>();
                List<String> bar2Data = Lists.newArrayList();
                bar2.setData(bar2Data);
                bar2.setName(QtManagerConstant.nightWorkName);
                barMap.put(QtManagerConstant.morningWorkName,bar1);
                barMap.put(QtManagerConstant.nightWorkName,bar2);
            }else {
                SysOfficeDTO officeDTO = new SysOfficeDTO();
                officeDTO.setOfficeName(QtManagerConstant.SUB_OFFICE_NAME);
                if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                    //产线
                    officeDTO.setOfficeType(String.valueOf(9));
                }else {
                    //车间
                    officeDTO.setOfficeType(String.valueOf(8));
                }
                R<List<SysOfficeVO>> rSysOfficeResult = remoteOfficeFeign.getByTypeAndLine(officeDTO);
                if(CollUtil.isNotEmpty(rSysOfficeResult.getData())){
                    rSysOfficeResult.getData().forEach(tmp -> {
                        QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                        List<String> bar1Data = Lists.newArrayList();
                        bar1.setData(bar1Data);
                        bar1.setName(tmp.getOfficeName());
                        barMap.put(tmp.getOfficeName(),bar1);
                    });
                }
            }
            //先放入x轴的数据和y轴数据
            Long dateRange = dto.getDateEnd().toEpochDay() - dto.getDateStart().toEpochDay();
            for(int i = 0; i < dateRange; i++){
                LocalDate tmpDate = dto.getDateStart().plusDays(i);
                xAis.add(tmpDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                List<ProducePassRateVO> groupByDateVO = groupByDate.get(tmpDate);
                Map<String,BigDecimal> dataMap = MapUtil.newHashMap();
                //若VO不为空
                if(CollUtil.isNotEmpty(groupByDateVO)){
                    //对markLine进行求和
                    BigDecimal markLineData = groupByDateVO.stream().collect(CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate));
                    markLineDataList.add(new DecimalFormat("0.00%").format(markLineData));
                    //对比维度为班次
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getClassesName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    //对比维度为产线
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getProductLineName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    //对比维度为车间
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getWorkshopName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    for(Map.Entry<String,QtChartBarSeriesVO> entry:barMap.entrySet()){
                        if(dataMap.get(entry.getKey())==null){
                            entry.getValue().getData().add("0.00%");
                        }else {
                            entry.getValue().getData().add(new DecimalFormat("0.00%").format(dataMap.get(entry.getKey())));
                        }
                    }
                }
            }
            for(Map.Entry<String,QtChartBarSeriesVO> entry:barMap.entrySet()){
                series.add(entry.getValue());
            }
        }
        markLine.setData(markLineDataList);
        vo.setXAis(xAis);
        vo.setMarkLine(markLine);
        vo.setSeries(series);
        return vo;
    }
*/
    /**
     *  查询数据最晚完整日期
     */
    public LocalDateTime calculateOrder(LocalDateTime now){
        //计算最晚数据完整日期 在区间 (当前日期（T）的8:00:00, 次日（T+1）的8:00:00]内访问系统，最晚数据完整日期为T-1
        LocalDateTime classTimeStart = now.withHour(8).withMinute(0).withSecond(0);
        LocalDateTime classTimeEnd = now.withHour(8).withMinute(0).withSecond(0).plusDays(1);
        //最晚数据完整日期
        LocalDateTime fullDate = now;
        if(now.isAfter(classTimeStart) && now.isBefore(classTimeEnd)){
            fullDate = now.minusDays(1);
        }
        return fullDate;
    }

    @Override
    public PassRateOverViewVO passRateOverview(Integer type) {
        LocalDate startLocalDate;
        LocalDate endLocalDate;
        LocalTime localTime = LocalTime.of(8,0,0);
        List<PassRateDateVo> passRateDateVoList = new ArrayList<>();

        //获取开始日期和结束日期
        if (LocalDateTime.now().getHour() < QtManagerConstant.beforeHour) {
            startLocalDate = LocalDate.now().minusDays(8);
            endLocalDate = LocalDate.now().minusDays(1);
        } else {
            startLocalDate = LocalDate.now().minusDays(7);
            endLocalDate = LocalDate.now();
        }

        //获取每天的完整日期时间
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
        for (int i = 0; i < passRateDateVoList.size(); i++) {
            List<QtCheckTask> checkTaskList;
            List<QtCheckTask> qtCheckTaskList = qtCheckTaskMapper.selectList(new LambdaQueryWrapper<QtCheckTask>()
                    .ge(QtCheckTask::getCreateTime, passRateDateVoList.get(i).getStartDateTime())
                    .le(QtCheckTask::getCreateTime, passRateDateVoList.get(i).getEndDateTime()));
            if (type.equals(0)) {
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(1)).collect(Collectors.toList());
            } else if (type.equals(1)) {
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(4) && qtCheckTask.getMaterialWorkshopName().contains("基片")).collect(Collectors.toList());
            } else {
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(4) && qtCheckTask.getMaterialWorkshopName().contains("深加工")).collect(Collectors.toList());
            }

            Integer goodNumSum = checkTaskList.stream().mapToInt(QtCheckTask::getGoodNum).sum();
            Integer checkNumSum = checkTaskList.stream().mapToInt(QtCheckTask::getCheckNum).sum();
            BigDecimal passRate = new BigDecimal(goodNumSum/checkNumSum).setScale(4,BigDecimal.ROUND_DOWN);
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

