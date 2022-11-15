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
import com.steccn.qms.util.CollectorsUtil;
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

/*    @Override
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
            *//*defectList.add(QtManagerConstant.WORKSHOP.SJGCJ1.getWorkShopName());
            defectList.add(QtManagerConstant.WORKSHOP.SJGCJ2.getWorkShopName());
            defectList.add(QtManagerConstant.WORKSHOP.SJGCJ3.getWorkShopName());*//*
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

    *//**
     * 深加工缺陷项分布图
     * @param dto
     * @return
     *//*
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
    }*/

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
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode())).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
                //获取基片寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.SUB_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.SJGXJHG.getCode().equals(type)){
                //获取深加工寻检查数据
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                        && Objects.nonNull(qtCheckTask.getProductWorkshop())
                        && StringUtils.isNotBlank(qtCheckTask.getProductWorkshop().getName())
                        && qtCheckTask.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)).collect(Collectors.toList());
            } else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)) {
                //获取基片缺陷总数
                List<String> defectJPList = Lists.newArrayList();
                defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ1.getWorkShopName());
                defectJPList.add(QtManagerConstant.WORKSHOP.JPCJ2.getWorkShopName());
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> defectJPList.contains(qtCheckTask.getProductWorkshop().getName())).collect(Collectors.toList());
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
                checkTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> defectSJGList.contains(qtCheckTask.getProductWorkshop().getName())).collect(Collectors.toList());
                BigDecimal passRate = BigDecimal.ZERO;
                badNum = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getBadNum()) ? 0 : qtCheckTask.getBadNum()).sum();
                Integer checkNumSum2 = checkTaskList.stream().mapToInt(qtCheckTask -> Objects.isNull(qtCheckTask.getCheckNum()) ? 0 : qtCheckTask.getCheckNum()).sum();
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
        if (qtChartTargetValueService.getOne(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget, type)) != null) {
            BigDecimal targetValue = qtChartTargetValueService.getOne(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget, type)).getValue();
            if (targetValue == null) {
                targetValue = BigDecimal.ZERO;
            }
            //获取目标值
            passRateOverViewVO.setTargetValue(targetValue);
            passRateOverViewVO.setKey(type);
        }
        if (QtManagerConstant.QtChatTargetValue.JPXJHG.getCode().equals(type)) {
            passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.JPXJHG.getName());
        } else if (QtManagerConstant.QtChatTargetValue.LLJHG.getCode().equals(type)){
            passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.LLJHG.getName());
        } else if (QtManagerConstant.QtChatTargetValue.SJGXJHG.getCode().equals(type)){
            passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.SJGXJHG.getName());
        }else if (QtManagerConstant.QtChatTargetValue.JPQX.getCode().equals(type)){
            passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.JPQX.getName());
        }else if (QtManagerConstant.QtChatTargetValue.SJGQX.getCode().equals(type)){
            passRateOverViewVO.setTitle(QtManagerConstant.QtChatTargetValue.SJGQX.getName());
        }

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

