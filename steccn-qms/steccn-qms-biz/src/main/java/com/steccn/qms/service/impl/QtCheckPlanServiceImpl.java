package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.QtCheckPlanMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author AndrewLuo
 * @description 针对表【qt_check_plan(检验方案表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
public class QtCheckPlanServiceImpl extends ServiceImpl<QtCheckPlanMapper, QtCheckPlan>
        implements QtCheckPlanService {

    private final QtCheckPlanGroupService qtCheckPlanGroupService;

    private final QtCheckPlanGroupItemService qtCheckPlanGroupItemService;

    private final QtCheckItemService qtCheckItemService;

    private final RemoteDictFeignApi remoteDictFeign;

    private final QtDefectiveItemService qtDefectiveItemService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;

    private final QtCheckPlanMapper qtCheckPlanMapper;

    /**
     * 新增检验计划
     *
     * @param dto
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtCheckPlanDTO dto) {
        //1、校验名字不能为空
        Map<String, Object> name = new HashMap<>(1);
        name.put("name", dto.getName());
        name.put("show_flag", MpConstant.YES);
        Collection<QtCheckPlan> checkNames = listByMap(name);
        //新增校验
        if (dto.getId() == null && !checkNames.isEmpty()) {
            throw new BusinessException("与已有的检验方案名称重复");
        }
        //修改校验
        if (dto.getId() != null && !checkNames.isEmpty()) {
            checkNames.forEach(tmp -> {
                if (!tmp.getId().equals(dto.getId())) {
                    throw new BusinessException("与已有的检验方案名称重复");
                }
            });
        }
        //2、插入计划表
        QtCheckPlan planModel = new QtCheckPlan();
        planModel.setStatus(1);
        planModel.setName(dto.getName());
        planModel.setShowFlag(MpConstant.YES);
        if (dto.getNote() == null) {
            dto.setNote("");
        }
        planModel.setNote(dto.getNote());
        //3、获取最大编码
        planModel.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYFA, CodeGenerateUtil.NUM_BIT_6, currentMaxCode()));
        //修改和新增为一个方法,若有id则是修改，若无则是新增
        if (dto.getId() == null) {
            planModel.setVersion(1L);
            save(planModel);
        } else {
            planModel.setCode(null);
            planModel.setCreateBy(null);
            planModel.setCreateName(null);
            planModel.setId(dto.getId());
            updateById(planModel);
            //升级版本
            upVersion(Sets.newHashSet(dto.getId()));
        }
        //4、处理检验分组
        saveGroupAndItem(planModel.getId(), dto);
        return true;
    }

    /**
     * 保存检验计划分组和明细信息
     *
     * @param planId 检验计划id
     * @param dto    dto
     */
    private void saveGroupAndItem(Integer planId, QtCheckPlanDTO dto) {
        //4、处理检验分组(当是计量检验时,分组不为空)
        if (dto.getCheckMethod()==0 && CollUtil.isNotEmpty(dto.getCheckGroup())) {
            for (int i = 0; i < dto.getCheckGroup().size(); i++) {
                QtCheckPlanGroup groupModel = new QtCheckPlanGroup();
                groupModel.setPlanId(planId);
                groupModel.setName(dto.getCheckGroup().get(i).getGroupName());
                groupModel.setSort(dto.getCheckGroup().get(i).getSort());
                qtCheckPlanGroupService.save(groupModel);
                saveItem(dto.getCheckGroup().get(i).getItems(),groupModel.getId());
            }
        }
        //计数检验直接传item
        if(dto.getCheckMethod()==1 && CollUtil.isNotEmpty(dto.getItems())){
            saveItem(dto.getItems(),0);
        }
    }

    /**
     * 保存item信息
     *
     * @param items 检验计划id
     * @param groupId    dto
     */
    private void saveItem(List<QtCheckPlanItemsDTO> items, Integer groupId){
        //5、处理检验项
        if (items != null && items.size() > 0) {
            List<QtCheckPlanGroupItem> itemsModel = new ArrayList<>();
            for (int j = 0; j < items.size(); j++) {
                QtCheckPlanGroupItem itemModel = new QtCheckPlanGroupItem();
                itemModel.setCheckItemId(items.get(j).getItemId());
                itemModel.setSort(items.get(j).getSort());
                itemModel.setGroupId(groupId);
                //平均数数组
                if (items.get(j).getAverageArray() != null && items.get(j).getAverageArray().size() > 0) {
                    itemModel.setAverageArray(items.get(j).getAverageArray());
                    //校验字典,查看是否自动计算平均值
                    //拿到所有检验项
                    Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(items.get(j).getAverageArray());
                    //拿到字典值
                    List<Long> dicId = new ArrayList<>();
                    qtCheckItems.forEach(tmp -> {
                        dicId.add(tmp.getDataType());
                    });
                    R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                    if (res.getData().size() != 1 && !res.getData().get(dicId.get(0)).getCode().equals("8")) {
                        throw new BusinessException("平均数数组中的检验项类型错误");
                    }
                }
                //极差数组
                if (items.get(j).getRangeArray() != null && items.get(j).getRangeArray().size() > 0) {
                    itemModel.setRangeArray(items.get(j).getRangeArray());
                    //校验字典,查看是否极差数组
                    //拿到所有检验项
                    Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(items.get(j).getRangeArray());
                    //拿到字典值
                    List<Long> dicId = new ArrayList<>();
                    qtCheckItems.forEach(tmp -> {
                        dicId.add(tmp.getDataType());
                    });
                    R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                    if (res.getData().size() != 1 && !res.getData().get(dicId.get(0)).getCode().equals("7")) {
                        throw new BusinessException("极差数组中的检验项类型错误");
                    }
                }
                //方差数组
                if (items.get(j).getMeanArray() != null && items.get(j).getMeanArray().size() > 0) {
                    itemModel.setMeanArray(items.get(j).getMeanArray());
                    //校验字典,查看是否方差
                    //拿到所有检验项
                    Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(items.get(j).getMeanArray());
                    //拿到字典值
                    List<Long> dicId = new ArrayList<>();
                    qtCheckItems.forEach(tmp -> {
                        dicId.add(tmp.getDataType());
                    });
                    R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                    if (res.getData().size() != 1 && !res.getData().get(dicId.get(0)).getCode().equals("9")) {
                        throw new BusinessException("均方差数组中的检验项类型错误");
                    }
                }
                //截尾平均值数组
                if (items.get(j).getEndAverageArray() != null && items.get(j).getEndAverageArray().size() > 0) {
                    itemModel.setEndAverageArray(items.get(j).getEndAverageArray());
                    //校验字典,查看是否方差
                    //拿到所有检验项
                    Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(items.get(j).getEndAverageArray());
                    //拿到字典值
                    List<Long> dicId = new ArrayList<>();
                    qtCheckItems.forEach(tmp -> {
                        dicId.add(tmp.getDataType());
                    });
                    R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                    if (res.getData().size() != 1 && !res.getData().get(dicId.get(0)).getCode().equals("10")) {
                        throw new BusinessException("截尾平均值数组中的检验项类型错误");
                    }
                }
                //标准值
                if (items.get(j).getStandardValue() != null) {
                    itemModel.setStandardValue(items.get(j).getStandardValue());
                }
                //上偏差
                if (items.get(j).getUpperValue() != null) {
                    itemModel.setUpperValue(items.get(j).getUpperValue());
                }
                //下偏差
                if (items.get(j).getLowerValue() != null) {
                    itemModel.setLowerValue(items.get(j).getLowerValue());
                }
                //关联缺陷项
                if (items.get(j).getLinkedDefectiveItem() != null && items.get(j).getLinkedDefectiveItem().size() > 0) {
                    itemModel.setLinkedDefectiveItem(items.get(j).getLinkedDefectiveItem());
                }
                //关联客户
                if (items.get(j).getLinkedCustomerCode() != null && items.get(j).getLinkedCustomerCode().size() > 0) {
                    itemModel.setLinkedCustomer(items.get(j).getLinkedCustomerCode());
                }
                itemsModel.add(itemModel);
            }
            qtCheckPlanGroupItemService.saveBatch(itemsModel);
        }
    }

    @Override
    public Page<QtCheckPlanVO> pageList(QtCheckPlanPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtCheckPlan> condition = new LambdaQueryWrapper<QtCheckPlan>()
                .eq(QtCheckPlan::getShowFlag, MpConstant.YES)
                .like(Objects.nonNull(dto.getName()), QtCheckPlan::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(name using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(name using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckPlan::getStatus);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_UPDATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckPlan::getUpdateTime);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckPlan::getCreateTime);
        } else {
            //默认按创建时间降序排列
            condition.orderBy(true, false, QtCheckPlan::getCreateTime);
        }
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<QtCheckPlan> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<QtCheckPlanVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (QtCheckPlan qtCheckPlan : resPage.getRecords()) {
                QtCheckPlanVO vo = new QtCheckPlanVO();
                BeanUtils.copyProperties(qtCheckPlan, vo);
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    public QtCheckPlanDetailsVO info(Integer id, Long taskId, Integer copy) {
        //1、先拿到方案
        QtCheckPlanDetailsVO resVo = new QtCheckPlanDetailsVO();
        QtCheckPlan qtCheckPlan = getById(id);
        BeanUtils.copyProperties(qtCheckPlan, resVo);
        //如果是计量检验，则有分组
        if(qtCheckPlan.getCheckMethod() == null ||qtCheckPlan.getCheckMethod() == 0){
            //2、拿到分组信息
            List<QtCheckPlanGroup> groups = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>()
                    .eq(QtCheckPlanGroup::getPlanId, resVo.getId())
                    .orderByAsc(QtCheckPlanGroup::getSort));
            List<QtCheckPlanGroupVO> qtCheckPlanGroupVOS = new ArrayList<>();
            groups.forEach(tmp -> {
                QtCheckPlanGroupVO groupVo = new QtCheckPlanGroupVO();
                BeanUtils.copyProperties(tmp, groupVo);
                groupVo.setGroupName(tmp.getName());
                //3、处理item信息
                //itemGroup的值
                List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                        .eq(QtCheckPlanGroupItem::getGroupId, tmp.getId())
                        .orderByAsc(QtCheckPlanGroupItem::getSort));
                List<QtCheckPlanItemVO> qtCheckPlanItemVOS = new ArrayList<>();
                qtCheckPlanGroupItems.forEach(groupItemTmp -> {
                    QtCheckPlanItemVO qtCheckPlanItemVo = new QtCheckPlanItemVO();
                    //复制itemGroup特有的值
                    BeanUtils.copyProperties(groupItemTmp, qtCheckPlanItemVo);
                    //复制原来item的值
                    //原来item的值
                    QtCheckItemListVO qtCheckItemListVO = qtCheckItemService.info(groupItemTmp.getCheckItemId());
                    BeanUtils.copyProperties(qtCheckItemListVO, qtCheckPlanItemVo);
                    qtCheckPlanItemVo.setId(groupItemTmp.getId());
                    //处理极差数组
                    if (groupItemTmp.getRangeArray() != null && groupItemTmp.getRangeArray().size() > 0) {
                        Collection<QtCheckItem> rangeArrayList = qtCheckItemService.listByIds(groupItemTmp.getRangeArray());
                        List<QtRangeArrayVO> rangeArrayVos = new ArrayList<>();
                        rangeArrayList.forEach(rangeArrayTmp -> {
                            QtRangeArrayVO qtRangeArrayVo = new QtRangeArrayVO();
                            BeanUtils.copyProperties(rangeArrayTmp, qtRangeArrayVo);
                            rangeArrayVos.add(qtRangeArrayVo);
                        });
                        qtCheckPlanItemVo.setRangeArray(rangeArrayVos);
                    }
                    //处理平均数数组
                    if (groupItemTmp.getAverageArray() != null && groupItemTmp.getAverageArray().size() > 0) {
                        Collection<QtCheckItem> averageArrayList = qtCheckItemService.listByIds(groupItemTmp.getAverageArray());
                        List<QtAverageArrayVO> averageArrayVos = new ArrayList<>();
                        averageArrayList.forEach(averageArrayTmp -> {
                            QtAverageArrayVO averageArrayVO = new QtAverageArrayVO();
                            BeanUtils.copyProperties(averageArrayTmp, averageArrayVO);
                            averageArrayVos.add(averageArrayVO);
                        });
                        qtCheckPlanItemVo.setAverageArray(averageArrayVos);
                    }

                    //处理缺陷项
                    if (groupItemTmp.getLinkedDefectiveItem() != null && groupItemTmp.getLinkedDefectiveItem().size() > 0) {
                        Collection<QtDefectiveItem> defectiveArrayList = qtDefectiveItemService.listByIds(groupItemTmp.getLinkedDefectiveItem());
                        List<QtLinkedDefectiveItemVO> defectItemVos = new ArrayList<>();
                        defectiveArrayList.forEach(defectiveArrayTmp -> {
                            QtLinkedDefectiveItemVO defectiveItemVo = new QtLinkedDefectiveItemVO();
                            BeanUtils.copyProperties(defectiveArrayTmp, defectiveItemVo);
                            defectItemVos.add(defectiveItemVo);
                        });
                        qtCheckPlanItemVo.setLinkedDefectiveItem(defectItemVos);
                    }

                    //处理关联客户
                    if (groupItemTmp.getLinkedCustomer() != null && groupItemTmp.getLinkedCustomer().size() > 0) {
                        R<List<SysOfficeVO>> officeRes = remoteOfficeFeign.data();
                        //把所有东西导入code和名字导入map
                        Map<String, String> resMap = new HashMap<>();
                        if (officeRes.getData() != null && officeRes.getData().size() > 0) {
                            officeRes.getData().forEach(officeTmp -> {
                                resMap.put(officeTmp.getOfficeCode(), officeTmp.getOfficeName());
                            });
                        }
                        List<QtLinkedCustomerCodeVO> qtLinkedCustomerCodeVOS = Lists.newArrayList();
                        groupItemTmp.getLinkedCustomer().forEach(customerTmp -> {
                            QtLinkedCustomerCodeVO qtLinkedCustomerCodeVo = new QtLinkedCustomerCodeVO();
                            if (resMap.get(customerTmp) != null) {
                                qtLinkedCustomerCodeVo.setCode(customerTmp);
                                qtLinkedCustomerCodeVo.setName(resMap.get(customerTmp));
                            }
                            qtLinkedCustomerCodeVOS.add(qtLinkedCustomerCodeVo);
                        });
                        qtCheckPlanItemVo.setLinkedCustomerCode(qtLinkedCustomerCodeVOS);
                    }

                    //处理检查结果
                    if (taskId != null && copy != null) {
                        List<QtCheckTaskDetail> taskDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                .eq(QtCheckTaskDetail::getTaskId, taskId)
                                .and((groupItemTmp.getCheckItemId() != null), wrapper -> wrapper.eq(QtCheckTaskDetail::getItemId, groupItemTmp.getCheckItemId()))
                                .and((groupItemTmp.getGroupId() != null), wrapper -> wrapper.eq(QtCheckTaskDetail::getGroupId, groupItemTmp.getGroupId()))
                                .and((copy != null), wrapper -> wrapper.eq(QtCheckTaskDetail::getCopy, copy)));
                        if (taskDetails != null && taskDetails.size() > 0) {
                            qtCheckPlanItemVo.setCheckResult(taskDetails.get(0).getCheckResult());
                            //如果缺陷项存在，返回缺陷项
                            if (taskDetails.get(0).getDefectId() != null) {
                                QtDefectiveItem defectiveItem = qtDefectiveItemService.getById(taskDetails.get(0).getDefectId());
                                if (defectiveItem != null) {
                                    qtCheckPlanItemVo.setDefectId(defectiveItem.getId());
                                    qtCheckPlanItemVo.setDefectName(defectiveItem.getName());
                                }
                            }
                        }
                    }
                    //设置itemId
                    qtCheckPlanItemVo.setItemId(groupItemTmp.getCheckItemId());
                    qtCheckPlanItemVOS.add(qtCheckPlanItemVo);
                    groupVo.setItems(qtCheckPlanItemVOS);
                });
                qtCheckPlanGroupVOS.add(groupVo);
            });
            resVo.setCheckGroup(qtCheckPlanGroupVOS);
        }else {

        }
        return resVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(QtCheckPlanDTO dto) {
        if (qtCheckPlanMapper.CountLindkedPlan(dto.getId()) != 0) {
            throw new BusinessException("无法保存！请完成相关检验中的任务再重试");
        }
        //查对应的groupid
        Map<String, Object> groupColumn = new HashMap<>();
        groupColumn.put("plan_id", dto.getId());
        Collection<QtCheckPlanGroup> qtCheckPlanGroups = qtCheckPlanGroupService.listByMap(groupColumn);
        List<Integer> qtCheckGroupIds = Lists.newArrayList();
        qtCheckPlanGroups.forEach(groupTmp -> {
            qtCheckGroupIds.add(groupTmp.getId());
        });
        //通过id删除数据
        qtCheckPlanGroupService.removeByIds(qtCheckGroupIds);
        //删除对应的检验项
        //构建条件,groupId
        LambdaQueryWrapper<QtCheckPlanGroupItem> condition = new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                .in(QtCheckPlanGroupItem::getGroupId, qtCheckGroupIds);
        qtCheckPlanGroupItemService.remove(condition);
        return add(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QtCheckPlan planSnapshot(Integer planId) {
        QtCheckPlan oldPlan = getById(planId);
        QtCheckPlan newPlan = new QtCheckPlan();
        BeanUtils.copyProperties(oldPlan, newPlan);
        //保存新检验计划
        newPlan.setId(null);
        newPlan.setShowFlag(MpConstant.NO);
        newPlan.setStatus(MpConstant.YES);
        newPlan.setCode(oldPlan.getCode());
        newPlan.setOriginalId(oldPlan.getId());
        //版本+1
        newPlan.setVersion(oldPlan.getVersion() + 1);
        save(newPlan);
        //旧编码设置为用户不可见
        oldPlan.setShowFlag(MpConstant.YES);
        oldPlan.setVersion(oldPlan.getVersion() + 1);
        updateById(oldPlan);
        List<QtCheckPlanGroup> planGroup = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>().eq(QtCheckPlanGroup::getPlanId, oldPlan.getId()));
        //定义校验项id集合
        List<Integer> checkItemIdList = Lists.newArrayList();
        //定义缺陷项id集合
        List<Integer> defectiveItemIdList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(planGroup)) {
            //性能优化：①检验项缺陷项重复做快照 ②避免重复查询分组明细信息
            //分组明细map，key->分组id value->分组对应的明细列表
            Map<Integer,List<QtCheckPlanGroupItem>> groupItemMap = Maps.newHashMap();
            planGroup.forEach(p ->{
                List<QtCheckPlanGroupItem> groupItemList = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>().eq(QtCheckPlanGroupItem::getGroupId, p.getId()));
                groupItemMap.put(p.getId(),groupItemList);
                if (CollUtil.isNotEmpty(groupItemList)) {
                    groupItemList.forEach(q -> {
                        if (Objects.nonNull(q.getCheckItemId())) {
                            checkItemIdList.add(q.getCheckItemId());
                        }
                        if (CollUtil.isNotEmpty(q.getRangeArray())) {
                            checkItemIdList.addAll(q.getRangeArray());
                        }
                        if (CollUtil.isNotEmpty(q.getAverageArray())) {
                            checkItemIdList.addAll(q.getAverageArray());
                        }
                        if (CollUtil.isNotEmpty(q.getLinkedDefectiveItem())) {
                            defectiveItemIdList.addAll(q.getLinkedDefectiveItem());
                        }
                    });
                }
            });
            //缺陷项和检查项需要备份
            //缺陷项和检查项的快照映射关系
            Map<Integer, Integer> checkItemIdMap = qtCheckItemService.checkItemSnapshot(checkItemIdList);
            Map<Integer, Integer> defectiveItemIdMap = qtDefectiveItemService.defectiveItemSnapshot(defectiveItemIdList);
            planGroup.forEach(v -> {
                v.setPlanId(newPlan.getId());
                Integer oldGroupId = v.getId();
                v.setId(null);
                qtCheckPlanGroupService.save(v);
                //获取所有的分组详细信息
                List<QtCheckPlanGroupItem> groupItemList = groupItemMap.get(oldGroupId);
                if (CollUtil.isNotEmpty(groupItemList)) {
                    List<QtCheckPlanGroupItem> batchItem = Lists.newArrayList();
                    groupItemList.forEach(q -> {
                        q.setGroupId(v.getId());
                        q.setId(null);
                        q.setCheckItemId(checkItemIdMap.get(q.getCheckItemId()));
                        //赋值数组
                        q.setRangeArray(snapshotArray(q.getRangeArray(), checkItemIdMap));
                        q.setAverageArray(snapshotArray(q.getAverageArray(), checkItemIdMap));
                        q.setLinkedDefectiveItem(snapshotArray(q.getLinkedDefectiveItem(), defectiveItemIdMap));
                        batchItem.add(q);
                    });
                    qtCheckPlanGroupItemService.saveBatch(batchItem);
                }
            });
        }
        return newPlan;
    }

    @Override
    public Set<Integer> linkedPlan(Integer itemId, Integer type) {
        Set<Integer> resPlanList = Sets.newHashSet();
        List<QtCheckPlanGroupItem> itemList;
        if (0 == type) {
            itemList = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                    .apply("JSON_CONTAINS(average_array, JSON_ARRAY({0}))", itemId)
                    .or()
                    .apply("JSON_CONTAINS(range_array, JSON_ARRAY({0}))", itemId)
                    .or()
                    .eq(QtCheckPlanGroupItem::getCheckItemId, itemId));
        } else {
            itemList = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                    .apply("JSON_CONTAINS(linked_defective_item, JSON_ARRAY({0}))", itemId));
        }
        if (CollUtil.isNotEmpty(itemList)) {
            List<Integer> groupIdList = itemList.stream().map(QtCheckPlanGroupItem::getGroupId).collect(Collectors.toList());
            List<QtCheckPlanGroup> planGroupList = qtCheckPlanGroupService.listByIds(groupIdList);
            if (CollUtil.isNotEmpty(planGroupList)) {
                return planGroupList.stream().map(QtCheckPlanGroup::getPlanId).collect(Collectors.toSet());
            }
        }
        return resPlanList;
    }

    @Override
    public void upVersion(Set<Integer> planIdList) {
        if(CollUtil.isNotEmpty(planIdList)){
            update(null, new LambdaUpdateWrapper<QtCheckPlan>().setSql("version = version + 1")
                    .eq(QtCheckPlan::getShowFlag, MpConstant.YES)
                    .in(QtCheckPlan::getId, planIdList));
        }
    }

    /**
     * 快照数组
     *
     * @param oldList 旧数据
     * @param map     map
     * @return list
     */
    private List<Integer> snapshotArray(List<Integer> oldList, Map<Integer, Integer> map) {
        List<Integer> resList = Lists.newArrayList();
        if (CollUtil.isEmpty(oldList)) {
            return resList;
        }
        for (Integer data : oldList) {
            resList.add(map.get(data));
        }
        return resList;
    }

    @Override
    public Boolean status(QtCheckPlanStausDTO dto) {
        //如果是启用则直接启用
        if (dto.getStatus() == 1) {
            QtCheckPlan qtCheckPlan = new QtCheckPlan();
            qtCheckPlan.setId(dto.getId());
            qtCheckPlan.setStatus(dto.getStatus());
            return updateById(qtCheckPlan);
        } else {
            //停用则查询方案是否关联了物料,没有关联则继续
            if (!weatherBindMaterial(dto.getId())) {
                QtCheckPlan qtCheckPlan = new QtCheckPlan();
                qtCheckPlan.setId(dto.getId());
                qtCheckPlan.setStatus(dto.getStatus());
                return updateById(qtCheckPlan);
            } else {
                throw new BusinessException("已有物料配置调用了该检验方案，不允许停用");
            }
        }

    }

    @Override
    public Boolean delete(Integer id) {
        //查询是否绑定了物料
        if (!weatherBindMaterial(id)) {
            return removeById(id);
        } else {
            throw new BusinessException("已有物料配置调用了该检验方案，不允许删除");
        }
    }

    @Override
    public List<QtCheckPlanListAllVO> listAll() {
        LambdaQueryWrapper<QtCheckPlan> condition = new LambdaQueryWrapper<QtCheckPlan>()
                .eq(QtCheckPlan::getStatus, 1)
                .eq(QtCheckPlan::getShowFlag, MpConstant.YES);
        List<QtCheckPlan> qtCheckPlans = list(condition);
        List<QtCheckPlanListAllVO> vos = Lists.newArrayList();
        qtCheckPlans.forEach(tmp -> {
            QtCheckPlanListAllVO vo = new QtCheckPlanListAllVO();
            BeanUtils.copyProperties(tmp, vo);
            vos.add(vo);
        });
        return vos;
    }

    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        QtCheckPlan maxCodeModel = getOne(new LambdaQueryWrapper<QtCheckPlan>().orderByDesc(QtCheckPlan::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_JYFA.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    /**
     * 查询物料是否绑定了方案
     *
     * @param id
     * @return 绑定返回true，没有返回false
     */
    private Boolean weatherBindMaterial(Integer id) {
        LambdaQueryWrapper<QtMaterialLinkCheckPlan> condition = new LambdaQueryWrapper<QtMaterialLinkCheckPlan>()
                .eq(QtMaterialLinkCheckPlan::getCheckPlanId, id);
        Long count = qtMaterialLinkCheckPlanService.count(condition);
        if (count == 0) {
            return false;
        } else {
            return true;
        }
    }
}




