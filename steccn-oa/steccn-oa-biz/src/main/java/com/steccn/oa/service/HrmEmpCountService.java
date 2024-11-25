package com.steccn.oa.service;

import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.oa.domain.HrmEmpCount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【hrm_emp_count】的数据库操作Service
* @createDate 2024-11-25 13:21:36
*/
public interface HrmEmpCountService extends IService<HrmEmpCount> {
    PmsOperateEnergyVO countDash(LocalDate day);
}
