package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeign;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.dto.QtCheckPlanPageDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCheckPlanMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final RemoteDictFeign remoteDictFeign;

    private final QtDefectiveItemService qtDefectiveItemService;

    private final RemoteOfficeFeign remoteOfficeFeign;

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
        if(!checkNames.isEmpty()){
            throw new BusinessException("与已有的检验方案名称重复");
        }
        //2、插入计划表
        QtCheckPlan planModel = new QtCheckPlan();
        planModel.setStatus(0);
        planModel.setName(dto.getName());
        if(dto.getNote()==null){
            dto.setNote("");
        }
        planModel.setNote(dto.getNote());
        //3、获取最大编码
        planModel.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYFA, CodeGenerateUtil.NUM_BIT_6, currentMaxCode()));
        if (Objects.isNull(dto.getId())) {
            planModel.setCreateBy(SecurityUtils.getUser().getUserId());
            planModel.setCreateName(SecurityUtils.getUser().getName());
        }
        planModel.setUpdateBy(SecurityUtils.getUser().getUserId());
        planModel.setUpdateName(SecurityUtils.getUser().getName());
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
                    groupModel.setCreateBy(SecurityUtils.getUser().getUserId());
                    groupModel.setCreateName(SecurityUtils.getUser().getName());
                }
                groupModel.setUpdateBy(SecurityUtils.getUser().getUserId());
                groupModel.setUpdateName(SecurityUtils.getUser().getName());
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
//                            if(res.getData().size()!=1 && !res.getData().get(dicId.get(0)).getCode().equals("8")){
//                                throw new BusinessException("平均数数组中的检验项类型错误");
//                            }
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
//                            if(res.getData().size()!=1 && !res.getData().get(dicId.get(0)).getCode().equals("7")){
//                                throw new BusinessException("极差数组中的检验项类型错误");
//                            }
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
                            itemModel.setLinkedDefectiveItem(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray());
                        }
                        //关联客户
                        if(dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode()!=null && dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode().size() > 0){
                            itemModel.setLinkedCustomer(dto.getCheckGroup().get(i).getItems().get(j).getLinkedCustomerCode());
                        }
                        itemModel.setCreateBy(SecurityUtils.getUser().getUserId());
                        itemModel.setCreateName(SecurityUtils.getUser().getName());
                        itemModel.setUpdateBy(SecurityUtils.getUser().getUserId());
                        itemModel.setUpdateName(SecurityUtils.getUser().getName());
                        itemsModel.add(itemModel);
                    }
                    qtCheckPlanGroupItemService.saveBatch(itemsModel);
                }
            }
        }
        return true;
    }

    @Override
    public Page<QtCheckPlanVo> pageList(QtCheckPlanPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtCheckPlan> condition = new LambdaQueryWrapper<QtCheckPlan>()
                .like(Objects.nonNull(dto.getName()), QtCheckPlan::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckPlan::getName);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckPlan::getStatus);
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
        List<QtCheckPlanVo> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (QtCheckPlan qtCheckPlan : resPage.getRecords()) {
                QtCheckPlanVo vo = new QtCheckPlanVo();
                BeanUtils.copyProperties(qtCheckPlan, vo);
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    public QtCheckPlanDetailsVo info(Integer id) {
        //1、先拿到方案
        QtCheckPlanDetailsVo resVo = new QtCheckPlanDetailsVo();
        QtCheckPlan qtCheckPlan = getById(id);
        BeanUtils.copyProperties(qtCheckPlan,resVo);
        //2、拿到分组信息
        List<QtCheckPlanGroup> groups = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>()
                .eq(QtCheckPlanGroup::getPlanId, resVo.getId())
                .orderByAsc(QtCheckPlanGroup::getSort));
        List<QtCheckPlanGroupVo> qtCheckPlanGroupVos = new ArrayList<>();
        groups.forEach(tmp->{
            QtCheckPlanGroupVo groupVo = new QtCheckPlanGroupVo();
            BeanUtils.copyProperties(tmp,groupVo);
            groupVo.setGroupName(tmp.getName());
            //3、处理item信息
            //itemGroup的值
            List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                    .eq(QtCheckPlanGroupItem::getGroupId, tmp.getId())
                    .orderByAsc(QtCheckPlanGroupItem::getSort));
            List<QtCheckPlanItemVo> qtCheckPlanItemVos = new ArrayList<>();
            qtCheckPlanGroupItems.forEach(groupItemTmp->{
                QtCheckPlanItemVo qtCheckPlanItemVo = new QtCheckPlanItemVo();
                //复制itemGroup特有的值
                BeanUtils.copyProperties(groupItemTmp,qtCheckPlanItemVo);
                //复制原来item的值
                //原来item的值
                QtCheckItemListVO qtCheckItemListVO = qtCheckItemService.info(groupItemTmp.getCheckItemId());
                BeanUtils.copyProperties(qtCheckItemListVO,qtCheckPlanItemVo);

                //处理极差数组
                if(groupItemTmp.getRangeArray()!=null && groupItemTmp.getRangeArray().size() > 0){
                    Collection<QtCheckItem> rangeArrayList = qtCheckItemService.listByIds(groupItemTmp.getRangeArray());
                    List<QtRangeArrayVo> rangeArrayVos = new ArrayList<>();
                    rangeArrayList.forEach(rangeArrayTmp->{
                        QtRangeArrayVo qtRangeArrayVo = new QtRangeArrayVo();
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
                    List<QtLinkedDefectiveItemVo> defectItemVos = new ArrayList<>();
                    defectiveArrayList.forEach(defectiveArrayTmp->{
                        QtLinkedDefectiveItemVo defectiveItemVo = new QtLinkedDefectiveItemVo();
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
                    List<QtLinkedCustomerCodeVo> qtLinkedCustomerCodeVos = Lists.newArrayList();
                    groupItemTmp.getLinkedCustomer().forEach(customerTmp->{
                        QtLinkedCustomerCodeVo qtLinkedCustomerCodeVo = new QtLinkedCustomerCodeVo();
                        if(resMap.get(customerTmp)!=null){
                            qtLinkedCustomerCodeVo.setCode(customerTmp);
                            qtLinkedCustomerCodeVo.setName(resMap.get(customerTmp));
                        }
                        qtLinkedCustomerCodeVos.add(qtLinkedCustomerCodeVo);
                    });
                    qtCheckPlanItemVo.setLinkedCustomerCode(qtLinkedCustomerCodeVos);
                }

                //改变id
                qtCheckPlanItemVo.setId(null);
                qtCheckPlanItemVo.setItemId(groupItemTmp.getId());
                qtCheckPlanItemVos.add(qtCheckPlanItemVo);
                groupVo.setItems(qtCheckPlanItemVos);
            });
            qtCheckPlanGroupVos.add(groupVo);
        });
        resVo.setCheckGroup(qtCheckPlanGroupVos);
        return  resVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(QtCheckPlanDTO dto) {
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
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_JYX.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




