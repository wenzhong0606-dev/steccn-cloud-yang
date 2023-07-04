package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.oa.domain.Hrmdepartment;
import com.steccn.oa.dto.OaClassUserNameDTO;
import com.steccn.oa.service.HrmdepartmentService;
import com.steccn.oa.mapper.HrmdepartmentMapper;
import com.steccn.oa.vo.OaDepartmentTreeVO;
import com.steccn.oa.vo.OaDepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
* @author Admin
* @description 针对表【hrmdepartment】的数据库操作Service实现
* @createDate 2023-05-30 14:54:41
*/
@Service
@RequiredArgsConstructor
public class HrmdepartmentServiceImpl extends ServiceImpl<HrmdepartmentMapper, Hrmdepartment>
    implements HrmdepartmentService{

    private final HrmdepartmentMapper hrmdepartmentMapper;

    @Override
    public OaDepartmentVO getOaDepartmentById(Integer id) {
        Hrmdepartment hrmdepartment = getById(id);
        OaDepartmentVO vo = new OaDepartmentVO();
        if(Objects.isNull(hrmdepartment)){
            return vo;
        }
        vo.setId(hrmdepartment.getId());
        vo.setDepartmentName(hrmdepartment.getDepartmentname());
        vo.setSuperId(hrmdepartment.getSupdepid());
        vo.setTLevel(hrmdepartment.getTlevel());
        return vo;
    }

    @Override
    public Map<String, List<OaClassUserNameDTO>> getClassUserName(Set<String> productWorkShop) {
        Map<String, List<OaClassUserNameDTO>> result = Maps.newHashMap();
        if(CollUtil.isNotEmpty(productWorkShop)){
            for(String tmp:productWorkShop){
                List<OaClassUserNameDTO> oaClassUserNameDTOS = hrmdepartmentMapper.selectGroupLeader(tmp);
                result.put(tmp,oaClassUserNameDTOS);
            }
        }
        return result;
    }

    @Override
    public List<OaDepartmentTreeVO> getDepartmentTree(Integer subCompanyId) {
        //部门列表
        List<Hrmdepartment> departmentList = list(new LambdaQueryWrapper<Hrmdepartment>()
                .eq(Hrmdepartment::getSubcompanyid1, subCompanyId)
                .isNull(Hrmdepartment::getCanceled));
        if(CollUtil.isEmpty(departmentList)){
            throw new BusinessException("该分部下不存在部门");
        }
        //倒入vo
        List<OaDepartmentTreeVO> vos = Lists.newArrayList();
        departmentList.forEach(tmp->{
            OaDepartmentTreeVO vo = new OaDepartmentTreeVO();
            BeanUtil.copyProperties(tmp,vo);
            vos.add(vo);
        });
        //构建部门ID与部门对象的映射关系
        Map<Integer,OaDepartmentTreeVO> departmentMap = Maps.newHashMap();
        for(OaDepartmentTreeVO vo:vos) {
            departmentMap.put(vo.getId(), vo);
        }
        //构建树形结构
        List<OaDepartmentTreeVO> treeList = Lists.newArrayList();
        for(OaDepartmentTreeVO vo:vos){
            Integer superid = vo.getSupdepid();
            if(superid == null || superid == 0){
                //根节点
                treeList.add(vo);
            }else {
                OaDepartmentTreeVO parentVO = departmentMap.get(superid);
                if(parentVO!=null){
                    parentVO.addChild(vo);
                }
            }
        }
        return treeList;
    }
}




