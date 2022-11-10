package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmProcessExcelDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.vo.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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
        if (Objects.isNull(materialId)&& Objects.isNull(checkType)){
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
        QtCheckGroupVO qtCheckGroupVO = new QtCheckGroupVO();
        qtCheckPlanGroups.forEach(q -> {
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
        QtCheckItemSPCVO qtCheckItemSPCVO = new QtCheckItemSPCVO();
        List<QtCheckItem> qtCheckItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().in(QtCheckItem::getId, checkItemIds));
        //添加检验项的id或名称
        qtCheckItems.forEach(q -> {
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
     * @param id
     * @return
     */
    @Override
    public List<QtReportChartVO> getQtReportList(Integer reportGroupId) {
        if (Objects.isNull(reportGroupId)){
            throw new BusinessException("分组id不能为空");
        }
        List<QtReport> qtReportList = list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, reportGroupId)
                .orderByDesc(QtReport::getId));
        List<Long> materialIds= qtReportList.stream().map(QtReport::getMaterialId).collect(Collectors.toList());

        List<QtMaterial> qtMaterials = qtMaterialService.list(new LambdaQueryWrapper<QtMaterial>().in(QtMaterial::getId, materialIds));
        List<QtReportChartVO> qtReportChartVOS= Lists.newArrayList();
        QtReportChartVO qtReportChartVO = new QtReportChartVO();
        qtMaterials.forEach(q->{
            qtReportChartVO.setMaterialName(q.getName());
            qtReportChartVOS.add(qtReportChartVO);
        });


        qtReportList.stream().forEach(q->{
            BeanUtils.copyProperties(q,qtReportChartVO);
            QtCheckTask qtCheckTask = qtCheckTaskService.getOne(new LambdaQueryWrapper<QtCheckTask>().eq(QtCheckTask::getMaterialId, q.getMaterialId()).eq(QtCheckTask::getTaskType, q.getCheckType()));
                  //  .and(Objects.nonNull(q.getWorkshop()),wrapper->wrapper.apply("JSON_CONTAIN(customer,JSON_OBJECT('Id',#{0})) "),id));
          //TODO
            qtReportChartVOS.add(qtReportChartVO);
        });
        return qtReportChartVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByGroupId(Integer id) {
        List<Integer> ids= list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, id))
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
    @Override
    public Page<QtSpcReportDetailVO> pageList(QtSpcReportDetailPageDTO dto) {
        //根据id先查找到spc数据库中的报表数据
        QtReport report = getById(dto.getId());
        //通过分组id以及检验项id去任务明细表里面获取任务id集合
        List<Long> taskIdList = Lists.newArrayList();
        if (Objects.nonNull(report)) {
            taskIdList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getGroupId, report.getCheckGroupId())
                            .eq(QtCheckTaskDetail::getItemId, report.getCheckItemId()))
                    .stream().map(QtCheckTaskDetail::getTaskId).collect(Collectors.toList());
        }
        //1.去任务表里面根据根据时间以及id构建查询条件
        LambdaQueryWrapper<QtCheckTask> condition = new LambdaQueryWrapper<QtCheckTask>()
                .in(QtCheckTask::getId, taskIdList)
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .le(QtCheckTask::getFinishedTime, report.getTimeScopeEnd());
        //2.解析排序规则
        if ((QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME.equals(dto.getSortField()))) {
            if (dto.getAscFlag()) {
                //使用last把sql用于排序的中文函数代码语句拼接到最后
                condition.last("ORDER BY CONVERT(finished_time) ASC");
            } else {
                condition.last("ORDER BY CONVERT(finished_time) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getMaterialId);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getProductLine);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getCustomerName);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getSupplierName);
        } else {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getMaterialWorkshopName);
        }

        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<QtCheckTask> resPage = qtCheckTaskService.page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的数据
        List<QtSpcReportDetailVO> qtSpcReportDetailVOList = Lists.newArrayList();

        return null;
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
     * 切割時間段
     *
     * @param dateType  交易類型 M/D/H/m/s -->每月/每天/每小時/每分鐘
     * @param start yyyy-MM-dd HH:mm:ss
     * @param end   yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<String> cutDate(String dateType, String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dBegin = sdf.parse(start);
            Date dEnd = sdf.parse(end);
            return findDates(dateType, dBegin, dEnd);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
    public static List<String> findDates(String dateType, Date dBegin, Date dEnd) throws Exception {
        List<String> listDate = new ArrayList<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (calEnd.after(calBegin)) {
            switch (dateType) {
                case "Y":
                    calBegin.add(Calendar.YEAR,1);
                    break;
                case "Q":
                    calBegin.add(Calendar.MONTH,3);
                    break;
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);
                    break;
                case "m":
                    calBegin.add(Calendar.MINUTE, 1);
                    break;
                case "S":
                    calBegin.add(Calendar.SECOND, 1);
                    break;
            }
            if (calEnd.after(calBegin))
                listDate.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calBegin.getTime()));
            else
                listDate.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calEnd.getTime()));
        }
        return listDate;
    }

}




