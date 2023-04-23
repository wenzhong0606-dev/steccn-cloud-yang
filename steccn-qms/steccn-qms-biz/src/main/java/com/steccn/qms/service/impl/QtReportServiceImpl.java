package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.util.*;
import com.steccn.qms.vo.*;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final QtReportWarnUserService qtReportWarnUserService;
    private final RemoteMsgFeignApi remoteMsgFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final QtCheckTaskMapper taskMapper;

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
        if (qtReports.size() > 0) {
            throw new BusinessException("该报表卡片已存在,请勿重复创建!");
        }
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto, qtReport);
        qtReport.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(dto.getTimeScopeStart()));
        LocalDate timeScopeEnd = dto.getTimeScopeEnd();
        qtReport.setTimeScopeEnd(LocalDateTime.of(timeScopeEnd.getYear(), timeScopeEnd.getMonth()
                , timeScopeEnd.getDayOfMonth(), 23, 59, 59));
        //根据分组id去查询分组
        this.save(qtReport);
        //保存告警人员信息
        if (QtManagerConstant.QtMaterialStatusEnum.ENABLE.getCode().equals(dto.getWarnFlag())) {
            qtReportWarnUserService.saveWarnUser(dto.getWarnUserIds(), qtReport.getId());
        }
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
        if (Objects.isNull(groupid)) {
            throw new BusinessException("分组id不能为空");
        }
        //获取检验项id集合
        List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>().
                eq(QtCheckPlanGroupItem::getGroupId, groupid));
        //取得检验项id的集合
        List<Integer> checkItemIds = qtCheckPlanGroupItems.stream().map(QtCheckPlanGroupItem::getCheckItemId)
                .filter(s -> Objects.nonNull(s)).collect(Collectors.toList());
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
        if (Objects.nonNull(qtCheckItem) && Objects.nonNull(qtCheckItem.getMeasureUnit())) {
            measureUnitSet.add(qtCheckItem.getMeasureUnit());
            Map<Long, String> dictData = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(measureUnitSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (Objects.nonNull(dictData)) {
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
     *
     * @param response
     * @return
     */
    public void setDownloadHeader(HttpServletResponse response, String name) throws UnsupportedEncodingException {
        response.setContentType(QtManagerConstant.QT_CHART_EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode(name, QtManagerConstant.QT_CHART_EXCEL_ENCODE).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    /**
     * 根据报表卡片分组ID去查询报表卡片列表
     *
     * @param reportGroupId
     * @return
     */
    @Override
    public List<QtReportChartVO> getQtReportList(Integer reportGroupId) {
        List<QtReportChartVO> qtReportChartVOlist = Lists.newArrayList();
        List<QtReport> qtReportList = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, reportGroupId)
                .orderByDesc(QtReport::getId));
        if (CollUtil.isEmpty(qtReportList)) {
            return qtReportChartVOlist;
        }

        //检验项快照集合
        Set<Integer> checkItemSet = Sets.newHashSet();
        //物料主键集合
        Set<Long> materialIdSet = Sets.newHashSet();
        qtReportList.forEach(ele -> {
            materialIdSet.add(ele.getMaterialId());
            checkItemSet.add(ele.getCheckItemId());
        });
        Map<Long, String> materialIdNameMap = FeignOptional.of(() -> remoteSpareFeignApi.listByIdCh(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取快照集合和检验项集合
        List<Integer> checkItemList = Lists.newArrayList(checkItemSet);
        Map<Integer, Set<Integer>> snapshotMap = qtCheckItemService.checkItemSnapshotSet(checkItemList);
        Map<Integer, QtCheckItem> checkItemMap = qtCheckItemService.mapCheckItem(checkItemList);
        QtCheckItem defaultQtCheckItem = new QtCheckItem();

        //获取检验项的数据类型和单位
        Set<Long> dictIdSet = Sets.newHashSet();
        checkItemMap.values().forEach(ele ->{
              dictIdSet.add(ele.getDataType());
              dictIdSet.add(ele.getMeasureUnit());
        });
        Map<Long, String> dictIdNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();


        //遍历查出来的集合封装数据
        qtReportList.forEach(q -> {
            //返回报表数据
            QtReportChartVO qtReportChartVO = new QtReportChartVO();
            BeanUtils.copyProperties(q, qtReportChartVO);
            //赋值物料名称
            qtReportChartVO.setMaterialName(materialIdNameMap.get(q.getMaterialId()));
            //该检验任务的包括快照的id集合
            Set<Integer> itemIds = snapshotMap.get(q.getCheckItemId());
            //设置检验项类型，数据类型和单位
            if (Objects.nonNull(q.getCheckItemId())) {
                QtCheckItem qtCheckItem = checkItemMap.getOrDefault(q.getCheckItemId(), defaultQtCheckItem);
                qtReportChartVO.setCheckItemName(qtCheckItem.getName());
                //设置检验项的数据类型
                qtReportChartVO.setDataTypeId(qtCheckItem.getDataType());
                String dataTypeName = dictIdNameMap.get(qtCheckItem.getDataType());
                qtReportChartVO.setDataTypeName(dataTypeName);
                qtReportChartVO.setMeasureUnit(dictIdNameMap.get(qtCheckItem.getMeasureUnit()));
                //如果是百分比，则单位会为% ， 否则其他都是单位本身
                if (Objects.nonNull(dataTypeName) && dataTypeName.contains("百分")) {
                    qtReportChartVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
                }
            }
            //设置开始结束时间
            qtReportChartVO.setTimeScopeStart(q.getTimeScopeStart().toLocalDate());
            qtReportChartVO.setTimeScopeEnd(q.getTimeScopeEnd().toLocalDate());
            QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(q.getGranularity());
            Assert.notNull(granularityEnum,"粒度不存在");

            if(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.equals(q.getCheckType())){
                //来料检
                incomingCheck(q, qtReportChartVO, itemIds,granularityEnum);
            }else{
                //其余检验方案，以完成时间来区分
                otherCheck(q, qtReportChartVO, itemIds,granularityEnum);
            }
            qtReportChartVOlist.add(qtReportChartVO);
        });
        return qtReportChartVOlist;
    }

    /**
     * 来料检
     * @param q
     * @param qtReportChartVO
     * @param itemIds
     */
    private void incomingCheck(QtReport q, QtReportChartVO qtReportChartVO, Set<Integer> itemIds,QtManagerConstant.QtGranularityEnum granularityEnum) {
        //获取符合条件的原始数据
        List<QtIncomingCheckDTO> sourceList = qtCheckTaskService.comingList(itemIds);
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType(), q.getTimeScopeStart(), q.getTimeScopeEnd());
        //设置折线图数据
        List<String> itemValue = new ArrayList<>();

        //将原始数据根据时间日期
        List<QtIncomingCheckDTO> dataList = Lists.newArrayList();
        sourceList.forEach(ele ->{
            ele.getSamplingTimeArray().forEach(inner ->{
                if(Objects.nonNull(inner)){
                    ele.setSamplingTime(inner);
                    dataList.add(ele);
                }
            });
        });
        //设置时间分片
        List<String> xList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出时间分片内所有检验任务的数据
            List<QtIncomingCheckDTO> conditionTasks = dataList.stream().filter(v -> isNumber(v.getCheckResult()) || isFloatNumber(v.getCheckResult())).filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if (LocalDateUtil.isRange(v.getSamplingTime(), start, end, true, false)) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            //取平均值
            BigDecimal reduce = conditionTasks.stream().map(v -> new BigDecimal(v.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
            itemValue.add(BigDecimalUtil.format(BigDecimalUtil.divide(reduce, new BigDecimal(conditionTasks.size()))));
            //赋值x轴点
            xList.add(XAxisUtil.convert(granularityEnum,start));
        }
        qtReportChartVO.setXData(xList);
        qtReportChartVO.setItemValue(itemValue);
    }

    /**
     * 其他检验类型
     * @param q 报表对象
     * @param qtReportChartVO 图表对象
     * @param itemIds 检验项快照id集合
     */
    private void otherCheck(QtReport q, QtReportChartVO qtReportChartVO, Set<Integer> itemIds,QtManagerConstant.QtGranularityEnum granularityEnum) {
        //根据条件查出所有的任务
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                .eq(QtCheckTask::getTaskType, q.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
        );
        //设置时间分片
        List<String> dates = Lists.newArrayList();
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType(), q.getTimeScopeStart(), q.getTimeScopeEnd());
        //设置折线图数据
        List<String> itemValue = new ArrayList<>();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出时间分片内所有检验任务的数据
            List<QtCheckTask> conditionTasks = qtCheckTasks.stream().filter(v -> Objects.nonNull(v.getFinishedTime())).filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if (LocalDateUtil.isRange(v.getFinishedTime(), start, end, true, false)) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(conditionTasks)) {
                //如果检验任务为空则该时间段下检验结果为0
                Set<Long> taskIdSet = conditionTasks.stream().map(v -> v.getId()).collect(Collectors.toSet());
                //检验任务样本结果集合
                List<BigDecimal> valueList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                .in(QtCheckTaskDetail::getTaskId, taskIdSet)
                                .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                        .stream().filter(qt -> Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                        .filter(qt -> isNumber(qt.getCheckResult()) || isFloatNumber(qt.getCheckResult()))
                        .map(v -> new BigDecimal(v.getCheckResult())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(valueList)) {
                    BigDecimal reduce = valueList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    itemValue.add(BigDecimalUtil.format(BigDecimalUtil.divide(reduce,new BigDecimal(valueList.size()))));
                } else {
                    itemValue.add(BigDecimalUtil.format(BigDecimal.ZERO));
                }
            } else {
                itemValue.add(BigDecimalUtil.format(BigDecimal.ZERO));
            }
            //赋值x轴点
            dates.add(XAxisUtil.convert(granularityEnum,start));
        }
        qtReportChartVO.setXData(dates);
        qtReportChartVO.setItemValue(itemValue);
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
     * 根据卡片id查询spc图表
     *
     * @param id
     * @return
     */
    @Override
    public QtReportChartVO getQtReportChartVO(Integer id) {
        QtReportChartVO qtReportChartVO = new QtReportChartVO();
        if (Objects.isNull(id)) {
            throw new BusinessException("卡片id不能为空");
        }
        QtReport q = getById(id);
        if (Objects.isNull(q)) {
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
            QtCheckItem qtCheckItem = qtCheckItemService.getById(q.getCheckItemId());
            if (Objects.nonNull(qtCheckItem)) {
                if (Objects.nonNull(qtCheckItem.getName())) {
                    qtReportChartVO.setCheckItemName(qtCheckItem.getName());
                }
                List<Integer> checkIds = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                        .eq(QtCheckItem::getCode, qtCheckItem.getCode()))
                        .stream().map(QtCheckItem::getId).collect(Collectors.toList());
                //添加快照ids
                if (CollUtil.isNotEmpty(checkIds)) {
                    checkIds.forEach(s -> {
                        itemIds.add(s);
                    });
                }
                //设置检验项的数据类型
                Long dataType = qtCheckItem.getDataType();
                qtReportChartVO.setDataTypeId(dataType);
                List<Long> ids = Lists.newArrayList();
                ids.add(dataType);
                Map<Long, SysDictVO> longSysDictVOMap = FeignOptional.of(() -> remoteDictFeignApi.listById(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                SysDictVO sysDictVO = longSysDictVOMap.get(dataType);
                if (Objects.nonNull(sysDictVO.getName())) {
                    qtReportChartVO.setDataTypeName(sysDictVO.getName());
                }

                if (Objects.nonNull(qtCheckItem.getMeasureUnit())) {
                    Set<Long> measureIds = Sets.newHashSet();
                    measureIds.add(qtCheckItem.getMeasureUnit());
                    Map<Long, String> longStringMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(measureIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    String measureName = longStringMap.get(qtCheckItem.getMeasureUnit());
                    if (Objects.nonNull(measureName)) {
                        qtReportChartVO.setMeasureUnit(measureName);

                    }
                    if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(qtCheckItem.getName())) {
                        qtReportChartVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
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
        if (Objects.isNull(granularityEnum)) {
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
            if (CollUtil.isEmpty(conditionTasks)) {
                itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
            } else {
                List<BigDecimal> bigDecimals = Lists.newArrayList();
                conditionTasks.forEach(r -> {
                    //检验任务样本结果集合
                    List<String> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getTaskId, r.getId())
                            .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                            .stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                            .map(QtCheckTaskDetail::getCheckResult)
                            .collect(Collectors.toList());
                    List<BigDecimal> resultList = Lists.newArrayList();
                    if (CollUtil.isNotEmpty(results)) {
                        results.forEach(re -> {
                            if (isNumber(re) || isFloatNumber(re)) {
                                BigDecimal bigDecimal = new BigDecimal(re);
                                if (Objects.nonNull(bigDecimal)) {
                                    resultList.add(bigDecimal);
                                }
                            }
                        });
                    }
                    if (CollUtil.isNotEmpty(resultList)) {
                        BigDecimal detailSum = resultList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal result = BigDecimalUtil.divide(detailSum, BigDecimal.valueOf(resultList.size()));
                        bigDecimals.add(result);
                    }
                });
                if (CollUtil.isNotEmpty(bigDecimals)) {
                    BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                    itemValue.add(average.toString());
                } else {
                    itemValue.add(QtManagerConstant.THE_CALCULATION_RESULTS);
                }
            }

            String timeFormat = new String();
            if (QtManagerConstant.QtGranularityEnum.HOUR.getName().equals(q.getGranularity())) {
                String startHourFormat = LocalDateUtil.format(start, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                String endHourFormat = LocalDateUtil.format(end, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                timeFormat = startHourFormat + "~" + endHourFormat;
            } else if (QtManagerConstant.QtGranularityEnum.DAY.getName().equals(q.getGranularity())) {
                timeFormat = LocalDateUtil.localDateFormat(start.toLocalDate(), DatePattern.NORM_DATE_PATTERN);
            } else if (QtManagerConstant.QtGranularityEnum.WEEK.getName().equals(q.getGranularity())) {
                LocalDate date = start.toLocalDate();
                WeekFields weekFields = WeekFields.ISO;
                int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
                String weekFormat = LocalDateUtil.localDateFormat(date, DatePattern.NORM_DATE_PATTERN);
                timeFormat = "第" + weekNumber + "周" + " " + weekFormat;
            } else if (QtManagerConstant.QtGranularityEnum.MONTH.getName().equals(q.getGranularity())) {
                timeFormat = LocalDateUtil.localDateFormat(start.toLocalDate(), DatePattern.NORM_MONTH_PATTERN);
            } else if (QtManagerConstant.QtGranularityEnum.QUARTER.getName().equals(q.getGranularity())) {
                int currentMonth = start.getMonthValue();
                String quarter = "";
                if (currentMonth >= 1 && currentMonth <= 3) {
                    quarter = "第一季度";
                } else if (currentMonth >= 4 && currentMonth <= 6) {
                    quarter = "第二季度";
                } else if (currentMonth >= 7 && currentMonth <= 9) {
                    quarter = "第三季度";
                } else if (currentMonth >= 10 && currentMonth <= 12) {
                    quarter = "第四季度";
                }
                timeFormat = start.getYear() + " " + quarter;
            } else if (QtManagerConstant.QtGranularityEnum.YEAR.getName().equals(q.getGranularity())) {
                timeFormat = start.getYear() + "";
            }
            dates.add(timeFormat);
        }
        qtReportChartVO.setXData(dates);
        qtReportChartVO.setItemValue(itemValue);
        //获取告警人员信息
        if (QtManagerConstant.QtMaterialStatusEnum.ENABLE.getCode().equals(q.getWarnFlag())) {
            qtReportChartVO.setWarnUserList(qtReportWarnUserService.getWarnUser(q.getId()));
        }
        return qtReportChartVO;
    }

    /**
     * 下载spc报表
     *
     * @param response
     * @param dto
     */
    @SneakyThrows
    @Override
    public void downloadSPC(HttpServletResponse response, QtReportExcelDTO dto) {
        Map<String, Object> map = MapUtils.newHashMap();

        QtReport report = getById(dto.getReportId());
        if (Objects.isNull(report)) {
            throw new BusinessException("该报表不存在,无法下载");
        }
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .lt(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());

        //根据条件先low出所有的检验任务数据
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        //设置时间分片
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(report.getGranularity());
        if (Objects.isNull(granularityEnum)) {
            throw new BusinessException("粒度不存在");
        }
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , report.getTimeScopeStart()
                , report.getTimeScopeEnd());

        //根据检验项id获取检验项名称
        QtCheckItem qtCheckItem = new QtCheckItem();
        qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        List<Integer> itemIds = qtCheckItemService
                .list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, qtCheckItem.getCode()))
                .stream().map(QtCheckItem::getId).collect(Collectors.toList());
        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        String measureUnitName = new String();
        if (Objects.nonNull(qtCheckItem)) {
            Set<Long> ids = Sets.newHashSet();
            ids.add(qtCheckItem.getMeasureUnit());
            Map<Long, String> longStringMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取单位名称
            measureUnitName = longStringMap.get(qtCheckItem.getMeasureUnit());
        }
        //查询物料名称
        QtMaterial qtMaterial = new QtMaterial();
        if (Objects.nonNull(report.getMaterialId())) {
            qtMaterial = qtMaterialService.getById(report.getMaterialId());
        }
        //spc任务表的详情数据集合
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出该时间分片下的所有检验任务
            List<QtCheckTask> conditionDataList = qtCheckTasks.stream()
                    .filter(v -> Objects.nonNull(v.getFinishedTime()))
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
            if (Objects.nonNull(measureUnitName)) {
                qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
            }
            if (Objects.nonNull(qtCheckItem.getName())) {
                if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(qtCheckItem.getName())) {
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
            BigDecimal resultAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
            if (CollUtil.isEmpty(conditionDataList)) {
                qtSpcReportDetailVO.setResultAverage(resultAverage);
            } else if (CollUtil.isNotEmpty(conditionDataList)) {
                List<BigDecimal> mainDecimals = Lists.newArrayList();
                //遍历该时间段下的检验任务设置详情
                conditionDataList.forEach(s -> {
                    QtSpcReportDetailTaskVO taskVO = new QtSpcReportDetailTaskVO();
                    BeanUtils.copyProperties(s, taskVO);

                    //设置完成时间
                    taskVO.setFinishedTime(LocalDateUtil.format(s.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                    //检验任务样本结果集合
                    List<String> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getTaskId, s.getId())
                            .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                            .stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                            .map(QtCheckTaskDetail::getCheckResult)
                            .collect(Collectors.toList());
                    List<BigDecimal> resultList = Lists.newArrayList();
                    if (CollUtil.isNotEmpty(results)) {
                        results.forEach(re -> {
                            if (isNumber(re) || isFloatNumber(re)) {
                                BigDecimal bigDecimal = new BigDecimal(re);
                                if (Objects.nonNull(bigDecimal)) {
                                    resultList.add(bigDecimal);
                                }
                            }
                        });
                    }
                    if (CollUtil.isNotEmpty(resultList)) {
                        BigDecimal detailSum = resultList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal result = BigDecimalUtil.divide(detailSum, BigDecimal.valueOf(resultList.size()));
                        taskVO.setCheckResult(result.toString());
                        mainDecimals.add(result);
                    } else {
                        taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                    }
                });
                if (CollUtil.isNotEmpty(mainDecimals)) {
                    BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));
                    qtSpcReportDetailVO.setResultAverage(resultAverage);
                } else {
                    qtSpcReportDetailVO.setResultAverage(new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS));
                }
            }
            mainList.add(qtSpcReportDetailVO);
        }
        //筛选dto数据
        if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.DELIVER.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.DELIVER.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getName());
        } else if (report.getCheckType() == QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()) {
            map.put("checkType", QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getName());
        }

        //根据自己的模版put数据
        map.put("reportName", report.getName());
        //设置物料名称
        if (Objects.nonNull(qtMaterial.getName())) {
            map.put("materialName", qtMaterial.getName());
        }
        //设置生产车间
        if (CollUtil.isNotEmpty(qtCheckTasks)) {
            QtCheckTask qtCheckTask = qtCheckTasks.get(0);
            QtBaseIdNameDTO materialWorkshop = qtCheckTask.getMaterialWorkshop();
            if (Objects.nonNull(materialWorkshop.getName())) {
                map.put("productWorkshopName", materialWorkshop.getName());
            }
            //设置客户
            QtBaseIdNameDTO customer = qtCheckTask.getCustomer();
            if (Objects.nonNull(customer.getName())) {
                map.put("customer", customer.getName());
            }
        }
        map.put("timeScopeStart", report.getTimeScopeStart().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        map.put("timeScopeEnd", report.getTimeScopeEnd().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));

        //对全量数据表进行筛选
        List<QtSpcReportDownloadVO> data = Lists.newArrayList();

        if (CollUtil.isNotEmpty(mainList)) {

            mainList.forEach(s -> {
                QtSpcReportDownloadVO vo = new QtSpcReportDownloadVO();
                vo.setStartTime(s.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
                vo.setEndTime(s.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
                if (QtManagerConstant.QtGranularityEnum.HOUR.getName().equals(report.getGranularity())) {
                    vo.setStartTime(LocalDateUtil.format(s.getStartTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                    vo.setEndTime(LocalDateUtil.format(s.getEndTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                }
                vo.setCheckItemName(s.getCheckItemName());
                vo.setMeasureUnit(s.getMeasureUnit());

                if (CollUtil.isNotEmpty(dto.getFilterConditions())) {
                    List<String> conditions = dto.getFilterConditions();
                    if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.UPLIMIT.getName())) {
                        //设置上限
                        vo.setUpLimit(s.getUpLimit());
                    }
                    if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.DOWNLIMIT.getName())) {
                        //设置下限
                        vo.setDownLimit(s.getDownLimit());
                    }
                    if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.STANDARDVALUE.getName())) {
                        //设置标准值
                        vo.setStandardValue(s.getStandardValue());
                    }
                    if (conditions.contains(QtManagerConstant.QtSpcFilterConditions.TESTVALUE.getName())) {
                        //设置标准值
                        vo.setResultAverage(s.getResultAverage());
                    }
                }
                data.add(vo);
            });

        }
        //封装的下载
        DownloadUtil.downloadTemplate(response, QtManagerConstant.QT_CHART_SPC_NAME, report.getName(), map, data, dto.getBase64());
    }


    /**
     * spc告警
     *
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param granularity 粒度:小时、天、周、月、季度、年
     * @return
     */
    @Override
    public Boolean warnSPC(LocalDateTime startTime, LocalDateTime endTime, String granularity) {
        //1.根据时间粒度获取spc卡片数据
        List<QtReport> list = list(new LambdaQueryWrapper<QtReport>()
                .le(QtReport::getTimeScopeStart, startTime)
                .ge(QtReport::getTimeScopeEnd, endTime)
                .eq(QtReport::getGranularity, granularity)
                .eq(QtReport::getWarnFlag, QtManagerConstant.QtMaterialStatusEnum.ENABLE.getCode()));
        Map<Integer, QtReport> resultMap = Maps.newHashMap();
        //2.根据spc卡片配置的物料以及检验类型检验项,物料使用车间,生产车间,生产产线获取该时间段内已检验的任务数据
        //获取检验项快照数据
        List<Integer> ids = Lists.newArrayList();
        Set<Long> materialIds = Sets.newHashSet();
        list.forEach(report -> {
            ids.add(report.getCheckItemId());
            materialIds.add(report.getMaterialId());
        });
        Map<Integer, Set<Integer>> checkItemSnapshotSet = qtCheckItemService.checkItemSnapshotSet(ids);
        List<Integer> reportIds = Lists.newArrayList();
        list.forEach(qtReport -> {
            LambdaQueryWrapper<QtCheckTask> condition = new LambdaQueryWrapper<QtCheckTask>()
                    .eq(QtCheckTask::getMaterialId, qtReport.getMaterialId())
                    .eq(QtCheckTask::getTaskType, qtReport.getCheckType())
                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                    .ge(QtCheckTask::getFinishedTime, startTime)
                    .lt(QtCheckTask::getFinishedTime, endTime);
            //根据条件获取该检验时间段的检验任务
            List<QtCheckTask> qtCheckTaskList = qtCheckTaskService.list(condition);
            //如果该卡片存在生产车间
            if (CollUtil.isNotEmpty(qtReport.getWorkshop())) {
                qtCheckTaskList = qtCheckTaskList.stream()
                        .filter(qtCheckTask ->qtCheckTask.getProductWorkshop() != null &&  qtReport.getWorkshop()
                                .stream()
                                .map(QtBaseIdNameDTO::getId)
                                .collect(Collectors.toSet())
                                .contains(qtCheckTask.getProductWorkshop().getId()))
                        .collect(Collectors.toList());

                //如果该卡片存在生产产线
                if (CollUtil.isNotEmpty(qtReport.getProductLine())) {
                    qtCheckTaskList = qtCheckTaskList.stream()
                            .filter(qtCheckTask ->qtCheckTask.getProductLine() != null &&  qtReport.getProductLine()
                                    .stream()
                                    .map(QtBaseIdNameDTO::getId)
                                    .collect(Collectors.toSet())
                                    .contains(qtCheckTask.getProductLine().getId()))
                            .collect(Collectors.toList());
                }
            }
            //如果该卡片存在供应商
            if (CollUtil.isNotEmpty(qtReport.getSupplier())) {
                qtCheckTaskList = qtCheckTaskList.stream()
                        .filter(qtCheckTask -> qtCheckTask.getSupplier() != null && qtReport.getSupplier().stream().map(QtBaseIdNameDTO::getId).collect(Collectors.toSet()).contains(qtCheckTask.getSupplier().getId()))
                        .collect(Collectors.toList());
            }
            //如果该卡片存在物料使用车间
            if (CollUtil.isNotEmpty(qtReport.getMaterialUseWorkshop())) {
                qtCheckTaskList = qtCheckTaskList.stream()
                        .filter(qtCheckTask -> qtCheckTask.getMaterialWorkshop() != null && qtReport.getMaterialUseWorkshop().stream().map(QtBaseIdNameDTO::getId).collect(Collectors.toSet()).contains(qtCheckTask.getMaterialWorkshop().getId()))
                        .collect(Collectors.toList());
            }
            //根据检验任务获取该spc的检验项的值,并计算平均值是否大于上限值或小于下限值

            qtCheckTaskList.forEach(qtCheckTask -> {
                List<QtCheckTaskDetail> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                        .eq(QtCheckTaskDetail::getTaskId, qtCheckTask.getId())
                        .in(Objects.nonNull(qtReport.getCheckItemId()), QtCheckTaskDetail::getItemId, checkItemSnapshotSet.get(qtReport.getCheckItemId())));
                List<String> collect = results.stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                        .map(QtCheckTaskDetail::getCheckResult)
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(collect)) {
                    BigDecimal sum = BigDecimal.ZERO;
                    for (String result : collect) {
                        if (isNumber(result) || isFloatNumber(result)) {
                            BigDecimal value = new BigDecimal(result);
                            sum = sum.add(value);
                        }
                    }
                    //计算该检验项的平均值
                    BigDecimal mean = BigDecimalUtil.divide(sum, BigDecimal.valueOf(results.size()));
                    //计算上限值与下限值，并和平均值比较上限和下限（上限 = 标准值 + 上偏差，下限 = 标准值 - 下偏差）
//                    BigDecimal upLimit = BigDecimalUtil.add(qtReport.getStandardValue(), qtReport.getUpLimit());
//                    BigDecimal downLimit = BigDecimalUtil.subtract(qtReport.getStandardValue(), qtReport.getDownLimit());
                    if (qtReport.getUpLimit().compareTo(mean) > 0 || qtReport.getDownLimit().compareTo(mean) < 0) {
                        resultMap.put(qtReport.getId(), qtReport);
                        reportIds.add(qtReport.getId());
                    }
                }
            });
        });
        //根据spc配置的告警人员进行信息告警
        if (CollUtil.isNotEmpty(resultMap)) {
            //获取告警人员
            Map<Integer, List<QtReportWarnUserVO>> integerListMap = qtReportWarnUserService.mapWarnUser(reportIds);
            //获取物料名称
            Map<Long, String> materialMap = FeignOptional.of(() ->remoteSpareFeignApi.listByIdCh(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取检验项名称
            Map<Integer, QtCheckItem> itemMap = qtCheckItemService.mapCheckItem(ids);
            for (Map.Entry<Integer, QtReport> entry : resultMap.entrySet()) {
                //定义消息模板
                SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
                sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.SPC_WARN.getTitle());
                sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SPC_WARN.getCode());
                String materialName = materialMap.get(entry.getValue().getMaterialId());
                sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SPC_WARN.getTemplate(),entry.getValue().getName(),materialName,itemMap.get(entry.getValue().getCheckItemId()).getName()));
                Set<Long> userIds = Sets.newHashSet();
                integerListMap.get(entry.getValue().getId()).forEach(user->userIds.add(user.getUserId()));
                sysMsgSendDTO.setUserList(userIds);
                SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
                sysMsgAdditionDTO.setId(Long.valueOf(entry.getValue().getId()));
                sysMsgAdditionDTO.setTab(Long.valueOf(entry.getValue().getReportGroupId()));
                sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
                sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                //发送消息
                try {
                    Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if (!aBoolean){
                        log.info(entry.getValue().getName()+"：消息发送失败");
                    }
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    throw new BusinessException("消息发送失败");
                }
            }
        }
        return Boolean.TRUE;
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
        if (Objects.isNull(qureyReport)) {
            throw new BusinessException("该卡片不存在,无法编辑");
        }
        List<QtReport> qtReports = list(new LambdaQueryWrapper<QtReport>()
                .eq(QtReport::getName, dto.getName())
                .eq(QtReport::getReportGroupId, dto.getReportGroupId()));
        if (qtReports.size() > 1) {
            throw new BusinessException("该卡片名称已在该分组使用,请换个卡片名称!");
        }
        QtReport report = new QtReport();
        if (Objects.nonNull(qureyReport)) {
            BeanUtils.copyProperties(dto, report);
            report.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(dto.getTimeScopeStart()));
            LocalDate timeScopeEnd = dto.getTimeScopeEnd();
            report.setTimeScopeEnd(LocalDateTime.of(timeScopeEnd.getYear(), timeScopeEnd.getMonth()
                    , timeScopeEnd.getDayOfMonth(), 23, 59, 59));
            updateById(report);
            //修改告警人员信息
            if (PmReportConstant.TYPE_1.equals(dto.getWarnFlag())) {
                qtReportWarnUserService.updateWarnUser(dto.getWarnUserIds(), report.getId());
            } else {
                qtReportWarnUserService.remove(new LambdaQueryWrapper<QtReportWarnUser>().in(QtReportWarnUser::getReportId, report.getId()));
            }
        }
        qureyReport = getById(dto.getId());
        //编辑完后回显数据
        QtReportVO qtReportVO = new QtReportVO();
        BeanUtils.copyProperties(qureyReport, qtReportVO);
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
        Assert.notNull(report, "该报表卡片不存在");

        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(report.getGranularity());
        Assert.notNull(granularityEnum,"粒度不存在");

        QtCheckItem qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        Assert.notNull(qtCheckItem,"检验项不存在");
        //获取检验项的快照集合
        Map<Integer, Set<Integer>> snapshotSet = qtCheckItemService.checkItemSnapshotSet(Lists.newArrayList(report.getCheckItemId()));
        Set<Integer> itemIds = snapshotSet.getOrDefault(report.getCheckItemId(), Sets.newHashSet());
        itemIds.add(report.getCheckItemId());

        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(qtCheckItem.getMeasureUnit()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取单位名称
        String measureUnitName = Objects.nonNull(sysDictVO) ? sysDictVO.getName() : Strings.EMPTY;

        //查询物料名称
        QtMaterial material = qtMaterialService.getById(report.getMaterialId());
        Assert.notNull(material, "物料不存在");

        if(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(report.getCheckType())){
            //来料检
            return incomingDetailPage(dto, report, granularityEnum, qtCheckItem, itemIds, measureUnitName, material);
        }else{
           //其余检验方式
           return otherDetailPage(dto, report, granularityEnum, qtCheckItem, itemIds, measureUnitName, material);
        }
    }

    /**
     * 来料检明细detail分页显示
     * @param dto 请求参数
     * @param report spc报表
     * @param granularityEnum 粒度
     * @param qtCheckItem 检验项
     * @param itemIds 快照检验项
     * @param measureUnitName 单位名称
     * @param material 物料
     * @return 分页对象
     */
    private Page<QtSpcReportDetailVO> incomingDetailPage(QtSpcReportDetailPageDTO dto, QtReport report, QtManagerConstant.QtGranularityEnum granularityEnum, QtCheckItem qtCheckItem, Set<Integer> itemIds, String measureUnitName, QtMaterial material) {
        //1.先查询所有的业务数据
        //获取符合条件的原始数据
        List<QtIncomingCheckDTO> sourceList = qtCheckTaskService.comingList(itemIds);
        SteccnTableAssertUtil.notEmpty(sourceList, "来料检全量数据表为空");

        List<QtIncomingCheckDTO> dataList = Lists.newArrayList();
        sourceList.forEach(ele ->{
            ele.getSamplingTimeArray().forEach(inner ->{
                if(Objects.nonNull(inner)){
                    ele.setSamplingTime(inner);
                    dataList.add(ele);
                }
            });
        });
        //将时间范围根据粒度分片
        List<LocalDateTime> rangeList = ShardTimeUtil.cutDateRange(granularityEnum.getType(), report.getTimeScopeStart(), report.getTimeScopeEnd());
        //spc任务明细表的数据集合
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < rangeList.size(); i++) {
            LocalDateTime start = rangeList.get(i - 1);
            LocalDateTime end = rangeList.get(i);
            //过滤出该时间分片下的所有检验任务
            List<QtIncomingCheckDTO> conditionDataList = dataList.stream()
                    .filter(v -> LocalDateUtil.isRange(v.getSamplingTime(), start, end, true, false)).collect(Collectors.toList());
            //取平均值
            BigDecimal reduce = conditionDataList.stream().map(v -> new BigDecimal(v.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimalUtil.format(BigDecimalUtil.divide(reduce, new BigDecimal(conditionDataList.size())));

            //设置返回数据类型对象
            QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
            BeanUtils.copyProperties(report, qtSpcReportDetailVO);
            //设置主键id
            qtSpcReportDetailVO.setId(i);
            //设置开始结束分片范围
            qtSpcReportDetailVO.setStartTime(start);
            qtSpcReportDetailVO.setEndTime(end);
            //检验项id
            qtSpcReportDetailVO.setCheckItemId(qtCheckItem.getId());
            //设置检验项名称
            qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());

            //设置计量单位名称
            qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
            if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(qtCheckItem.getName())) {
                qtSpcReportDetailVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
            }

            // 创建检验项详情对象
            QtSpcReportDetailTaskAllVO qtSpcReportDetailTaskAllVO = new QtSpcReportDetailTaskAllVO();
            //获取表头
            if (Objects.nonNull(qtCheckItem.getName()) && Objects.nonNull(report.getCheckType())) {
                List<QtSpcHeadTitleVO> headerList = getHeaderList(report.getCheckType(), qtCheckItem.getName());
                if (Objects.nonNull(headerList)) {
                    qtSpcReportDetailTaskAllVO.setHeader(headerList);
                }
            }

            getIQCList(itemIds, material.getName(),conditionDataList, qtSpcReportDetailVO, qtSpcReportDetailTaskAllVO);
            qtSpcReportDetailVO.setDetailTaskAllVO(qtSpcReportDetailTaskAllVO);
            mainList.add(qtSpcReportDetailVO);
        }
        //逻辑分页返回数据
        Page<QtSpcReportDetailVO> page = LogicalPageUtil.page(mainList, dto.getCurrent(), dto.getSize());
        return page;
    }


    /**
     * 其余检验类型【非来料检】明细detail分页显示
     * @param dto 请求参数
     * @param report spc报表
     * @param granularityEnum 粒度
     * @param qtCheckItem 检验项
     * @param itemIds 快照检验项
     * @param measureUnitName 单位名称
     * @param material 物料
     * @return 分页对象
     */
    private Page<QtSpcReportDetailVO> otherDetailPage(QtSpcReportDetailPageDTO dto, QtReport report, QtManagerConstant.QtGranularityEnum granularityEnum, QtCheckItem qtCheckItem, Set<Integer> itemIds, String measureUnitName, QtMaterial material) {
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .lt(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());

        //根据条件先low出所有的检验任务数据
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        SteccnTableAssertUtil.notEmpty(qtCheckTasks,"全量数据表为空");
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType(), report.getTimeScopeStart(), report.getTimeScopeEnd());
        //spc任务明细表的数据集合
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            //过滤出该时间分片下的所有检验任务
            List<QtCheckTask> conditionDataList = qtCheckTasks.stream()
                    .filter(v -> Objects.nonNull(v.getFinishedTime()))
                    .filter(v -> LocalDateUtil.isRange(v.getFinishedTime(), start, end, true, false)).collect(Collectors.toList());

            //设置返回数据类型对象
            QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
            BeanUtils.copyProperties(report, qtSpcReportDetailVO);
            //设置主键id
            qtSpcReportDetailVO.setId(i);
            //设置开始结束分片范围
            qtSpcReportDetailVO.setStartTime(start);
            qtSpcReportDetailVO.setEndTime(end);
            //检验项id
            qtSpcReportDetailVO.setCheckItemId(qtCheckItem.getId());
            //设置检验项名称
            qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());

            //设置计量单位名称
            qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
            if (QtManagerConstant.QtChartUnit.PERCENT.getName().equals(qtCheckItem.getName())) {
                qtSpcReportDetailVO.setMeasureUnit(QtManagerConstant.QtChartUnit.PERCENT.getUnitType());
            }

            // 创建检验项详情对象
            QtSpcReportDetailTaskAllVO qtSpcReportDetailTaskAllVO = new QtSpcReportDetailTaskAllVO();
            //获取表头
            if (Objects.nonNull(qtCheckItem.getName()) && Objects.nonNull(report.getCheckType())) {
                List<QtSpcHeadTitleVO> headerList = getHeaderList(report.getCheckType(), qtCheckItem.getName());
                if (Objects.nonNull(headerList)) {
                    qtSpcReportDetailTaskAllVO.setHeader(headerList);
                }
            }

            //区分不同的检验类型
            if (QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode().equals(report.getCheckType())) {
                getMIXEDList(itemIds, material.getName(),
                        conditionDataList, qtSpcReportDetailVO, qtSpcReportDetailTaskAllVO);
            } else {
                getHSCList(itemIds, material.getName(),
                        conditionDataList, qtSpcReportDetailVO, qtSpcReportDetailTaskAllVO);
            }
            qtSpcReportDetailVO.setDetailTaskAllVO(qtSpcReportDetailTaskAllVO);
            mainList.add(qtSpcReportDetailVO);
        }
        //逻辑分页返回数据
        Page<QtSpcReportDetailVO> page = LogicalPageUtil.page(mainList, dto.getCurrent(), dto.getSize());
        return page;
    }


    private void getHSCList(Set<Integer> itemIds, String name, List<QtCheckTask> conditionDataList, QtSpcReportDetailVO qtSpcReportDetailVO, QtSpcReportDetailTaskAllVO qtSpcReportDetailTaskAllVO) {
        //如果conditionDataList为空则该时间段下没有检验任务,检验结果平均值设置为0,检验详情数据为空
        List<QtSpcReportHSCDetailTaskVO> qtSpcReportHSCDetailTaskVOS = Lists.newArrayList();
        BigDecimal resultAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
        if (CollUtil.isEmpty(conditionDataList)) {
            qtSpcReportDetailVO.setResultAverage(resultAverage);
            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(qtSpcReportHSCDetailTaskVOS);
        } else if (CollUtil.isNotEmpty(conditionDataList)) {
            List<BigDecimal> mainDecimals = Lists.newArrayList();
            //遍历该时间段下的检验任务设置详情
            conditionDataList.forEach(s -> {
                QtSpcReportHSCDetailTaskVO taskVO = new QtSpcReportHSCDetailTaskVO();
                //详情检验结果
                BeanUtils.copyProperties(s, taskVO);
                //设置物料名称
                if (Objects.nonNull(name)) {
                    taskVO.setMaterialName(name);
                }
                //设置完成时间
                taskVO.setFinishedTime(LocalDateUtil.format(s.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                //检验任务样本结果集合
                List<String> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                        .eq(QtCheckTaskDetail::getTaskId, s.getId())
                        .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                        .stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                        .map(QtCheckTaskDetail::getCheckResult)
                        .collect(Collectors.toList());
                List<BigDecimal> resultList = Lists.newArrayList();
                if (CollUtil.isNotEmpty(results)) {
                    results.forEach(re -> {
                        if (isNumber(re) || isFloatNumber(re)) {
                            BigDecimal bigDecimal = new BigDecimal(re);
                            if (Objects.nonNull(bigDecimal)) {
                                resultList.add(bigDecimal);
                            }
                        }
                    });
                }
                if (CollUtil.isNotEmpty(resultList)) {
                    BigDecimal detailSum = resultList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal result = BigDecimalUtil.divide(detailSum, BigDecimal.valueOf(resultList.size()));
                    taskVO.setCheckResult(result.toString());
                    mainDecimals.add(result);
                } else {
                    taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                }
                taskVO.setProductLine(s.getProductLine());
                qtSpcReportHSCDetailTaskVOS.add(taskVO);
            });
            if (CollUtil.isNotEmpty(mainDecimals)) {
                BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));
                qtSpcReportDetailVO.setResultAverage(resultAverage);
            } else {
                qtSpcReportDetailVO.setResultAverage(new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS));
            }
            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(qtSpcReportHSCDetailTaskVOS);
        }
    }

    private void getMIXEDList(Set<Integer> itemIds, String name, List<QtCheckTask> conditionDataList, QtSpcReportDetailVO qtSpcReportDetailVO, QtSpcReportDetailTaskAllVO qtSpcReportDetailTaskAllVO) {
        //如果conditionDataList为空则该时间段下没有检验任务,检验结果平均值设置为0,检验详情数据为空
        List<QtSpcReportSamplingDetailTaskVO> qtSamplingDetailVOS = Lists.newArrayList();
        BigDecimal resultAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
        if (CollUtil.isEmpty(conditionDataList)) {
            qtSpcReportDetailVO.setResultAverage(resultAverage);
            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(qtSamplingDetailVOS);
        } else if (CollUtil.isNotEmpty(conditionDataList)) {
            List<BigDecimal> mainDecimals = Lists.newArrayList();
            //遍历该时间段下的检验任务设置详情
            conditionDataList.forEach(s -> {
                QtSpcReportSamplingDetailTaskVO taskVO = new QtSpcReportSamplingDetailTaskVO();
                //详情检验结果平均值
                BeanUtils.copyProperties(s, taskVO);
                //设置物料名称
                if (Objects.nonNull(name)) {
                    taskVO.setMaterialName(name);
                }
                //设置完成时间
                taskVO.setFinishedTime(LocalDateUtil.format(s.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                //检验任务样本结果集合
                List<String> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                        .eq(QtCheckTaskDetail::getTaskId, s.getId())
                        .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                        .stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                        .map(QtCheckTaskDetail::getCheckResult)
                        .collect(Collectors.toList());
                List<BigDecimal> resultList = Lists.newArrayList();
                if (CollUtil.isNotEmpty(results)) {
                    results.forEach(re -> {
                        if (isNumber(re) || isFloatNumber(re)) {
                            BigDecimal bigDecimal = new BigDecimal(re);
                            if (Objects.nonNull(bigDecimal)) {
                                resultList.add(bigDecimal);
                            }
                        }
                    });
                }
                if (CollUtil.isNotEmpty(resultList)) {
                    BigDecimal detailSum = resultList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal result = BigDecimalUtil.divide(detailSum, BigDecimal.valueOf(resultList.size()));
                    taskVO.setCheckResult(result.toString());
                    mainDecimals.add(result);
                } else {
                    taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                }
                taskVO.setWorkshop(s.getMaterialWorkshop());
                qtSamplingDetailVOS.add(taskVO);
            });
            if (CollUtil.isNotEmpty(mainDecimals)) {
                BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));
                qtSpcReportDetailVO.setResultAverage(resultAverage);
            } else {
                qtSpcReportDetailVO.setResultAverage(new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS));
            }
            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(qtSamplingDetailVOS);
        }
    }

    /**
     * 获取来料检
     * @param itemIds
     * @param name
     * @param conditionDataList
     * @param qtSpcReportDetailVO
     * @param qtSpcReportDetailTaskAllVO
     */
    private void getIQCList(Set<Integer> itemIds, String name, List<QtIncomingCheckDTO> conditionDataList, QtSpcReportDetailVO qtSpcReportDetailVO, QtSpcReportDetailTaskAllVO qtSpcReportDetailTaskAllVO) {
        //如果conditionDataList为空则该时间段下没有检验任务,检验结果平均值设置为0,检验详情数据为空
        List<QtSpcReportIQCDetailTaskVO> reportDetailTaskVOS = Lists.newArrayList();
        BigDecimal resultAverage = new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS);
        if (CollUtil.isEmpty(conditionDataList)) {
            qtSpcReportDetailVO.setResultAverage(resultAverage);
            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(reportDetailTaskVOS);
        } else {
            List<BigDecimal> mainDecimals = Lists.newArrayList();
            //遍历该时间段下的检验任务设置详情
            conditionDataList.forEach(s -> {
                QtSpcReportIQCDetailTaskVO taskVO = new QtSpcReportIQCDetailTaskVO();
                //详情检验结果值
                BeanUtils.copyProperties(s, taskVO);
                //设置物料名称
                if (Objects.nonNull(name)) {
                    taskVO.setMaterialName(name);
                }

                //设置完成时间
                taskVO.setFinishedTime(LocalDateUtil.format(s.getSamplingTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                //检验任务样本结果集合
                List<String> results = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                        .eq(QtCheckTaskDetail::getTaskId, s.getTaskId())
                        .in(CollUtil.isNotEmpty(itemIds), QtCheckTaskDetail::getItemId, itemIds))
                        .stream().filter(qt -> Objects.nonNull(qt) && Objects.nonNull(qt.getCheckResult()) && StrUtil.isNotBlank(qt.getCheckResult()))
                        .map(QtCheckTaskDetail::getCheckResult)
                        .collect(Collectors.toList());
                List<BigDecimal> resultList = Lists.newArrayList();
                if (CollUtil.isNotEmpty(results)) {
                    results.forEach(re -> {
                        if (isNumber(re) || isFloatNumber(re)) {
                            BigDecimal bigDecimal = new BigDecimal(re);
                            if (Objects.nonNull(bigDecimal)) {
                                resultList.add(bigDecimal);
                            }
                        }
                    });
                }
                if (CollUtil.isNotEmpty(resultList)) {
                    BigDecimal detailSum = resultList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal result = BigDecimalUtil.divide(detailSum, BigDecimal.valueOf(resultList.size()));
                    taskVO.setCheckResult(result.toString());
                    mainDecimals.add(result);
                } else {
                    taskVO.setCheckResult(QtManagerConstant.THE_CALCULATION_RESULTS);
                }
                taskVO.setWorkshop(s.getMaterialWorkshop());
                taskVO.setSupplier(s.getSupplier());
                reportDetailTaskVOS.add(taskVO);
            });
            if (CollUtil.isNotEmpty(mainDecimals)) {
                BigDecimal sum = mainDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                resultAverage = BigDecimalUtil.divide(sum, BigDecimal.valueOf(mainDecimals.size()));
                qtSpcReportDetailVO.setResultAverage(resultAverage);
            } else {
                qtSpcReportDetailVO.setResultAverage(new BigDecimal(QtManagerConstant.THE_CALCULATION_RESULTS));
            }

            qtSpcReportDetailTaskAllVO.setReportDetailTaskVOList(reportDetailTaskVOS);
        }
    }

    /**
     * 根据检验类型获取各自对应的检验任务详情表头
     *
     * @param dataType
     * @return
     */
    private List<QtSpcHeadTitleVO> getHeaderList(Integer dataType, String name) {
        List<QtSpcHeadTitleVO> qtSpcHeadTitleVOS = Lists.newArrayList();
        //检验任务单号
        QtSpcHeadTitleVO qtSpcHeadTitleVO = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FIELD_ORDER,QtManagerConstant.SPC_HEADER_FIELD_ORDER_VALUE);
        //检验完成时间
        QtSpcHeadTitleVO qtSpcHeadTitleVO2 = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME,QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME_VALUE);
        //检验项
        QtSpcHeadTitleVO qtSpcHeadTitleVO3 = new QtSpcHeadTitleVO(name,QtManagerConstant.SPC_HEADER_FIELD_FINISHED_CHECK_RESULT_VALUE);
        //物料名称
        QtSpcHeadTitleVO qtSpcHeadTitleVO4 = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME,QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME_VALUE);
        //生产产线
        QtSpcHeadTitleVO qtSpcHeadTitleVO5 = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE,QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE_VALUE);
        //物料使用车间
        QtSpcHeadTitleVO qtSpcHeadTitleVO6 = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FIELD_WORKSHOP,QtManagerConstant.SPC_HEADER_FIELD_WORKSHOP_VALUE);
        //供应商
        QtSpcHeadTitleVO qtSpcHeadTitleVO7 = new QtSpcHeadTitleVO(QtManagerConstant.SPC_HEADER_FILELD_SUPPLIER,QtManagerConstant.SPC_HEADER_FILELD_SUPPLIER_VALUE);
        //所有检都需要【检验任务单号，检验完成时间，检验项，物料名称】
        Collections.addAll(qtSpcHeadTitleVOS, qtSpcHeadTitleVO, qtSpcHeadTitleVO2, qtSpcHeadTitleVO3, qtSpcHeadTitleVO4);
        if (QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(dataType)) {
            //来料检需要加入【物料使用车间，供应商】
            Collections.addAll(qtSpcHeadTitleVOS, qtSpcHeadTitleVO6, qtSpcHeadTitleVO7);
        } else if (QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode().equals(dataType)) {
            //配料抽检需要加入【物料使用车间】
            qtSpcHeadTitleVOS.add(qtSpcHeadTitleVO6);
        } else {
            //其余检需要加入【生产产线】
            qtSpcHeadTitleVOS.add(qtSpcHeadTitleVO5);
        }
        return qtSpcHeadTitleVOS;
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


    /**
     * 判断是否为非负浮点数
     *
     * @param str
     * @return
     */
    public static boolean isFloatNumber(String str) {
        Pattern pattern = Pattern.compile(QtManagerConstant.REGULAR_NON_FLOATING_POINT_NUMBER);
        Matcher num = pattern.matcher(str);
        if (!num.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为整数
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile(QtManagerConstant.REGULAR_NON_NUMBER);
        Matcher num = pattern.matcher(str);
        if (!num.matches()) {
            return false;
        }
        return true;
    }
}