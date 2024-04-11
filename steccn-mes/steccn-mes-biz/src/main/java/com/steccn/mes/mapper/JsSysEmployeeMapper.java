package com.steccn.mes.mapper;

import com.steccn.mes.domain.JsSysEmployee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.mes.vo.MesUserInfoVO;
import com.steccn.pms.dto.PmsPullForceDayCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【js_sys_employee(员工表)】的数据库操作Mapper
* @createDate 2024-01-03 16:26:50
* @Entity com.steccn.mes.domain.JsSysEmployee
*/
@Mapper
public interface JsSysEmployeeMapper extends BaseMapper<JsSysEmployee> {
    /**
     * 获取登录code对应的人员信息
     * @param idSet
     * @return
     */
    List<MesUserInfoVO> selectEmpInfo(@Param("idSet") Set<String> idSet);
}




