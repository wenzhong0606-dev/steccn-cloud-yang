package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeign;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.service.QtCheckPlanGroupService;
import com.steccn.qms.service.QtCheckPlanService;
import com.steccn.qms.mapper.QtCheckPlanMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author yequan
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

//    private final RemoteDictFeign remoteDictFeign;


    @Override
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
        save(planModel);
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
                        //平均数数组
                        if(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray()!=null && dto.getCheckGroup().get(i).getItems().get(j).getAverageArray().size() > 0){
                            itemModel.setAverageArray(dto.getCheckGroup().get(i).getItems().get(j).getAverageArray());
                            //校验字典
//                            QtCheckItem qtCheckItem = qtCheckItemService.getById(dto.getCheckGroup().get(i).getItems().get(j).getItemId());
//                            remoteDictFeign.existTypeId()
                        }
                        //极差数组
                        if(dto.getCheckGroup().get(i).getItems().get(j).getRangeArray()!=null && dto.getCheckGroup().get(i).getItems().get(j).getRangeArray().size() > 0){
                            itemModel.setRangeArray(dto.getCheckGroup().get(i).getItems().get(j).getRangeArray());
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




