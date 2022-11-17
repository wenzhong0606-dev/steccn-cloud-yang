package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.PageUtil;
import com.alibaba.excel.EasyExcel;
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
import com.steccn.pms.vo.MaterialRequisitionPlanVO;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.util.CollectorsUtil;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.util.ShardingTimeUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * 新建报表卡片
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtReportDTO dto) {
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto, qtReport);
        QtReport report = getOne(new LambdaQueryWrapper<QtReport>().eq(QtReport::getName, dto.getName()));
        Assert.isNull(report, "该报表卡片已存在,请勿重复创建!");
        qtReport.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(dto.getTimeScopeStart()));
        qtReport.setTimeScopeEnd(LocalDateUtil.getEndTimeOfDayStr(dto.getTimeScopeEnd()));
        //根据分组id去查询分组
        this.save(qtReport);
        return Boolean.TRUE;
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
        //获取检验项id集合
        List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>().
                eq(QtCheckPlanGroupItem::getGroupId, groupid));
        List<Integer> checkItemIds = qtCheckPlanGroupItems.stream().map(QtCheckPlanGroupItem::getCheckItemId).collect(Collectors.toList());
        if (CollUtil.isEmpty(checkItemIds)) {
            throw new BusinessException("该检验分组下没有检验项");
        }
        //创建返回检验项的集合
        List<QtCheckItemSPCVO> qtCheckItemSPCVOS = Lists.newArrayList();
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
        //根据检验项id去check_plan_group_item数据库中查询
        QtCheckPlanGroupItem qtCheckPlanGroupItem = qtCheckPlanGroupItemService
                .getOne(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                .eq(QtCheckPlanGroupItem::getCheckItemId, itemId)
                .eq(QtCheckPlanGroupItem::getGroupId, groupId));
        //根据id查询检验项
        QtCheckItem qtCheckItem = qtCheckItemService.getById(itemId);
        Set<Long> measureUnitSet = new HashSet<>();
        measureUnitSet.add(qtCheckItem.getMeasureUnit());
        Map<Long, String> dictData = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureUnitSet)).get();
        String measureName = dictData.get(qtCheckItem.getMeasureUnit());
        qtCheckItemUnitVO.setMeasureUnitName(measureName);
