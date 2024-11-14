package com.steccn.mes.service;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.JsSysEmployee;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.vo.MesUserInfoVO;

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

    /**
     * 获取用户的机构和岗位
     * @param userName
     * @return t1用户名 t2机构 t3岗位
     */
    List<MesUserInfoVO>  getEmpDepPost(Set<String> userName);

    /**
     * 获取传入depCode下所有的人
     * @param depCode
     * @return
     */
    Map<String, List<String>> getEmpByDepTree(Set<String> depCode);

}
