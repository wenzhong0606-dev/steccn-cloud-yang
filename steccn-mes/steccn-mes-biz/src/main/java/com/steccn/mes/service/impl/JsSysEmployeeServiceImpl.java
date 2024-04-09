package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.mes.domain.JsSysEmployee;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.mapper.JsSysEmployeeMapper;
import com.steccn.oa.constant.OaConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【js_sys_employee(员工表)】的数据库操作Service实现
* @createDate 2024-01-03 16:26:50
*/
@Service
public class JsSysEmployeeServiceImpl extends ServiceImpl<JsSysEmployeeMapper, JsSysEmployee>
    implements JsSysEmployeeService{

    @Override
    public Map<String, List<String>> getPostUser(Set<String> postCode) {
        Map<String, List<String>> result = Maps.newHashMap();
        List<JsSysEmployee> list = list(new LambdaQueryWrapper<JsSysEmployee>()
                .select(JsSysEmployee::getEmpNo, JsSysEmployee::getOfficeCode)
                .eq(JsSysEmployee::getStatus, OaConstant.NO.toString())
                .in(JsSysEmployee::getOfficeCode, postCode));
        if(CollUtil.isNotEmpty(list)){
            result = list.stream()
                    .collect(Collectors.groupingBy(
                            JsSysEmployee::getOfficeCode,
                            Collectors.mapping(JsSysEmployee::getEmpNo,Collectors.toList())
                    ));
        }
        return result;
    }
}