//        qtCheckItemUnitVO.setLowerValue(qtCheckItem.get);
        if (Objects.isNull(qtCheckPlanGroupItem)) {
            return qtCheckItemUnitVO;
        } else {
            BeanUtils.copyProperties(qtCheckPlanGroupItem, qtCheckItemUnitVO);
            return qtCheckItemUnitVO;
        }
    }


    /**
     * 下载模板
     *
     * @param response
     */
    @Override
    public void download(HttpServletResponse response) throws IOException {
        downloadExcel(response, new ArrayList<>());
    }

    /**
     * 下载SPC报表excel
     *
     * @param response
     * @param list
     * @throws IOException
     */
    private void downloadExcel(HttpServletResponse response, List<QtReportExcelDTO> list) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("报表卡片名称", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), QtReportExcelDTO.class).sheet("模板").doWrite(list);
    }

    /**
     * 根据分组id查询报表图表列表
     *
     * @param id
     * @return
     */
    @Override
    public List<QtReportChartVO> getQtReportList(Integer reportGroupId) {
        //创建返回前端的集合
        List<QtReportChartVO> qtReportChartVOlist = Lists.newArrayList();
        if (Objects.isNull(reportGroupId)) {
            return qtReportChartVOlist;
        }
        List<QtReport> qtReportList = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, reportGroupId)
                .orderByDesc(QtReport::getId));
        //List<Long> materialIds= qtReportList.stream().map(QtReport::getMaterialId).collect(Collectors.toList());
        if (CollUtil.isEmpty(qtReportList)) {
            return qtReportChartVOlist;
        }
        List<String> spcfilter = Lists.newArrayList();
        Set<Long> measureIds = Sets.newHashSet();
        //遍历查出来的集合封装数据`
        qtReportList.forEach(q -> {
            QtReportChartVO qtReportChartVO = new QtReportChartVO();
            BeanUtils.copyProperties(q, qtReportChartVO);
            qtReportChartVO.setReportCardType(q.getReportCardType());
            qtReportChartVO.setId(q.getId());
            qtReportChartVO.setMaterialId(q.getMaterialId());
            //根据物料id查询物料
            QtMaterial material=new QtMaterial();
            if (Objects.nonNull(q.getMaterialId())){
                 material= qtMaterialService.getById(q.getMaterialId());
            }
            //设置物料名称
            qtReportChartVO.setMaterialName(material.getName());
            QtCheckItem qtCheckItem = new QtCheckItem();
            if (Objects.nonNull(q.getCheckItemId())){
                 qtCheckItem = qtCheckItemService.getById(q.getCheckItemId());
            }
            Collections.addAll(spcfilter,material.getName(), qtCheckItem.getName());
            //根据检验项id去获取计量单位
            measureIds.add(qtCheckItem.getMeasureUnit());
            R<Map<Long, String>> mapR = remoteDictFeignApi.listByIdCh(measureIds);
            String measureName = mapR.getData().get(qtCheckItem.getMeasureUnit());
            qtReportChartVO.setMeasureUnit(measureName);

            //根据条件查出所有的任务
            List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                            .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                            .eq(QtCheckTask::getTaskType, q.getCheckType())
                            .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                            .ge(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                            .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
                            .orderByAsc(QtCheckTask::getFinishedTime)
                    //.and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),q.getCustomer())
            );


            //设置开始结束时间
            qtReportChartVO.setTimeScopeStart(q.getTimeScopeStart().toLocalDate());
            qtReportChartVO.setTimeScopeEnd(q.getTimeScopeEnd().toLocalDate());
            //设置时间分片
            List<String> dates = Lists.newArrayList();
            QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(q.getGranularity());
            Assert.notNull(granularityEnum,"粒度不存在");
            //将时间范围根据粒度分片
            List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                    , q.getTimeScopeStart()
                    , q.getTimeScopeEnd());
            //设置折线图数据
            List<String> itemValue = new ArrayList<>();
            for (int i = 1; i < localDateTimes.size(); i++) {
                LocalDateTime start = localDateTimes.get(i - 1);
                LocalDateTime end = localDateTimes.get(i);
//                LocalDateUtil.format(start,)
                //过滤出时间分片内的数据
                List<QtCheckTask> conditionTasks = qtCheckTasks.stream().filter(v -> Objects.nonNull(v.getFinishedTime())).filter(v -> {
                    //数据范围应该是大于等于开始时间  小于结束时间
                    if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }).collect(Collectors.toList());
                List<Long> taskids = conditionTasks.stream().map(QtCheckTask::getId).collect(Collectors.toList());
              //如果id集合为空该时间段下没有
                if (CollUtil.isEmpty(taskids)){
                    itemValue.add("0");
                }else {
                    List<String> stringList = qtCheckTaskDetailService
                            .list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .in(QtCheckTaskDetail::getTaskId, taskids)).stream()
                            .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                    List<BigDecimal> bigDecimals = Lists.newArrayList();

                    stringList.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                        BigDecimal bigDecimal = new BigDecimal(s);
                        bigDecimals.add(bigDecimal);
                    });

                    BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                    itemValue.add(average.toString());
                }
                dates.add(start.toString());
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
        BeanUtils.copyProperties(q, qtReportChartVO);
        qtReportChartVO.setReportCardType(q.getReportCardType());
        qtReportChartVO.setMaterialId(q.getMaterialId());
        //根据物料id查询物料
        QtMaterial material=new QtMaterial();
        if (Objects.nonNull(q.getMaterialId())){
            material= qtMaterialService.getById(q.getMaterialId());
        }
        //设置物料名称
        qtReportChartVO.setMaterialName(material.getName());
        QtCheckItem qtCheckItem = new QtCheckItem();
        if (Objects.nonNull(q.getCheckItemId())){
            qtCheckItem = qtCheckItemService.getById(q.getCheckItemId());
        }
