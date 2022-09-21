package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.dto.QtCheckPlanPageDTO;
import com.steccn.qms.dto.QtCheckPlanStausDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCheckPlanMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author AndrewLuo
* @description 针对表【qt_check_plan(检验方案表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
@RequiredArgsConstructor
public class QtCheckPlanServiceImpl extends ServiceImpl<QtCheckPlanMapper, QtCheckPlan>
    implements QtCheckPlanService{

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
     * @param dto
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtCheckPlanDTO dto) {
        //1、校验名字不能为空
        Map<String,Object> name = new HashMap<>(1);
        name.put("name",dto.getName());
        Collection<QtCheckPlan> checkNames = listByMap(name);
        //新增校验
        if(dto.getId()==null&&!checkNames.isEmpty()){
            throw new BusinessException("与已有的检验方案名称重复");
        }
        //修改校验
        if(dto.getId()!=null&&!checkNames.isEmpty()){
            checkNames.forEach(tmp->{
                if(!tmp.getId().equals(dto.getId())){
                    throw new BusinessException("与已有的检验方案名称重复");
                }
            });
        }
        //2、插入计划表
        QtCheckPlan planModel = new QtCheckPlan();
        planModel.setStatus(1);
        planModel.setName(dto.getName());
        if(dto.getNote()==null){
            dto.setNote("");
        }
        planModel.setNote(dto.getNote());
        //3、获取最大编码
        planModel.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYFA, CodeGenerateUtil.NUM_BIT_6, currentMaxCode()));
        if (Objects.isNull(dto.getId())) {
//            planModel.setCreateBy(SecurityUtils.getUser().getUserId());
//            planModel.setCreateName(SecurityUtils.getUser().getName());
        }
