package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtCheckPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.qms.domain.QtCheckTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author AndrewLuo
* @description 针对表【qt_check_plan(检验方案表)】的数据库操作Mapper
* @createDate 2022-09-02 08:54:09
* @Entity com.steccn.qms.domain.QtCheckPlan
*/
@Mapper
public interface QtCheckPlanMapper extends BaseMapper<QtCheckPlan> {
    /**
     * 通过计划id查询绑定的物料
     * @param planId
     * @return
     */
    @Select("SELECT COUNT(*) FROM qt_material_link_check_plan a LEFT JOIN qt_check_task b ON a.material_id = b.material_id WHERE a.check_plan_id = #{planId} AND a.del_flag = 0 AND b.del_flag = 0 AND b.status = 1 ")
    Long  CountLindkedPlan(@Param("planId") Integer planId);
}




