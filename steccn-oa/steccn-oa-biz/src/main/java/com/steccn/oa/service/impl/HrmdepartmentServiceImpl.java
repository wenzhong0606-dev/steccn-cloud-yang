package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.domain.Hrmdepartment;
import com.steccn.oa.service.HrmdepartmentService;
import com.steccn.oa.mapper.HrmdepartmentMapper;
import com.steccn.oa.vo.OaDepartmentVO;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【hrmdepartment】的数据库操作Service实现
* @createDate 2023-05-30 14:54:41
*/
@Service
public class HrmdepartmentServiceImpl extends ServiceImpl<HrmdepartmentMapper, Hrmdepartment>
    implements HrmdepartmentService{

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
}