//        List<String> spcfilter = Lists.newArrayList();
//        Collections.addAll(spcfilter,material.getName(), qtCheckItem.getName());
//        qtReportChartVO.setSpcFilter(spcfilter);
        //根据检验项id去获取计量单位
        Set<Long> measureIds = Sets.newHashSet();
        measureIds.add(qtCheckItem.getMeasureUnit());
        Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureIds)).get();
        String measureName = longStringMap.get(qtCheckItem.getMeasureUnit());
        qtReportChartVO.setMeasureUnit(measureName);

        //根据条件查出所有的任务
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                        .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                        .eq(QtCheckTask::getTaskType, q.getCheckType())
                        .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                        .ge(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                        .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
                        .orderByAsc(QtCheckTask::getFinishedTime)
                //.and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),q.getCustomer())
        );

        //设置开始结束时间
        qtReportChartVO.setTimeScopeStart(q.getTimeScopeStart().toLocalDate());
        qtReportChartVO.setTimeScopeEnd(q.getTimeScopeEnd().toLocalDate());
        //设置时间分片
        List<String> dates = Lists.newArrayList();
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(q.getGranularity());
        Assert.notNull(granularityEnum,"粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , q.getTimeScopeStart()
                , q.getTimeScopeEnd());
        //设置折线图数据
        List<String> itemValue = new ArrayList<>();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
