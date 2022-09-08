package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanTypeDTO;
import com.steccn.qms.dto.QtMaterialPageDTO;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import com.steccn.qms.service.QtMaterialService;
import com.steccn.qms.mapper.QtMaterialMapper;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtMaterialPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
* @author yequan
* @description 针对表【qt_material(物料表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
@RequiredArgsConstructor
public class QtMaterialServiceImpl extends ServiceImpl<QtMaterialMapper, QtMaterial>
    implements QtMaterialService{

    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    /**
     * 配置检验项和检验方案
     * @param dto
     * @return
     */
    @Override
    public Boolean edit(QtMaterialLinkCheckPlanDTO dto) {
        QtMaterial qtMaterial = new QtMaterial();
        qtMaterial.setId(Long.parseLong(dto.getMaterialId()));
        if(dto.getType() == null || dto.getType().size() == 0){
            throw new BusinessException("type为空");
        }
        qtMaterial.setType(dto.getType());
        //插入
        //1、先删除之前的配置数据
        Map<String, Object> materialColumn = new HashMap<>();
        materialColumn.put("material_id",dto.getMaterialId());
        qtMaterialLinkCheckPlanService.removeByMap(materialColumn);
        List<QtMaterialLinkCheckPlan> qtMaterialLinkCheckPlanList = Lists.newArrayList();
        if(dto.getType()!=null && dto.getType().size() > 0){
            dto.getType().forEach(typeTmp->{
                QtMaterialLinkCheckPlan qtMaterialLinkCheckPlan = new QtMaterialLinkCheckPlan();
                qtMaterialLinkCheckPlan.setMaterialId(Long.parseLong(dto.getMaterialId()));
                qtMaterialLinkCheckPlan.setCheckPlanId(typeTmp.getCheckPlanId());
                qtMaterialLinkCheckPlanList.add(qtMaterialLinkCheckPlan);
            });
        }
        if(qtMaterialLinkCheckPlanList.size() > 0){
            qtMaterialLinkCheckPlanService.saveBatch(qtMaterialLinkCheckPlanList);
        }
        return updateById(qtMaterial);
    }

    /**
     * 返回分页
     * @param dto
     * @return
     */
    @Override
    public Page<QtMaterialPageVO> pageList(QtMaterialPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtMaterial> condition = new LambdaQueryWrapper<QtMaterial>()
                //物料名称或者物料编码
                .and((dto.getNameOrCode() != null && !dto.getNameOrCode().equals("")),wrapper -> wrapper.like(QtMaterial::getName,dto.getNameOrCode()).or().like(QtMaterial::getCode,dto.getNameOrCode()))
                //检验类型
                .and((dto.getCheckType() != null && !dto.getCheckType().equals("")),wrapper -> wrapper.like(QtMaterial::getType,"\"checkType\":" +dto.getCheckType()+"\""))
                //检验方案
                .and((dto.getLinkedPlan() != null && !dto.getLinkedPlan().equals("")),wrapper -> wrapper.like(QtMaterial::getType,"\"checkPlanName\":" +dto.getLinkedPlan()+"\""));
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtMaterial::getName);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_UPDATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtMaterial::getUpdateTime);
        } else {
            //默认按创建时间降序排列
            condition.orderBy(true, false, QtMaterial::getCreateTime);
        }
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<QtMaterial> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<QtMaterialPageVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (QtMaterial qtMaterial : resPage.getRecords()) {
                QtMaterialPageVO vo = new QtMaterialPageVO();
                BeanUtils.copyProperties(qtMaterial, vo);
                //处理list数据
                if(qtMaterial.getType()!=null){
                    String s = JSONObject.toJSONString(qtMaterial.getType());
                    List<QtMaterialLinkCheckPlanTypeDTO> lists = JSONObject.parseArray(s,QtMaterialLinkCheckPlanTypeDTO.class);
                    List<String> matchedCheckType = new ArrayList<>();
                    List<String> linkedCheckPlan = new ArrayList<>();
                    for(int i = 0; i < lists.size(); i++){
                        linkedCheckPlan.add(lists.get(i).getCheckPlanName());
                        matchedCheckType.add(lists.get(i).getCheckType());
                    }
                    vo.setMatchedCheckType(matchedCheckType);
                    vo.setLinkedCheckPlan(linkedCheckPlan);
                }
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    /**
     * 回显
     * @param id
     * @return
     */
    @Override
    public QtMaterial info(Long id) {
        QtMaterial qtMaterial = getById(id);
        R<List<SysOfficeVO>> res = remoteOfficeFeign.flatData("7");
        return qtMaterial;
    }
}




