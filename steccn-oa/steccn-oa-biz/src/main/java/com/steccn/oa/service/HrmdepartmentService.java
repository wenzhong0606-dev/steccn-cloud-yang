package com.steccn.oa.service;

import com.steccn.oa.domain.Hrmdepartment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaClassUserNameDTO;
import com.steccn.oa.vo.OaDepartmentTreeVO;
import com.steccn.oa.vo.OaDepartmentVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【hrmdepartment】的数据库操作Service
* @createDate 2023-05-30 14:54:41
*/
public interface HrmdepartmentService extends IService<Hrmdepartment> {

    /**
     * 根据部门id获取信息
     * @param id
     * @return
     */
    OaDepartmentVO getOaDepartmentById(Integer id);

    /**
     * 获取班组长
     * @param productWorkShop 车间名字集合
     * @return
     */
    Map<String, List<OaClassUserNameDTO>> getClassUserName(Set<String> productWorkShop);

    /**
     * 分部id
     * @param subCompanyId
     * @return
     */
    List<OaDepartmentTreeVO> getDepartmentTree(Integer subCompanyId);
}