//        planModel.setUpdateBy(SecurityUtils.getUser().getUserId());
//        planModel.setUpdateName(SecurityUtils.getUser().getName());
        //修改和新增为一个方法,若有id则是修改，若无则是新增
        if(dto.getId()==null){
            save(planModel);
        }else {
            planModel.setCode(null);
            planModel.setCreateBy(null);
            planModel.setCreateName(null);
            planModel.setId(dto.getId());
            updateById(planModel);
        }
        //4、处理检验分组
        if(dto.getCheckGroup()!=null && dto.getCheckGroup().size() > 0){
            for(int i = 0; i < dto.getCheckGroup().size(); i++){
                QtCheckPlanGroup groupModel = new QtCheckPlanGroup();
                groupModel.setPlanId(planModel.getId());
                groupModel.setName(dto.getCheckGroup().get(i).getGroupName());
                groupModel.setSort(dto.getCheckGroup().get(i).getSort());
                if (Objects.isNull(dto.getCheckGroup().get(i).getId())) {
//                    groupModel.setCreateBy(SecurityUtils.getUser().getUserId());
//                    groupModel.setCreateName(SecurityUtils.getUser().getName());
                }
//                groupModel.setUpdateBy(SecurityUtils.getUser().getUserId());
//                groupModel.setUpdateName(SecurityUtils.getUser().getName());
                qtCheckPlanGroupService.save(groupModel);
                //5、处理检验项
                if(dto.getCheckGroup().get(i).getItems()!=null && dto.getCheckGroup().get(i).getItems().size() > 0){
                    List<QtCheckPlanGroupItem> itemsModel = new ArrayList<>();
                    for(int j = 0; j < dto.getCheckGroup().get(i).getItems().size(); j++){
                        QtCheckPlanGroupItem itemModel = new QtCheckPlanGroupItem();
                        itemModel.setCheckItemId(dto.getCheckGroup().get(i).getItems().get(j).getItemId());
                        itemModel.setSort(dto.getCheckGroup().get(i).getItems().get(j).getSort());
                        itemModel.setGroupId(groupModel.getId());
                        //平均数数组
                        if(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray()!=null && dto.getCheckGroup().get(i).getItems().get(j).getAverageArray().size() > 0){
                            itemModel.setAverageArray(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray());
                            //校验字典,查看是否自动计算平均值
                            //拿到所有检验项
                            Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray());
                            //拿到字典值
                            List<Long> dicId = new ArrayList<>();
                            qtCheckItems.forEach(tmp -> {
                                dicId.add(tmp.getDataType());
                            });
                            R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                            if(res.getData().size()!=1 && !res.getData().get(dicId.get(0)).getCode().equals("8")){
                                throw new BusinessException("平均数数组中的检验项类型错误");
                            }
                        }
                        //极差数组
                        if(dto.getCheckGroup().get(i).getItems().get(j).getRangeArray()!=null && dto.getCheckGroup().get(i).getItems().get(j).getRangeArray().size() > 0){
                            itemModel.setRangeArray(dto.getCheckGroup().get(i).getItems().get(j).getRangeArray());
                            //校验字典,查看是否极差数组
                            //拿到所有检验项
                            Collection<QtCheckItem> qtCheckItems = qtCheckItemService.listByIds(dto.getCheckGroup().get(i).getItems().get(j).getRangeArray());
                            //拿到字典值
                            List<Long> dicId = new ArrayList<>();
                            qtCheckItems.forEach(tmp -> {
                                dicId.add(tmp.getDataType());
                            });
                            R<Map<Long, SysDictVO>> res = remoteDictFeign.listById(dicId);
                            if(res.getData().size()!=1 && !res.getData().get(dicId.get(0)).getCode().equals("7")){
                                throw new BusinessException("极差数组中的检验项类型错误");
                            }
                        }
                        //标准值
                        if(dto.getCheckGroup().get(i).getItems().get(j).getStandardValue()!=null){
                            itemModel.setStandardValue(dto.getCheckGroup().get(i).getItems().get(j).getStandardValue());
                        }
                        //上偏差
                        if(dto.getCheckGroup().get(i).getItems().get(j).getUpperValue()!=null){
                            itemModel.setUpperValue(dto.getCheckGroup().get(i).getItems().get(j).getUpperValue());
                        }
                        //下偏差
                        if(dto.getCheckGroup().get(i).getItems().get(j).getLowerValue()!=null){
                            itemModel.setLowerValue(dto.getCheckGroup().get(i).getItems().get(j).getLowerValue());
                        }
                        //关联缺陷项
                        if(dto.getCheckGroup().get(i).getItems().get(j).getLinkedDefectiveItem()!=null && dto.getCheckGroup().get(i).getItems().get(j).getLinkedDefectiveItem().size() > 0){
                            itemModel.setLinkedDefectiveItem(dto.getCheckGroup().get(i).getItems().get(j).getLinkedDefectiveItem());
                        }
                        //关联客户
                        if(dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode()!=null && dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode().size() > 0){
                            itemModel.setLinkedCustomer(dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode());
                        }
//                        itemModel.setCreateBy(SecurityUtils.getUser().getUserId());
//                        itemModel.setCreateName(SecurityUtils.getUser().getName());
//                        itemModel.setUpdateBy(SecurityUtils.getUser().getUserId());
//                        itemModel.setUpdateName(SecurityUtils.getUser().getName());
                        itemsModel.add(itemModel);
                    }
                    qtCheckPlanGroupItemService.saveBatch(itemsModel);
                }
            }
        }
        return true;
    }

    @Override
    public Page<QtCheckPlanVO> pageList(QtCheckPlanPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtCheckPlan> condition = new LambdaQueryWrapper<QtCheckPlan>()
                .like(Objects.nonNull(dto.getName()), QtCheckPlan::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckPlan::getName);
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
    public QtCheckPlanDetailsVO info(Integer id,Long taskId,Integer copy) {
        //1、先拿到方案
        QtCheckPlanDetailsVO resVo = new QtCheckPlanDetailsVO();
        QtCheckPlan qtCheckPlan = getById(id);
        BeanUtils.copyProperties(qtCheckPlan,resVo);
        //2、拿到分组信息
        List<QtCheckPlanGroup> groups = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>()
                .eq(QtCheckPlanGroup::getPlanId, resVo.getId())
                .orderByAsc(QtCheckPlanGroup::getSort));
        List<QtCheckPlanGroupVO> qtCheckPlanGroupVOS = new ArrayList<>();
        groups.forEach(tmp->{
            QtCheckPlanGroupVO groupVo = new QtCheckPlanGroupVO();
            BeanUtils.copyProperties(tmp,groupVo);
            groupVo.setGroupName(tmp.getName());
            //3、处理item信息
            //itemGroup的值
            List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                    .eq(QtCheckPlanGroupItem::getGroupId, tmp.getId())
                    .orderByAsc(QtCheckPlanGroupItem::getSort));
            List<QtCheckPlanItemVO> qtCheckPlanItemVOS = new ArrayList<>();
            qtCheckPlanGroupItems.forEach(groupItemTmp->{
                QtCheckPlanItemVO qtCheckPlanItemVo = new QtCheckPlanItemVO();
                //复制itemGroup特有的值
                BeanUtils.copyProperties(groupItemTmp,qtCheckPlanItemVo);
                //复制原来item的值
                //原来item的值
                QtCheckItemListVO qtCheckItemListVO = qtCheckItemService.info(groupItemTmp.getCheckItemId());
                BeanUtils.copyProperties(qtCheckItemListVO,qtCheckPlanItemVo);
                qtCheckPlanItemVo.setId(groupItemTmp.getId());
                //处理极差数组
                if(groupItemTmp.getRangeArray()!=null && groupItemTmp.getRangeArray().size() > 0){
                    Collection<QtCheckItem> rangeArrayList = qtCheckItemService.listByIds(groupItemTmp.getRangeArray());
                    List<QtRangeArrayVO> rangeArrayVos = new ArrayList<>();
                    rangeArrayList.forEach(rangeArrayTmp->{
                        QtRangeArrayVO qtRangeArrayVo = new QtRangeArrayVO();
                        BeanUtils.copyProperties(rangeArrayTmp,qtRangeArrayVo);
                        rangeArrayVos.add(qtRangeArrayVo);
                    });
                    qtCheckPlanItemVo.setRangeArray(rangeArrayVos);
                }
                //处理平均数数组
                if(groupItemTmp.getAverageArray()!=null && groupItemTmp.getAverageArray().size() > 0){
                    Collection<QtCheckItem> averageArrayList = qtCheckItemService.listByIds(groupItemTmp.getAverageArray());
                    List<QtAverageArrayVO> averageArrayVos = new ArrayList<>();
                    averageArrayList.forEach(averageArrayTmp->{
                        QtAverageArrayVO averageArrayVO = new QtAverageArrayVO();
                        BeanUtils.copyProperties(averageArrayTmp,averageArrayVO);
                        averageArrayVos.add(averageArrayVO);
                    });
                    qtCheckPlanItemVo.setAverageArray(averageArrayVos);
                }

                //处理缺陷项
                if(groupItemTmp.getLinkedDefectiveItem()!=null && groupItemTmp.getLinkedDefectiveItem().size() > 0){
                    Collection<QtDefectiveItem> defectiveArrayList = qtDefectiveItemService.listByIds(groupItemTmp.getLinkedDefectiveItem());
                    List<QtLinkedDefectiveItemVO> defectItemVos = new ArrayList<>();
                    defectiveArrayList.forEach(defectiveArrayTmp->{
                        QtLinkedDefectiveItemVO defectiveItemVo = new QtLinkedDefectiveItemVO();
                        BeanUtils.copyProperties(defectiveArrayTmp,defectiveItemVo);
                        defectItemVos.add(defectiveItemVo);
                    });
                    qtCheckPlanItemVo.setLinkedDefectiveItem(defectItemVos);
                }

                //处理关联客户
                if(groupItemTmp.getLinkedCustomer()!=null && groupItemTmp.getLinkedCustomer().size() > 0){
                    R<List<SysOfficeVO>> officeRes = remoteOfficeFeign.data();
                    //把所有东西导入code和名字导入map
                    Map<String,String> resMap = new HashMap<>();
                    if(officeRes.getData()!=null && officeRes.getData().size() > 0){
                        officeRes.getData().forEach(officeTmp->{
                            resMap.put(officeTmp.getOfficeCode(),officeTmp.getOfficeName());
                        });
                    }
                    List<QtLinkedCustomerCodeVO> qtLinkedCustomerCodeVOS = Lists.newArrayList();
                    groupItemTmp.getLinkedCustomer().forEach(customerTmp->{
                        QtLinkedCustomerCodeVO qtLinkedCustomerCodeVo = new QtLinkedCustomerCodeVO();
                        if(resMap.get(customerTmp)!=null){
                            qtLinkedCustomerCodeVo.setCode(customerTmp);
                            qtLinkedCustomerCodeVo.setName(resMap.get(customerTmp));
                        }
                        qtLinkedCustomerCodeVOS.add(qtLinkedCustomerCodeVo);
                    });
                    qtCheckPlanItemVo.setLinkedCustomerCode(qtLinkedCustomerCodeVOS);
                }

                //处理检查结果
                if(taskId!=null && copy!=null){
                    List<QtCheckTaskDetail> taskDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                            .eq(QtCheckTaskDetail::getTaskId,taskId)
                            .and((groupItemTmp.getCheckItemId() != null),wrapper->wrapper.eq(QtCheckTaskDetail::getItemId,groupItemTmp.getCheckItemId()))
                            .and((groupItemTmp.getGroupId() != null),wrapper -> wrapper.eq(QtCheckTaskDetail::getGroupId,groupItemTmp.getGroupId()))
                            .and((copy != null),wrapper -> wrapper.eq(QtCheckTaskDetail::getCopy,copy)));
                    if(taskDetails!=null && taskDetails.size() > 0){
                        qtCheckPlanItemVo.setCheckResult(taskDetails.get(0).getCheckResult());
                        //如果缺陷项存在，返回缺陷项
                        if(taskDetails.get(0).getDefectId() != null){
                            QtDefectiveItem defectiveItem = qtDefectiveItemService.getById(taskDetails.get(0).getDefectId());
                            if(defectiveItem!=null){
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
        return  resVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(QtCheckPlanDTO dto) {
        if(qtCheckPlanMapper.CountLindkedPlan(dto.getId())!=0){
            throw new BusinessException("无法保存！请完成相关检验中的任务再重试");
        }
        //查对应的groupid
        Map<String, Object> groupColumn = new HashMap<>();
        groupColumn.put("plan_id",dto.getId());
        Collection<QtCheckPlanGroup> qtCheckPlanGroups = qtCheckPlanGroupService.listByMap(groupColumn);
        List<Integer> qtCheckGroupIds = Lists.newArrayList();
        qtCheckPlanGroups.forEach(groupTmp->{
            qtCheckGroupIds.add(groupTmp.getId());
        });
        //通过id删除数据
        qtCheckPlanGroupService.removeByIds(qtCheckGroupIds);
        //删除对应的检验项
        //构建条件,groupId
        LambdaQueryWrapper<QtCheckPlanGroupItem> condition = new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                .in(QtCheckPlanGroupItem::getGroupId,qtCheckGroupIds);
        qtCheckPlanGroupItemService.remove(condition);
        return add(dto);
    }

    @Override
    public Boolean status(QtCheckPlanStausDTO dto) {
        //如果是启用则直接启用
        if(dto.getStatus() == 1){
            QtCheckPlan qtCheckPlan = new QtCheckPlan();
            qtCheckPlan.setId(dto.getId());
            qtCheckPlan.setStatus(dto.getStatus());
            return updateById(qtCheckPlan);
        }else {
            //停用则查询方案是否关联了物料,没有关联则继续
            if(!weatherBindMaterial(dto.getId())){
                QtCheckPlan qtCheckPlan = new QtCheckPlan();
                qtCheckPlan.setId(dto.getId());
                qtCheckPlan.setStatus(dto.getStatus());
                return updateById(qtCheckPlan);
            }else {
                throw new BusinessException("已有物料配置调用了该检验方案，不允许停用");
            }
        }

    }

    @Override
    public Boolean delete(Integer id) {
        //查询是否绑定了物料
        if(!weatherBindMaterial(id)){
            return removeById(id);
        }else {
            throw new BusinessException("已有物料配置调用了该检验方案，不允许删除");
        }
    }

    @Override
    public List<QtCheckPlanListAllVO> listAll() {
        LambdaQueryWrapper<QtCheckPlan> condition = new LambdaQueryWrapper<QtCheckPlan>()
                .eq(QtCheckPlan::getStatus,1);
        List<QtCheckPlan> qtCheckPlans = list(condition);
        List<QtCheckPlanListAllVO> vos = Lists.newArrayList();
        qtCheckPlans.forEach(tmp->{
            QtCheckPlanListAllVO vo = new QtCheckPlanListAllVO();
            BeanUtils.copyProperties(tmp,vo);
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
     * @param id
     * @return 绑定返回true，没有返回false
     */
    private Boolean weatherBindMaterial(Integer id){
        LambdaQueryWrapper<QtMaterialLinkCheckPlan> condition = new LambdaQueryWrapper<QtMaterialLinkCheckPlan>()
                .eq(QtMaterialLinkCheckPlan::getCheckPlanId,id);
        Long count = qtMaterialLinkCheckPlanService.count(condition);
        if(count == 0){
            return false;
        }else {
            return true;
        }
    }
}




