package com.steccn.oa.service;

import com.steccn.oa.domain.Hrmdepartment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaDepartmentVO;

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

}
