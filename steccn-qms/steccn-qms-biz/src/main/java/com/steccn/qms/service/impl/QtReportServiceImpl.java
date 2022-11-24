package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.metadata.data.ClientAnchorData;
import com.alibaba.excel.util.FileTypeUtils;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.vo.MaterialRequisitionPlanVO;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.util.*;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 86178
 * @description 针对表【qt_report】的数据库操作Service实现
 * @createDate 2022-11-01 16:42:01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtReportServiceImpl extends ServiceImpl<QtReportMapper, QtReport>
        implements QtReportService {
    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;
    private final QtCheckPlanGroupService qtCheckPlanGroupService;
    private final QtCheckPlanGroupItemService qtCheckPlanGroupItemService;
    private final QtCheckItemService qtCheckItemService;
    private final QtCheckTaskService qtCheckTaskService;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final QtMaterialService qtMaterialService;
    private final RemoteOfficeFeign remoteOfficeFeign;

    /**
     * 新建报表卡片
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(QtReportDTO dto) {
        List<QtReport> qtReports = list(new LambdaQueryWrapper<QtReport>()
                .eq(QtReport::getName, dto.getName())
                .eq(QtReport::getReportGroupId, dto.getReportGroupId()));
        if (qtReports.size()>0){
           throw new BusinessException("该报表卡片已存在,请勿重复创建!");
       }
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto, qtReport);
        qtReport.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(dto.getTimeScopeStart()));
        LocalDate timeScopeEnd = dto.getTimeScopeEnd();
        qtReport.setTimeScopeEnd(LocalDateTime.of(timeScopeEnd.getYear(),timeScopeEnd.getMonth()
                ,timeScopeEnd.getDayOfMonth(),23,59,59));
        //根据分组id去查询分组
        this.save(qtReport);
        return qtReport.getId();
    }

    /**
     * 根据物料id以及检验类型选择检验分组
     *
     * @param materialId
     * @param checkType
     * @return
     */
    @Override
    public List<QtCheckGroupVO> getGroupIdList(Long materialId, Integer checkType) {
        if (Objects.isNull(materialId) && Objects.isNull(checkType)) {
            throw new BusinessException("请先选择物料名称以及检验类型");
        }
        List<QtMaterialLinkCheckPlan> materialLinkCheckPlans = qtMaterialLinkCheckPlanService.list(new LambdaQueryWrapper<QtMaterialLinkCheckPlan>()
                .eq(QtMaterialLinkCheckPlan::getMaterialId, materialId)
                .eq(QtMaterialLinkCheckPlan::getCheckTypeCode, checkType));
        //获取检验方案id
        List<Integer> collectCheckPlanId = materialLinkCheckPlans.stream().map(QtMaterialLinkCheckPlan::getCheckPlanId).collect(Collectors.toList());
        if (CollUtil.isEmpty(collectCheckPlanId)) {
            throw new BusinessException("请先在物料配置中为物料配置#选中检验类型#的检验方案");
        }
        //创建检验方案分组id集合
        List<QtCheckGroupVO> qtCheckGroupVOS = Lists.newArrayList();
        //遍历检验方案id,去检验方案分组表查询获取分组id并添加进分组id集合
        List<QtCheckPlanGroup> qtCheckPlanGroups = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>().in(QtCheckPlanGroup::getPlanId, collectCheckPlanId));
        qtCheckPlanGroups.forEach(q -> {
            QtCheckGroupVO qtCheckGroupVO = new QtCheckGroupVO();
            BeanUtils.copyProperties(q, qtCheckGroupVO);
            qtCheckGroupVOS.add(qtCheckGroupVO);
        });
        return qtCheckGroupVOS;
    }

    /**
     * 根据检验分组id选择检验项
     *
     * @param groupid
     * @return
     */
    @Override
    public List<QtCheckItemSPCVO> getQtCheckItemList(Integer groupid) {
        if (Objects.isNull(groupid)){
            throw new BusinessException("分组id不能为空");
        }
        //获取检验项id集合
        List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>().
                eq(QtCheckPlanGroupItem::getGroupId, groupid));
        //取得检验项id的集合
        List<Integer> checkItemIds = qtCheckPlanGroupItems.stream().map(QtCheckPlanGroupItem::getCheckItemId)
                .filter(s->Objects.nonNull(s)).collect(Collectors.toList());
        //创建返回检验项的集合
        List<QtCheckItemSPCVO> qtCheckItemSPCVOS = Lists.newArrayList();
        if (CollUtil.isEmpty(checkItemIds)) {
            throw new BusinessException("该检验分组下没有检验项,请先配置");
        }
        List<QtCheckItem> qtCheckItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().in(QtCheckItem::getId, checkItemIds));
        //添加检验项的id或名称
        qtCheckItems.forEach(q -> {
            QtCheckItemSPCVO qtCheckItemSPCVO = new QtCheckItemSPCVO();
            qtCheckItemSPCVO.setCheckItemId(q.getId());
            qtCheckItemSPCVO.setCheckItemName(q.getName());
            qtCheckItemSPCVOS.add(qtCheckItemSPCVO);
        });
        //去重
        List<QtCheckItemSPCVO> qtCheckItemSPCVOList = qtCheckItemSPCVOS.stream().distinct().collect(Collectors.toList());
        return qtCheckItemSPCVOList;
    }

    /**
     * 根据检验项的id返回前端标准值、上偏差、下偏差,单位
     *
     * @param groupId
     * @param itemId
     * @return
     */
    @Override
    public QtCheckItemUnitVO getQtCheckItemUnitVO(Integer groupId, Integer itemId) {
        //创建返回数据的对象
        QtCheckItemUnitVO qtCheckItemUnitVO = new QtCheckItemUnitVO();
        if (Objects.isNull(groupId) || Objects.isNull(itemId)) {
            //有一个条件不满足返回给前端空对象
            return qtCheckItemUnitVO;
        }
        //根据检验项id以及分组id去check_plan_group_item数据库中查询检验项分组对象
        QtCheckPlanGroupItem qtCheckPlanGroupItem = qtCheckPlanGroupItemService
                .getOne(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                .eq(QtCheckPlanGroupItem::getCheckItemId, itemId)
                .eq(QtCheckPlanGroupItem::getGroupId, groupId));
        //根据id查询检验项
        QtCheckItem qtCheckItem = qtCheckItemService.getById(itemId);
        Set<Long> measureUnitSet = new HashSet<>();
        if (Objects.nonNull(qtCheckItem)&&Objects.nonNull(qtCheckItem.getMeasureUnit())){
            measureUnitSet.add(qtCheckItem.getMeasureUnit());
            Map<Long, String> dictData = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureUnitSet)).get();
            if (Objects.nonNull(dictData)){
                String measureName = dictData.get(qtCheckItem.getMeasureUnit());
                qtCheckItemUnitVO.setMeasureUnitName(measureName);
            }
        }
        if (Objects.isNull(qtCheckPlanGroupItem)) {
            return qtCheckItemUnitVO;
        } else {
            BeanUtils.copyProperties(qtCheckPlanGroupItem, qtCheckItemUnitVO);
            return qtCheckItemUnitVO;
        }
    }

    /**
     * 下载excel Response封装
     * @param response
     * @return
     */
    public void setDownloadHeader(HttpServletResponse response,String name) throws UnsupportedEncodingException {
        response.setContentType(QtManagerConstant.QT_CHART_EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode(name, QtManagerConstant.QT_CHART_EXCEL_ENCODE).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    /**
     * 查询改分组下的所有报表卡片
     *
     * @param reportGroupId
     * @return
     */
    @Override
    public List<QtReportChartVO> getQtReportList(Integer reportGroupId) {
        List<QtReportChartVO> qtReportChartVOlist=Lists.newArrayList();
        if (Objects.isNull(reportGroupId)){
            return qtReportChartVOlist;
        }

        List<QtReport> qtReportList = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, reportGroupId)
                .orderByDesc(QtReport::getId));
        //List<Long> materialIds= qtReportList.stream().map(QtReport::getMaterialId).collect(Collectors.toList());
        if (CollUtil.isEmpty(qtReportList)) {
            return qtReportChartVOlist;
        }

        //遍历查出来的集合封装数据`
        qtReportList.forEach(q -> {
            //返回报表数据
            QtReportChartVO qtReportChartVO = new QtReportChartVO();
            BeanUtils.copyProperties(q, qtReportChartVO);
            qtReportChartVO.setReportCardType(q.getReportCardType());
            qtReportChartVO.setId(q.getId());
            qtReportChartVO.setMaterialId(q.getMaterialId());

            //根据物料id查询物料
            if (Objects.nonNull(q.getMaterialId())) {
                QtMaterial material = qtMaterialService.getById(q.getMaterialId());
                //设置物料名称
                if (Objects.nonNull(material)) {
                    qtReportChartVO.setMaterialName(material.getName());
                }
            }
            //该检验任务的包括快照的id集合
            List<Integer> itemIds = Lists.newArrayList();
            if (Objects.nonNull(q.getCheckItemId())) {
                QtCheckItem qtCheckItem  = qtCheckItemService.getById(q.getCheckItemId());
                if (Objects.nonNull(qtCheckItem)) {
                    if (Objects.nonNull(qtCheckItem.getName())) {
                        qtReportChartVO.setCheckItemName(qtCheckItem.getName());
                    }
                    List<Integer> checkIds = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                                    .eq(QtCheckItem::getCode, qtCheckItem.getCode()))
                            .stream().map(QtCheckItem::getId).collect(Collectors.toList());
                    //添加快照ids
                    if (CollUtil.isNotEmpty(checkIds)){
                        checkIds.forEach(s->{
                            itemIds.add(s);
                        });
                    }
                    //设置检验项的数据类型
                    Long dataType = qtCheckItem.getDataType();
                    qtReportChartVO.setDataTypeId(dataType);
                    List<Long> ids = Lists.newArrayList();
                    ids.add(dataType);
                    Map<Long, SysDictVO> longSysDictVOMap = FeignOptional.of(remoteDictFeignApi.listById(ids)).get();
                    SysDictVO sysDictVO = longSysDictVOMap.get(dataType);
                    if (Objects.nonNull(sysDictVO.getName())) {
                        qtReportChartVO.setDataTypeName(sysDictVO.getName());
                    }

                    if (Objects.nonNull(qtCheckItem.getMeasureUnit())) {
                        Set<Long> measureIds = Sets.newHashSet();
                        measureIds.add(qtCheckItem.getMeasureUnit());
                        Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureIds)).get();
                        String measureName = longStringMap.get(qtCheckItem.getMeasureUnit());
                        if (Objects.nonNull(measureName)) {
                            qtReportChartVO.setMeasureUnit(measureName);

                        }
                    }
                    if (QtManagerConstant.QtChartUnit.PERCENT.getCode().equals(dataType)) {
                        qtReportChartVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
                    }
                }
            }

            //根据条件查出所有的任务
            List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                            .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                            .eq(QtCheckTask::getTaskType, q.getCheckType())
                            .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                            .ge(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                            .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
                    //.and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),q.getCustomer())
            );

            //设置开始结束时间
            qtReportChartVO.setTimeScopeStart(q.getTimeScopeStart().toLocalDate());
            qtReportChartVO.setTimeScopeEnd(q.getTimeScopeEnd().toLocalDate());
            //设置时间分片
            List<String> dates = Lists.newArrayList();
            QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(q.getGranularity());
            if (Objects.isNull(granularityEnum)){
                throw new BusinessException("粒度不存在");
            }
            //将时间范围根据粒度分片
            List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                    , q.getTimeScopeStart()
                    , q.getTimeScopeEnd());
            //设置折线图数据
            List<String> itemValue = new ArrayList<>();
            for (int i = 1; i < localDateTimes.size(); i++) {
                LocalDateTime start = localDateTimes.get(i - 1);
                LocalDateTime end = localDateTimes.get(i);
                //过滤出时间分片内所有检验任务的数据
                List<QtCheckTask> conditionTasks = qtCheckTasks.stream().filter(v -> Objects.nonNull(v.getFinishedTime())).filter(v -> {
                    //数据范围应该是大于等于开始时间  小于结束时间
                    if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }).collect(Collectors.toList());

                //如果检验任务为空则该时间段下检验结果为0
                if (CollUtil.isEmpty(conditionTasks)){
                    itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
                }else {
                    List<BigDecimal> bigDecimals = Lists.newArrayList();
                    conditionTasks.forEach(r->{
                        List<String> stringList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                        .eq(QtCheckTaskDetail::getTaskId, r.getId()).in(CollUtil.isNotEmpty(itemIds),QtCheckTaskDetail::getItemId, itemIds))
                                .stream()
                                .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                        stringList.stream().filter(s -> Objects.nonNull(s)).filter(s -> StrUtil.isNotBlank(s)).forEach(s -> {
                            BigDecimal bigDecimal = new BigDecimal(s);
                            if (Objects.nonNull(bigDecimal)){
                                bigDecimals.add(bigDecimal);
                            }
                        });
                    });
                    if (CollUtil.isNotEmpty(bigDecimals)){
                        BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                        itemValue.add(average.toString());
                    }else {
                        itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
                    }
                }

                String timeFormat = new String();
                if (QtManagerConstant.QtGranularityEnum.HOUR.getName().equals(q.getGranularity())){
                  String startHourFormat = LocalDateUtil.format(start, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    String endHourFormat = LocalDateUtil.format(end, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    timeFormat = startHourFormat +"~"+endHourFormat;
                }else if(QtManagerConstant.QtGranularityEnum.DAY.getName().equals(q.getGranularity())){
                    timeFormat = LocalDateUtil.localDateFormat(start.toLocalDate(),DatePattern.NORM_DATE_PATTERN);
                }else if (QtManagerConstant.QtGranularityEnum.WEEK.getName().equals(q.getGranularity())){
                    LocalDate date = start.toLocalDate();
                    WeekFields weekFields = WeekFields.ISO;
                    int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
                  String weekFormat =  LocalDateUtil.localDateFormat(date,DatePattern.NORM_DATE_PATTERN);
                    timeFormat="第"+weekNumber+"周"+" "+weekFormat;
                }else if (QtManagerConstant.QtGranularityEnum.MONTH.getName().equals(q.getGranularity())){
                    timeFormat= LocalDateUtil.localDateFormat(start.toLocalDate(),DatePattern.NORM_MONTH_PATTERN);
                }else if (QtManagerConstant.QtGranularityEnum.QUARTER.getName().equals(q.getGranularity())){
                    int currentMonth = start.getMonthValue();
                    String  quarter ="";
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        quarter="第一季度";
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        quarter="第二季度";
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        quarter="第三季度";
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        quarter="第四季度";
                    }
                    timeFormat = start.getYear()+" "+quarter;
                }else if (QtManagerConstant.QtGranularityEnum.YEAR.getName().equals(q.getGranularity())){
                    timeFormat= start.getYear()+"";
                }
                dates.add(timeFormat);
            }
            qtReportChartVO.setXData(dates);
            qtReportChartVO.setItemValue(itemValue);

            qtReportChartVOlist.add(qtReportChartVO);
        });
        return qtReportChartVOlist;
    }

    /**
     * 根据分组id批量删除卡片
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByGroupId(Integer id) {
        List<Integer> ids = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, id))
                .stream().map(QtReport::getId).collect(Collectors.toList());
        return removeByIds(ids);
    }

    /**
     * 根据报表id查询图表详情
     * @param id
     * @return
     */
    @Override
    public QtReportChartVO getQtReportChartVO(Integer id) {
        QtReportChartVO qtReportChartVO = new QtReportChartVO();
        if (Objects.isNull(id)){
            throw new BusinessException("卡片id不能为空");
        }
        QtReport q = getById(id);
        if (Objects.isNull(q)){
            return qtReportChartVO;
        }
        BeanUtils.copyProperties(q, qtReportChartVO);
        if (Objects.nonNull(q.getCheckType())) {
            qtReportChartVO.setReportCardType(q.getReportCardType());
        }
        if (Objects.nonNull(q.getMaterialId())) {
            qtReportChartVO.setMaterialId(q.getMaterialId());
        }
        //根据物料id查询物料
        if (Objects.nonNull(q.getMaterialId())) {
            QtMaterial material = qtMaterialService.getById(q.getMaterialId());
            //设置物料名称
            if (Objects.nonNull(material)) {
                qtReportChartVO.setMaterialName(material.getName());
            }
        }
        //该检验任务的包括快照的id集合
        List<Integer> itemIds = Lists.newArrayList();
        if (Objects.nonNull(q.getCheckItemId())) {
            QtCheckItem qtCheckItem  = qtCheckItemService.getById(q.getCheckItemId());
            if (Objects.nonNull(qtCheckItem)) {
                if (Objects.nonNull(qtCheckItem.getName())) {
                    qtReportChartVO.setCheckItemName(qtCheckItem.getName());
                }
                List<Integer> checkIds = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                                .eq(QtCheckItem::getCode, qtCheckItem.getCode()))
                        .stream().map(QtCheckItem::getId).collect(Collectors.toList());
                //添加快照ids
                if (CollUtil.isNotEmpty(checkIds)){
                    checkIds.forEach(s->{
                        itemIds.add(s);
                    });
                }
                //设置检验项的数据类型
                Long dataType = qtCheckItem.getDataType();
                qtReportChartVO.setDataTypeId(dataType);
                List<Long> ids = Lists.newArrayList();
                ids.add(dataType);
                Map<Long, SysDictVO> longSysDictVOMap = FeignOptional.of(remoteDictFeignApi.listById(ids)).get();
                SysDictVO sysDictVO = longSysDictVOMap.get(dataType);
                if (Objects.nonNull(sysDictVO.getName())) {
                    qtReportChartVO.setDataTypeName(sysDictVO.getName());
                }

                if (Objects.nonNull(qtCheckItem.getMeasureUnit())) {
                    Set<Long> measureIds = Sets.newHashSet();
                    measureIds.add(qtCheckItem.getMeasureUnit());
                    Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureIds)).get();
                    String measureName = longStringMap.get(qtCheckItem.getMeasureUnit());
                    if (Objects.nonNull(measureName)) {
                        qtReportChartVO.setMeasureUnit(measureName);
                        if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(measureName)) {
                            qtReportChartVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
                        }
                    }
                }

            }
        }

        //根据条件查出所有的任务
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                        .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                        .eq(QtCheckTask::getTaskType, q.getCheckType())
                        .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                        .ge(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                        .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
                //.and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),q.getCustomer())
        );

        //设置开始结束时间
        qtReportChartVO.setTimeScopeStart(q.getTimeScopeStart().toLocalDate());
        qtReportChartVO.setTimeScopeEnd(q.getTimeScopeEnd().toLocalDate());
        //设置时间分片
        List<String> dates = Lists.newArrayList();
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(q.getGranularity());
        if (Objects.isNull(granularityEnum)){
            throw new BusinessException("粒度不存在");
        }
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , q.getTimeScopeStart()
                , q.getTimeScopeEnd());
        //设置折线图数据
        List<String> itemValue = new ArrayList<>();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出时间分片内所有检验任务的数据
            List<QtCheckTask> conditionTasks = qtCheckTasks.stream().filter(v -> Objects.nonNull(v.getFinishedTime())).filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            //如果检验任务为空则该时间段下检验结果为0
            if (CollUtil.isEmpty(conditionTasks)){
                itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
            }else {
                List<BigDecimal> bigDecimals = Lists.newArrayList();
                conditionTasks.forEach(r->{
                    List<String> stringList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .eq(QtCheckTaskDetail::getTaskId, r.getId()).in(CollUtil.isNotEmpty(itemIds),QtCheckTaskDetail::getItemId, itemIds))
                            .stream()
                            .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                    stringList.stream().filter(s -> Objects.nonNull(s)).filter(s -> StrUtil.isNotBlank(s)).forEach(s -> {
                        BigDecimal bigDecimal = new BigDecimal(s);
                        if (Objects.nonNull(bigDecimal)){
                            bigDecimals.add(bigDecimal);
                        }
                    });
                });
                if (CollUtil.isNotEmpty(bigDecimals)){
                    BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                    itemValue.add(average.toString());
                }else {
                    itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
                }
            }

            String timeFormat = new String();
            if (QtManagerConstant.QtGranularityEnum.HOUR.getName().equals(q.getGranularity())){
                String startHourFormat = LocalDateUtil.format(start, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                String endHourFormat = LocalDateUtil.format(end, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                timeFormat = startHourFormat +"~"+endHourFormat;
            }else if(QtManagerConstant.QtGranularityEnum.DAY.getName().equals(q.getGranularity())){
                timeFormat = LocalDateUtil.localDateFormat(start.toLocalDate(),DatePattern.NORM_DATE_PATTERN);
            }else if (QtManagerConstant.QtGranularityEnum.WEEK.getName().equals(q.getGranularity())){
                LocalDate date = start.toLocalDate();
                WeekFields weekFields = WeekFields.ISO;
                int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
                String weekFormat =  LocalDateUtil.localDateFormat(date,DatePattern.NORM_DATE_PATTERN);
                timeFormat="第"+weekNumber+"周"+" "+weekFormat;
            }else if (QtManagerConstant.QtGranularityEnum.MONTH.getName().equals(q.getGranularity())){
                timeFormat= LocalDateUtil.localDateFormat(start.toLocalDate(),DatePattern.NORM_MONTH_PATTERN);
            }else if (QtManagerConstant.QtGranularityEnum.QUARTER.getName().equals(q.getGranularity())){
                int currentMonth = start.getMonthValue();
                String  quarter ="";
                if (currentMonth >= 1 && currentMonth <= 3) {
                    quarter="第一季度";
                } else if (currentMonth >= 4 && currentMonth <= 6) {
                    quarter="第二季度";
                } else if (currentMonth >= 7 && currentMonth <= 9) {
                    quarter="第三季度";
                } else if (currentMonth >= 10 && currentMonth <= 12) {
                    quarter="第四季度";
                }
                timeFormat = start.getYear()+" "+quarter;
            }else if (QtManagerConstant.QtGranularityEnum.YEAR.getName().equals(q.getGranularity())){
                timeFormat= start.getYear()+"";
            }
            dates.add(timeFormat);
        }

        qtReportChartVO.setXData(dates);
        qtReportChartVO.setItemValue(itemValue);
        return qtReportChartVO;
    }

    /**
     * 下载spc报表
     * @param response
     * @param dto
     */
    @SneakyThrows
    @Override
    public void downloadSPC(HttpServletResponse response, QtReportExcelDTO dto) {
        Map<String, Object> map = MapUtils.newHashMap();

        QtReport report = getById(dto.getReportId());
        if (Objects.isNull(report)){
            throw new BusinessException("该报表不存在,无法下载");
        }
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .le(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());

        //根据条件先low出所有的检验任务数据
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        //设置时间分片
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(report.getGranularity());
        if (Objects.isNull(granularityEnum)){
            throw new BusinessException("粒度不存在");
        }
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , report.getTimeScopeStart()
                , report.getTimeScopeEnd());

        //根据检验项id获取检验项名称
        QtCheckItem qtCheckItem=new QtCheckItem();
        qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        String measureUnitName = new String();
        if (Objects.nonNull(qtCheckItem)){
            Set<Long> ids = Sets.newHashSet();
            ids.add(qtCheckItem.getMeasureUnit());
            Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
            //获取单位名称
            measureUnitName = longStringMap.get(qtCheckItem.getMeasureUnit());
        }
        //查询物料名称
        QtMaterial qtMaterial=new QtMaterial();
        if (Objects.nonNull(report.getMaterialId())) {
            qtMaterial= qtMaterialService.getById(report.getMaterialId());
        }
        //spc任务明细表的数据集合
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start =  localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出该时间分片下的所有检验任务
            List<QtCheckTask> conditionDataList = qtCheckTasks.stream()
                    .filter(v->Objects.nonNull(v.getFinishedTime()))
                    .filter(v -> {
                        //数据范围应该是大于等于开始时间  小于结束时间
                        if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime()))
                                && end.isAfter(v.getFinishedTime())) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }).collect(Collectors.toList());

            //设置返回数据类型对象
            QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
            //设置明细表的主键id
            qtSpcReportDetailVO.setId(i);
            //设置开始结束分片范围
            qtSpcReportDetailVO.setStartTime(start);
            qtSpcReportDetailVO.setEndTime(end);
            //设置检验项类型
            qtSpcReportDetailVO.setCheckType(report.getCheckType());
            if (Objects.nonNull(qtCheckItem)) {
                //检验项id
                qtSpcReportDetailVO.setCheckItemId(qtCheckItem.getId());
                //设置检验项名称
                qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());
            }
            //设置计量单位名称
            if (Objects.nonNull(measureUnitName)){
                qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
                if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(measureUnitName)){
                    qtSpcReportDetailVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
                }
            }

            //设置上限
            qtSpcReportDetailVO.setUpLimit(report.getUpLimit());
            //设置下限
            qtSpcReportDetailVO.setDownLimit(report.getDownLimit());
            //设置标准值
            qtSpcReportDetailVO.setStandardValue(report.getStandardValue());

            //如果conditionDataList为空则该时间段下没有检验任务,检验结果平均值设置为0,检验详情数据为空
            List<QtSpcReportDetailTaskVO> qtSpcReportDetailTaskVOS= Lists.newArrayList();
            BigDecimal resultAverage =new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
            if (CollUtil.isEmpty(conditionDataList)){
                qtSpcReportDetailVO.setResultAverage(resultAverage);
                qtSpcReportDetailVO.setReportDetailTaskVOList(qtSpcReportDetailTaskVOS);
            }else if (CollUtil.isNotEmpty(conditionDataList)){
                List<BigDecimal> mainDecimals = Lists.newArrayList();
                //遍历该时间段下的检验任务设置详情
                conditionDataList.forEach(s->{
                    QtSpcReportDetailTaskVO taskVO = new QtSpcReportDetailTaskVO();
                    //详情检验结果平均值
                    BigDecimal detailAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
                    BeanUtils.copyProperties(s,taskVO);

                    //设置完成时间
                    taskVO.setFinishedTime(LocalDateUtil.format(s.getFinishedTime(),DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                    //检验任务结果集合
                    List<String> stringList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .eq(QtCheckTaskDetail::getTaskId, s.getId())).stream()
                            .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                    List<BigDecimal> bigDecimals = Lists.newArrayList();
                    stringList.stream().filter(st -> Objects.nonNull(st)).filter(st -> StrUtil.isNotBlank(st)).forEach(st -> {
                        BigDecimal bigDecimal = new BigDecimal(st);
                        if (Objects.nonNull(bigDecimal)){
                            bigDecimals.add(bigDecimal);
                        }
                    });
                    if (CollUtil.isNotEmpty(bigDecimals)){
                        BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        detailAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                        taskVO.setCheckResult(detailAverage.toString());
                    }else {
                        taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                    }
                    mainDecimals.add(detailAverage);
                    qtSpcReportDetailTaskVOS.add(taskVO);
                });
                BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));

                //设置检验项平均值
                qtSpcReportDetailVO.setResultAverage(resultAverage);
                qtSpcReportDetailVO.setReportDetailTaskVOList(qtSpcReportDetailTaskVOS);
            }
            mainList.add(qtSpcReportDetailVO);
        }
        //筛选dto数据
        if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.DELIVER.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.DELIVER.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getName());
        }else if (report.getCheckType()==QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()){
            map.put("checkType",QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getName());
        }

        //根据自己的模版put数据
        map.put("reportName",report.getName());
        //设置物料名称
        if (Objects.nonNull( qtMaterial.getName())){
            map.put("materialName", qtMaterial.getName());
        }
        //设置生产车间
        if (CollUtil.isNotEmpty(qtCheckTasks)){
            QtCheckTask qtCheckTask = qtCheckTasks.get(0);
            QtBaseIdNameDTO materialWorkshop = qtCheckTask.getMaterialWorkshop();
            if (Objects.nonNull(materialWorkshop.getName())){
                map.put("productWorkshopName",materialWorkshop.getName());
            }
            //设置客户
            QtBaseIdNameDTO customer = qtCheckTask.getCustomer();
            if (Objects.nonNull(customer.getName())){
                map.put("customer",customer.getName());
            }
        }
        map.put("timeScopeStart",report.getTimeScopeStart().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        map.put("timeScopeEnd",report.getTimeScopeEnd().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));

        //对全量数据表进行筛选
        List<QtSpcReportDownloadVO> data = Lists.newArrayList();

        if(CollUtil.isNotEmpty(mainList)){

         mainList.forEach(s->{
             QtSpcReportDownloadVO vo = new QtSpcReportDownloadVO();
             vo.setStartTime(s.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
             vo.setEndTime(s.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
             if (QtManagerConstant.QtGranularityEnum.HOUR.getName().equals(report.getGranularity())){
                 vo.setStartTime(LocalDateUtil.format(s.getStartTime(),DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                 vo.setEndTime(LocalDateUtil.format(s.getEndTime(),DatePattern.NORM_DATETIME_MINUTE_PATTERN));
             }
             vo.setCheckItemName(s.getCheckItemName());
             vo.setMeasureUnit(s.getMeasureUnit());

             if (CollUtil.isNotEmpty(dto.getFilterConditions())){
                 List<String> conditions = dto.getFilterConditions();
                 if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.UPLIMIT.getName())){
                     //设置上限
                     vo.setUpLimit(s.getUpLimit());
                 }
                 if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.DOWNLIMIT.getName())){
                     //设置下限
                     vo.setDownLimit(s.getDownLimit());
                 }
                 if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.STANDARDVALUE.getName())){
                     //设置标准值
                     vo.setStandardValue(s.getStandardValue());
                 }
                 if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.TESTVALUE.getName())){
                     //设置标准值
                     vo.setResultAverage(s.getResultAverage());
                 }
             }
             data.add(vo);
         });

        }
        //封装的下载
        DownloadUtil.downloadTemplate(response,QtManagerConstant.QT_CHART_SPC_NAME,report.getName(),map,data,dto.getBase64());
    }


    /**
     * 根据id删除spc卡片
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean reportDelete(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("spc卡片id不能为空");
        }
        removeById(id);
        return Boolean.TRUE;
    }

    /**
     * 根据id查询spc控制图
     *
     * @param id
     * @return
     */
    @Override
    public QtReportVO getQtReport(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("查询id不能为空");
        }
        QtReport qtReport = getById(id);
        QtReportVO qtReportVO = new QtReportVO();
        if (Objects.isNull(qtReport)) {
            throw new BusinessException("数据库中没有此SPC控制图");
        }
        BeanUtils.copyProperties(qtReport, qtReportVO);
        return qtReportVO;
    }

    /**
     * 编辑SPC控制图卡片
     *
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public QtReportVO edit(QtReportDTO dto) {
        QtReport qureyReport = getById(dto.getId());
        QtReport report = new QtReport();
        if (Objects.nonNull(qureyReport)) {
            BeanUtils.copyProperties(dto, report);
            report.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(dto.getTimeScopeStart()));
            LocalDate timeScopeEnd = dto.getTimeScopeEnd();
            report.setTimeScopeEnd(LocalDateTime.of(timeScopeEnd.getYear(),timeScopeEnd.getMonth()
                    ,timeScopeEnd.getDayOfMonth(),23,59,59));
            updateById(report);
        }
       qureyReport = getById(dto.getId());
        //编辑完后回显数据
        QtReportVO qtReportVO = new QtReportVO();
        BeanUtils.copyProperties(qureyReport,qtReportVO);
        qtReportVO.setTimeScopeStart(qureyReport.getTimeScopeStart().toLocalDate());
        qtReportVO.setTimeScopeEnd(qureyReport.getTimeScopeEnd().toLocalDate());
        return qtReportVO;
    }

    /**
     * spc控制图明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtSpcReportDetailVO> pageList(QtSpcReportDetailPageDTO dto) {
        //根据报表卡片id先查找到spc数据库中的报表数据
        QtReport report = getById(dto.getId());
        if (Objects.isNull(report)){
            throw new BusinessException("该报表卡片不存在");
        }
        //2.解析排序规则
//        if ((QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME.equals(dto.getSortField()))) {
//            if (dto.getAscFlag()) {
//                //使用last把sql用于排序的中文函数代码语句拼接到最后
//                condition.last("ORDER BY CONVERT(finished_time using gbk) ASC");
//            } else {
//                condition.last("ORDER BY CONVERT(finished_time using gbk) DESC");
//            }
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialId);
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductLine);
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getCustomerName);
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getSupplierName);
//        } else {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialWorkshopName);
//        }
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .le(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());

        //根据条件先low出所有的检验任务数据
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        //设置时间分片
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(report.getGranularity());
        if (Objects.isNull(granularityEnum)){
            throw new BusinessException("粒度不存在");
        }
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , report.getTimeScopeStart()
                , report.getTimeScopeEnd());

        //3.定义分页对象
        Page<QtSpcReportDetailVO> page = new Page<>();
        //4.查询分页数据
        List<QtCheckTask> dataList = qtCheckTaskService.list(condition);
        //根据检验项id获取检验项名称
        QtCheckItem qtCheckItem=new QtCheckItem();
        qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        String measureUnitName = new String();
        if (Objects.nonNull(qtCheckItem)){
        Set<Long> ids = Sets.newHashSet();
        ids.add(qtCheckItem.getMeasureUnit());
        Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
        //获取单位名称
         measureUnitName = longStringMap.get(qtCheckItem.getMeasureUnit());
        }

        //查询物料名称
        QtMaterial qtMaterial=new QtMaterial();
        if (Objects.nonNull(report.getMaterialId())) {
            qtMaterial= qtMaterialService.getById(report.getMaterialId());
        }
        String name = qtMaterial.getName();
        //spc任务明细表的数据集合
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出该时间分片下的所有检验任务
            List<QtCheckTask> conditionDataList = qtCheckTasks.stream()
                    .filter(v->Objects.nonNull(v.getFinishedTime()))
                    .filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime()))
                        && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            //设置返回数据类型对象
            QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
            //设置主键id
            qtSpcReportDetailVO.setId(i);
            //设置开始结束分片范围
            qtSpcReportDetailVO.setStartTime(start);
            qtSpcReportDetailVO.setEndTime(end);
            //设置检验项类型
            qtSpcReportDetailVO.setCheckType(report.getCheckType());
            if (Objects.nonNull(qtCheckItem)) {
                //检验项id
                qtSpcReportDetailVO.setCheckItemId(qtCheckItem.getId());
                //设置检验项名称
                qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());
            }
            //设置计量单位名称
            if (Objects.nonNull(measureUnitName)){
                qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
            } else if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(measureUnitName)){
                qtSpcReportDetailVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
            }
            //设置标准值
            qtSpcReportDetailVO.setStandardValue(report.getStandardValue());
            //设置上限
            qtSpcReportDetailVO.setUpLimit(report.getUpLimit());
            //设置下限
            qtSpcReportDetailVO.setDownLimit(report.getDownLimit());
            //如果conditionDataList为空则该时间段下没有检验任务,检验结果平均值设置为0,检验详情数据为空
            List<QtSpcReportDetailTaskVO> qtSpcReportDetailTaskVOS= Lists.newArrayList();
            BigDecimal resultAverage =new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
            if (CollUtil.isEmpty(conditionDataList)){
                qtSpcReportDetailVO.setResultAverage(resultAverage);
                qtSpcReportDetailVO.setReportDetailTaskVOList(qtSpcReportDetailTaskVOS);
            }else if (CollUtil.isNotEmpty(conditionDataList)){
                List<BigDecimal> mainDecimals = Lists.newArrayList();
                //遍历该时间段下的检验任务设置详情
                conditionDataList.forEach(s->{
                    QtSpcReportDetailTaskVO taskVO = new QtSpcReportDetailTaskVO();
                    //详情检验结果平均值
                    BigDecimal detailAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
                    BeanUtils.copyProperties(s,taskVO);
                    //设置物料名称
                    if (Objects.nonNull(name)){
                        taskVO.setMaterialName(name);
                    }
                    //设置完成时间
                    taskVO.setFinishedTime(LocalDateUtil.format(s.getFinishedTime(),DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                    //检验任务结果集合
                    List<String> stringList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getTaskId, s.getId())).stream()
                            .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                    List<BigDecimal> bigDecimals = Lists.newArrayList();
                    stringList.stream().filter(st -> Objects.nonNull(st)).filter(st -> StrUtil.isNotBlank(st)).forEach(st -> {
                        BigDecimal bigDecimal = new BigDecimal(st);
                        if (Objects.nonNull(bigDecimal)){
                            bigDecimals.add(bigDecimal);
                        }
                    });
                    if (CollUtil.isNotEmpty(bigDecimals)){
                        BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        detailAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                        taskVO.setCheckResult(detailAverage.toString());
                    }else {
                        taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                    }
                    mainDecimals.add(detailAverage);
                    qtSpcReportDetailTaskVOS.add(taskVO);
                });
                BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));
                qtSpcReportDetailVO.setResultAverage(resultAverage);
                qtSpcReportDetailVO.setReportDetailTaskVOList(qtSpcReportDetailTaskVOS);
            }
            mainList.add(qtSpcReportDetailVO);
        }

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
        //排序
        List<QtSpcReportDetailVO> collect = mainList.stream().skip(skip).limit(dto.getSize()).collect(Collectors.toList());
        page.setCurrent(dto.getCurrent());
        page.setTotal(mainList.size());
        page.setSize(dto.getSize());
        page.setRecords(collect);

        return page;
    }

    /**
     * 根据Spc卡片类型查询卡片列表
     *
     * @param reportCardType
     * @return
     */
    @Override
    public List<QtReportVO> getQtReportVOListByType(String reportCardType) {
        List<QtReport> qtReports = list(new LambdaQueryWrapper<QtReport>()
                .like(QtReport::getReportCardType, reportCardType)
                .last(StringUtils.isBlank(reportCardType), "limit 100"));
        List<QtReportVO> qtReportVOS = Lists.newArrayList();
        if (Objects.nonNull(qtReports)) {
            QtReportVO qtReportVO = new QtReportVO();
            qtReports.forEach(q -> {
                BeanUtils.copyProperties(q, qtReportVO);
                qtReportVOS.add(qtReportVO);
            });
        }
        return qtReportVOS;
    }
}




