package com.steccn.qms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseMapper;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.QtDefectItemInfoVO;
import com.steccn.qms.vo.QtIQCAcceptabilityDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author AndrewLuo
 * @description 针对表【qt_check_task(巡检任务表)】的数据库操作Mapper
 * @createDate 2022-09-02 08:54:09
 * @Entity com.steccn.qms.domain.QtCheckTask
 */
@Mapper
public interface QtCheckTaskMapper extends MPJBaseMapper<QtCheckTask> {

    /**
     * 通过任务类型查找数据
     *
     * @param checkTypeCode 检验类型code
     * @param status        状态
     * @return
     */
    @Select("<script> SELECT a.id AS id,a.type AS type,a.material_id AS materialId,a.workshop_id AS workshopId ,a.product_line_id AS productLineId ,a.status AS status,a.spot_time AS spotTime FROM qt_check_task a LEFT JOIN qt_material_link_check_plan b on a.material_id = b.material_id WHERE b.del_flag = 0 AND b.check_type_code = #{checkTypeCode} AND a.del_flag = 0 AND (a.status = #{status} <if test = 'status == 1'> or a.status = 4 </if>) </script>")
    public List<QtCheckTask> selectByTaskType(@Param("checkTypeCode") Integer checkTypeCode, @Param("status") Integer status);

    /**
     * 获取合格率的基础数据表
     *
     * @param produceLineSet 产线集合
     * @param dto            dto
     * @return list 基础数据表
     */
    List<QtCheckTask> rateDataList(@Param("line") Set<Integer> produceLineSet, @Param("dto") QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 获取合格来料检合格率数据
     *
     * @param page
     * @param dto
     * @return
     */
    Page<QtIQCAcceptabilityDetailVO> pageList(@Param("page") Page page, @Param("dto") QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 获取合格来料检合格率数据
     *
     * @param dto
     * @return
     */
    List<QtIQCAcceptabilityDetailVO> list(@Param("dto") QtSubOrDeepAcceptabilityDTO dto);

    /**
     * 获取检验项为外观状态的缺陷项数据
     *
     * @param apparentCondition
     * @param taskIds
     * @return
     */
    List<QtDefectItemInfoVO> getDefectItemList(@Param("apparentCondition") String apparentCondition, @Param("taskIds") List<Long> taskIds);


    /**
     * 获取所有的配料检数
     *
     * @param morning 早上
     * @param night   晚上
     * @return
     */
    List<QtDaySamplingDTO> samplingList(@Param("morning") LocalDateTime morning, @Param("night") LocalDateTime night);

    /**
     * 获取来料检
     *
     * @param itemIdSet 检验项集合
     * @return 所有的来料检
     */
    List<QtIncomingCheckDTO> comingList(@Param("itemIdSet") Set<Integer> itemIdSet);

    /**
     * 其余检的数据
     *
     * @param checkType 校验类型
     * @param itemIdSet 检验项集合
     * @param start     开始时间
     * @param end       结束时间
     * @return list
     */
    List<QtOtherCheckDTO> otherList(@Param("checkType") Integer checkType, @Param("itemIdSet") Set<Integer> itemIdSet, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 获取巡检的厚度平均值的平均值
     *
     * @param productLineList 产线集合
     * @param thickItemIdList 检验项id集合
     * @param start           开始时间
     * @param end             结束时间
     * @return 集合
     */
    List<QtThickAvgDTO> thickAvg(@Param("lineIdList") List<Integer> productLineList,
                                 @Param("itemIdSet") List<Integer> thickItemIdList,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);
}




