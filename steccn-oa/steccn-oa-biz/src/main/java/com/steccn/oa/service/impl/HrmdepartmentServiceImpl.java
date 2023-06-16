package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.oa.domain.Hrmdepartment;
import com.steccn.oa.dto.OaClassUserNameDTO;
import com.steccn.oa.service.HrmdepartmentService;
import com.steccn.oa.mapper.HrmdepartmentMapper;
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
}




