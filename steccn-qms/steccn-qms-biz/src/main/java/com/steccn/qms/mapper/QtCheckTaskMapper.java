package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtCheckTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author AndrewLuo
* @description 针对表【qt_check_task(巡检任务表)】的数据库操作Mapper
* @createDate 2022-09-02 08:54:09
* @Entity com.steccn.qms.domain.QtCheckTask
*/
@Mapper
public interface QtCheckTaskMapper extends BaseMapper<QtCheckTask> {

    /**
     * 通过任务类型查找数据
     * @param checkTypeCode 检验类型code
     * @param status 状态
     * @return
     */
    @Select("<script> SELECT a.id AS id,a.type AS type,a.material_id AS materialId,a.workshop_id AS workshopId ,a.product_line_id AS productLineId ,a.status AS status FROM qt_check_task a LEFT JOIN qt_material_link_check_plan b on a.material_id = b.material_id WHERE b.del_flag = 0 AND b.check_type_code = #{checkTypeCode} AND a.del_flag = 0 AND (a.status = #{status} <if test = 'status == 1'> or a.status = 4 </if>) </script>")
    public List<QtCheckTask> selectByTaskType(@Param("checkTypeCode") Integer checkTypeCode, @Param("status") Integer status);
}




