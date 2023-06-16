package com.steccn.oa.mapper;

import com.steccn.oa.domain.Hrmdepartment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.oa.dto.OaClassUserNameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Admin
* @description 针对表【hrmdepartment】的数据库操作Mapper
* @createDate 2023-05-30 14:54:41
* @Entity com.steccn.oa.domain.Hrmdepartment
*/
@Mapper
public interface HrmdepartmentMapper extends BaseMapper<Hrmdepartment> {
    /**
     * 查询车间下各班的班组长
     * @param productWorkshopName
     * @return
     */
    List<OaClassUserNameDTO> selectGroupLeader(@Param("productWorkshopName") String productWorkshopName);
}