//                LocalDateUtil.format(start,)
            //过滤出时间分片内的数据
            List<QtCheckTask> conditionTasks = qtCheckTasks.stream().filter(v -> Objects.nonNull(v.getFinishedTime())).filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            List<Long> taskids = conditionTasks.stream().map(QtCheckTask::getId).collect(Collectors.toList());
            //如果id集合为空该时间段下没有
            if (CollUtil.isEmpty(taskids)){
                itemValue.add("0");
            }else {
                List<String> stringList = qtCheckTaskDetailService
                        .list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                .in(QtCheckTaskDetail::getTaskId, taskids)).stream()
                        .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                List<BigDecimal> bigDecimals = Lists.newArrayList();

                stringList.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                    BigDecimal bigDecimal = new BigDecimal(s);
                    bigDecimals.add(bigDecimal);
                });

                BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
                itemValue.add(average.toString());
            }
            dates.add(start.toString());
        }

        qtReportChartVO.setXData(dates);
        qtReportChartVO.setItemValue(itemValue);
        return qtReportChartVO;
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
     * @param qtReportDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean edit(QtReportDTO dto) {
        LambdaQueryWrapper<QtReport> eq = new LambdaQueryWrapper<QtReport>().eq(QtReport::getId, dto.getId());
        QtReport report = new QtReport();
        if (Objects.nonNull(eq)) {
            BeanUtils.copyProperties(dto, report);
        }
         updateById(report);
         return Boolean.TRUE;
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
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>()

                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .le(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());

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
        //根据条件先low出所有的数据
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(condition);
        //设置时间分片
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(report.getGranularity());
        Assert.notNull(granularityEnum,"粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> localDateTimes = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , report.getTimeScopeStart()
                , report.getTimeScopeEnd());

        //3.定义分页对象
        Page<QtSpcReportDetailVO> page = new Page<>();
        //4.查询分页数据
        List<QtCheckTask> dataList = qtCheckTaskService.list(condition);
        //5.拷贝page信息
//        BeanUtils.copyProperties(resPage, page);
//        //6.拷贝返回的数据
//        List<QtSpcReportDetailVO> qtSpcReportDetailVOList = Lists.newArrayList();
//        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);

        //根据检验项id获取检验项名称
        QtCheckItem qtCheckItem=new QtCheckItem();
        if (Objects.nonNull(report.getCheckItemId())){
            qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        }
        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        Set<Long> ids = Sets.newHashSet();
        ids.add(qtCheckItem.getMeasureUnit());
        Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
        //获取单位名称
        String measureUnitName = longStringMap.get(qtCheckItem.getMeasureUnit());

        //设置物料名称
        QtMaterial qtMaterial=new QtMaterial();
        if (Objects.nonNull(report.getMaterialId())) {
            qtMaterial= qtMaterialService.getById(report.getMaterialId());
        }
        String name = qtMaterial.getName();
        List<QtSpcReportDetailVO> mainList = Lists.newArrayList();
        for (int i = 1; i < localDateTimes.size(); i++) {
            LocalDateTime start = localDateTimes.get(i - 1);
            LocalDateTime end = localDateTimes.get(i);
            List<QtCheckTask> conditionDataList = qtCheckTasks.stream().filter(v->Objects.nonNull(v.getFinishedTime())).filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            List<Long> taskids = conditionDataList.stream().map(QtCheckTask::getId).collect(Collectors.toList());
            //如果id集合为空该时间段下没有
            BigDecimal average =new BigDecimal("0");
            List<QtSpcReportDetailTaskVO> qtSpcReportDetailTaskVOS1 = Lists.newArrayList();
            conditionDataList.forEach(ele ->{
                QtSpcReportDetailTaskVO taskVO = new QtSpcReportDetailTaskVO();
                BeanUtils.copyProperties(ele,taskVO);
                taskVO.setFinishedTime(ele.getFinishedTime().toLocalDate());
                taskVO.setMaterialName(name);
//                QtCheckTaskDetail qtCheckTaskDetail = qtCheckTaskDetailService.getOne(new LambdaQueryWrapper<QtCheckTaskDetail>()
//                        .eq(QtCheckTaskDetail::getTaskId, ele.getId())
//                        .eq(QtCheckTaskDetail::getGroupId, report.getCheckGroupId())
//                        .eq(QtCheckTaskDetail::getItemId, report.getCheckItemId()));
//                taskVO.setCheckResult(qtCheckTaskDetail.getCheckResult());
                taskVO.setId(ele.getId());
                taskVO.setCustomer(ele.getCustomer());
                 qtSpcReportDetailTaskVOS1.add(taskVO);
            });

           // List<QtSpcReportDetailTaskVO> qtSpcReportDetailTaskVOS1 = BeanUtil.copyToList(conditionDataList, QtSpcReportDetailTaskVO.class);

            //设置详情列表返回集合
            List<QtSpcReportDetailTaskVO> qtSpcReportDetailTaskVOS= Lists.newArrayList();
            if (CollUtil.isNotEmpty(taskids)){
                List<String> stringList = qtCheckTaskDetailService
                        .list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                .in(QtCheckTaskDetail::getTaskId, taskids)).stream()
                        .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
                List<BigDecimal> bigDecimals = Lists.newArrayList();
                stringList.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                    BigDecimal bigDecimal = new BigDecimal(s);
                    bigDecimals.add(bigDecimal);
                    QtSpcReportDetailTaskVO qtSpcReportDetailTaskVO = new QtSpcReportDetailTaskVO();
                    BeanUtils.copyProperties(s, qtSpcReportDetailTaskVO);
                    qtSpcReportDetailTaskVOS.add(qtSpcReportDetailTaskVO);
                });
                BigDecimal sum = bigDecimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                 average = BigDecimalUtil.divide(sum, BigDecimal.valueOf(bigDecimals.size()));
            }

            //设置返回数据类型对象
            QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
            qtSpcReportDetailVO.setCheckItemResultAverage(average);

            //设置开始结束分片范围
            qtSpcReportDetailVO.setStartTime(start);
            qtSpcReportDetailVO.setEndTime(end);
            //设置检验项名称
            qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());
            //设置计量单位名称
            qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
            //设置标准值
            qtSpcReportDetailVO.setStandardValue(report.getStandardValue());
            //设置上限
            qtSpcReportDetailVO.setUpLimit(report.getUpLimit());
            //设置下限
            qtSpcReportDetailVO.setDownLimit(report.getDownLimit());

            // 设置每个检验项下面的检验任务根据分片时间分好片的任务详情
            qtSpcReportDetailVO.setReportDetailTaskVOList(qtSpcReportDetailTaskVOS1);

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

    /**
     * 获取时间分片
     *
     * @param type
     * @param start
     * @param end
     * @return
     */
  /*  private List<String> getDateStringList(String type, String start, String end) {
        List<String> dates = Lists.newArrayList();
        try {
            dates = ShardingTimeUtil.cutDate(type, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }*/
}




