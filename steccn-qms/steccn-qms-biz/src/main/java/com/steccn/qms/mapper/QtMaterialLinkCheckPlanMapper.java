package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author AndrewLuo
* @description 针对表【qt_material_link_check_plan(物料关联检验计划表)】的数据库操作Mapper
* @createDate 2022-09-02 08:54:09
* @Entity com.steccn.qms.domain.QtMaterialLinkCheckPlan
*/
@Mapper
public interface QtMaterialLinkCheckPlanMapper extends BaseMapper<QtMaterialLinkCheckPlan> {
    /**
     * 通过检验Code查询任务
     * @param typeCode 来料检1/首检2/在线检3/巡检4/出货检5
     * @return
     */
    @Select("SELECT COUNT(*) FROM qt_material_link_check_plan a LEFT JOIN qt_check_task b on a.check_plan_id = b.check_plan_id WHERE a.check_type_code = #{typeCode} AND a.del_flag = 0 AND b.del_flag = 0 AND b.status = 0")
    Long  CountTaskByTaskType(@Param("typeCode") Integer typeCode);
}




