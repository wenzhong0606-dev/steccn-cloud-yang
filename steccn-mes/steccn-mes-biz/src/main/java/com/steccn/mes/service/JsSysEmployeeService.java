package com.steccn.mes.service;

import com.steccn.mes.domain.JsSysEmployee;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【js_sys_employee(员工表)】的数据库操作Service
* @createDate 2024-01-03 16:26:50
*/
public interface JsSysEmployeeService extends IService<JsSysEmployee> {

    /**
     * 获取岗位下的用户
     * @param postCode 岗位code
     * @return
     */
    Map<String, List<String>> getPostUser(Set<String> postCode);

}
