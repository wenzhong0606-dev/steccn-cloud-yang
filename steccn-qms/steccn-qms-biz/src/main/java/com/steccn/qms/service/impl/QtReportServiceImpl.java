package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
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
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
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
import java.net.URLEncoder;
import java.util.*;
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
        QtCheckPlanGroupItem qtCheckPlanGroupItem = qtCheckPlanGroupItemService.getOne(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                .eq(QtCheckPlanGroupItem::getCheckItemId, itemId)
                .eq(QtCheckPlanGroupItem::getGroupId, groupId));
        //根据id查询检验项
        QtCheckItem qtCheckItem = qtCheckItemService.getById(itemId);
        Set<Long> measureUnitSet = new HashSet<>();
        measureUnitSet.add(qtCheckItem.getMeasureUnit());
        Map<Long, String> dictData = FeignOptional.of(remoteDictFeignApi.listByIdCh(measureUnitSet)).get();
        String measureName = dictData.get(qtCheckItem.getMeasureUnit());
        qtCheckItemUnitVO.setMeasureUnitName(measureName);
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
    //TODO:根据分组id查询报表图表列表
    @Override
    public List<QtReportChartVO> getQtReportList(Integer reportGroupId) {
        //创建返回前端的集合
        List<QtReportChartVO> qtReportChartVOlist = Lists.newArrayList();
        if (Objects.isNull(reportGroupId)) {
            throw new BusinessException("分组id不能为空");
        }
        List<QtReport> qtReportList = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, reportGroupId)
                .orderByDesc(QtReport::getId));
        //List<Long> materialIds= qtReportList.stream().map(QtReport::getMaterialId).collect(Collectors.toList());
        if (CollUtil.isEmpty(qtReportList)) {
            return qtReportChartVOlist;
        }
        List<String> spcfilter = Lists.newArrayList();
        Set<Long> measureIds = Sets.newHashSet();
        //遍历查出来的集合封装数据
        qtReportList.forEach(q -> {
            QtReportChartVO qtReportChartVO = new QtReportChartVO();
            BeanUtils.copyProperties(q, qtReportChartVO);
            //根据物料id查询物料
            QtMaterial material = qtMaterialService.getById(q.getMaterialId());
            qtReportChartVO.setMaterialName(material.getName());
            Collections.addAll(spcfilter, material.getName(), q.getCheckItemId().toString());
            qtReportChartVO.setSpcFilter(spcfilter);
            //根据检验项id去获取计量单位
            QtCheckItem qtCheckItem = qtCheckItemService.getById(q.getCheckItemId());
            measureIds.add(qtCheckItem.getMeasureUnit());
            R<Map<Long, String>> mapR = remoteDictFeignApi.listByIdCh(measureIds);
            String measureName = mapR.getData().get(qtCheckItem.getMeasureUnit());
            qtReportChartVO.setMaterialName(measureName);

            //设置时间分片
            String start = LocalDateUtil.format(q.getTimeScopeStart(),DatePattern.NORM_DATETIME_PATTERN);
            String end = LocalDateUtil.format(q.getTimeScopeEnd(),DatePattern.NORM_DATETIME_PATTERN);
            List<String> dates = Lists.newArrayList();

            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.DAY.getName())) {
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.DAY.getEname(), start, end);

            }
            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.WEEK.getName())) {
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.WEEK.getEname(), start, end);
            }
            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.MONTH.getName())) {
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.MONTH.getEname(), start, end);
            }
            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.QUARTER.getName())) {
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.QUARTER.getEname(), start, end);

            }
            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.YEAR.getName())) {
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.YEAR.getEname(), start, end);

            }
            if (q.getGranularity().equals(QtManagerConstant.QtSPCGranularityEnum.HOUR.getName())) {
                start = LocalDateUtil.format(q.getTimeScopeStart(),DatePattern.NORM_DATETIME_PATTERN);
                end = LocalDateUtil.format(q.getTimeScopeEnd(),DatePattern.NORM_DATETIME_PATTERN);
                dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.HOUR.getEname(), start, end);

            }

            qtReportChartVO.setXData(dates);
            //获取任务id列表
            List<Long> qtTaskIds = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                            .eq(QtCheckTask::getMaterialId, q.getMaterialId())
                            .eq(QtCheckTask::getTaskType, q.getCheckType())
                            .gt(QtCheckTask::getFinishedTime, q.getTimeScopeStart())
                            .lt(QtCheckTask::getFinishedTime, q.getTimeScopeEnd())
                    //.and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),q.getCustomer())
            ).stream().map(QtCheckTask::getId).collect(Collectors.toList());

            //获取任务结果
            if (CollUtil.isEmpty(qtTaskIds)) {
                throw new BusinessException("暂无任务");
            }
            List<String> result = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .in(QtCheckTaskDetail::getTaskId, qtTaskIds)).stream()
                    .map(QtCheckTaskDetail::getCheckResult).collect(Collectors.toList());
            qtReportChartVO.setItemValue(result);
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
    public Boolean edit(QtReportDTO qtReportDTO) {
        LambdaQueryWrapper<QtReport> eq = new LambdaQueryWrapper<QtReport>().eq(QtReport::getId, qtReportDTO.getId());
        QtReport report = new QtReport();
        if (Objects.nonNull(eq)) {
            BeanUtils.copyProperties(qtReportDTO, report);
        }
        report.setTimeScopeStart(LocalDateUtil.getStartTimeOfDayStr(qtReportDTO.getTimeScopeStart()));
        report.setTimeScopeEnd(LocalDateUtil.getEndTimeOfDayStr(qtReportDTO.getTimeScopeEnd()));
        return updateById(report);
    }

    /**
     * spc控制图明细
     *
     * @param dto
     * @return
     */
    //TODO:spc控制图明细
    @Override
    public Page<QtSpcReportDetailVO> pageList(QtSpcReportDetailPageDTO dto) {
        //根据报表卡片id先查找到spc数据库中的报表数据
        QtReport report = getById(dto.getId());
        //通过检验分组id以及检验项id去任务明细表里面获取任务id集合
        List<Long> taskIdList = Lists.newArrayList();
        if (Objects.nonNull(report)) {
            taskIdList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getGroupId, report.getCheckGroupId())
                            .eq(QtCheckTaskDetail::getItemId, report.getCheckItemId()))
                    .stream().map(QtCheckTaskDetail::getTaskId).collect(Collectors.toList());
        }
        //1.去任务表里面根据时间以及id获取已经完成的检验项并查询检验结果

        if (CollUtil.isEmpty(taskIdList)) {
            throw new BusinessException("该分组及其检验项下没有检验任务");
        }
        //设置查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) as type," +
                        "any_value(product_order) as product_order , any_value(product_batch) product_batch ,any_value(check_plan_id) as check_plan_id , " +
                        "any_value(status) as status , any_value(check_num) as check_num ,any_value(good_num) as good_num , " +
                        "any_value(bad_num) as bad_num , any_value(result) as result ,any_value(note) as note , any_value(supplier) as supplier , " +
                        "any_value(customer) as customer")
                .lambda().in(QtCheckTask::getId, taskIdList)
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .le(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());
        //2.解析排序规则
        if ((QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME.equals(dto.getSortField()))) {
            if (dto.getAscFlag()) {
                //使用last把sql用于排序的中文函数代码语句拼接到最后
                condition.last("ORDER BY CONVERT(finished_time using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(finished_time using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialId);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductLine);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getCustomerName);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getSupplierName);
        } else {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialWorkshopName);
        }

        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<QtCheckTask> resPage = qtCheckTaskService.page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的数据
        List<QtSpcReportDetailVO> qtSpcReportDetailVOList = Lists.newArrayList();
        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);

        //把时间范围分片
        String start = LocalDateUtil.format(report.getTimeScopeStart(), DatePattern.NORM_DATETIME_PATTERN);
        String end = LocalDateUtil.format(report.getTimeScopeEnd(), DatePattern.NORM_DATETIME_PATTERN);
       List<String> dateAll= Lists.newArrayList();
        List<String> dates = Lists.newArrayList();
        if (QtManagerConstant.QtSPCGranularityEnum.HOUR.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.HOUR.getEname(), start, end);
            dateAll.add(start);
        }
        if (QtManagerConstant.QtSPCGranularityEnum.DAY.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.DAY.getEname(), start, end);
        }
        if (QtManagerConstant.QtSPCGranularityEnum.WEEK.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.WEEK.getEname(), start, end);
        }
        if (QtManagerConstant.QtSPCGranularityEnum.MONTH.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.MONTH.getEname(), start, end);
        }
        if (QtManagerConstant.QtSPCGranularityEnum.QUARTER.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.QUARTER.getEname(), start, end);
        }
        if (QtManagerConstant.QtSPCGranularityEnum.YEAR.getName().equals(report.getGranularity())) {
            dates = getDateStringList(QtManagerConstant.QtSPCGranularityEnum.YEAR.getEname(), start, end);
        }

        //把初始时间添加进dateAll
        if (QtManagerConstant.QtSPCGranularityEnum.HOUR.getName().equals(report.getGranularity())) {
            dateAll.add(start);
        }else {
            String startFormat = LocalDateUtil.localDateFormat(report.getTimeScopeStart().toLocalDate(), DatePattern.NORM_DATE_PATTERN);
            dateAll.add(startFormat);
        }
        dates.forEach(d->{
            dateAll.add(d);
        });
        //根据检验项id获取检验项名称
        QtCheckItem qtCheckItem = qtCheckItemService.getById(report.getCheckItemId());
        //根据检验项里面计量id去数据字典dict数据库获取计量单位名称
        Set<Long> ids = Sets.newHashSet();
        ids.add(qtCheckItem.getMeasureUnit());
        R<Map<Long, String>> mapR = remoteDictFeignApi.listByIdCh(ids);
        Map<Long, String> data = mapR.getData();
        String measureUnitName = data.get(qtCheckItem.getMeasureUnit());


        //按照时间分片把任务的明细放在该时间段内
        dateAll.forEach(d->{
            //TODO
            //如果是小时按小时的最小时间到最大时间分片
            //如果是天按天的最小时到最大时分
            //如果是月按天月的最小时到最大时分
            //如果是季度按天季度的最小时到最大时分
            //如果是按年按年的最小时间到最大时间分

            //设置返回的数据
            qtCheckTasks.forEach(q -> {
                QtSpcReportDetailVO qtSpcReportDetailVO = new QtSpcReportDetailVO();
                QtSpcReportDetailTaskVO qtSpcReportDetailTaskVO = new QtSpcReportDetailTaskVO();
                BeanUtils.copyProperties(q, qtSpcReportDetailVO);
                //设置检验项名称
                qtSpcReportDetailVO.setCheckItemName(qtCheckItem.getName());
                //设置计量单位名称
                qtSpcReportDetailVO.setMeasureUnit(measureUnitName);
                //设置标准值
                qtSpcReportDetailVO.setStandardValue(report.getStandardValue());
                //设置上限
                qtSpcReportDetailVO.setUpLimit(report.getUpLimit());
                //设置下限
                qtSpcReportDetailVO.setDownLimit(report.getDowmLimit());

                qtSpcReportDetailVOList.add(qtSpcReportDetailVO);
            });
        });

        return page.setRecords(qtSpcReportDetailVOList);
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
    private List<String> getDateStringList(String type, String start, String end) {
        List<String> dates = Lists.newArrayList();
        try {
            dates = ShardingTimeUtil.cutDate(type, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }
}




