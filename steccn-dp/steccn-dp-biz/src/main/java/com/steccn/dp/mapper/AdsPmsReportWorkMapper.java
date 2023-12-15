package com.steccn.dp.mapper;

import com.steccn.common.dto.TripleDTO;
import com.steccn.dp.domain.AdsPmsReportWork;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ads_pms_report_work(各车间产线工序每日报工统计)】的数据库操作Mapper
 * @createDate 2023-11-29 16:09:13
 * @Entity com.steccn.dp.domain.AdsPmsReportWork
 */
@Mapper
public interface AdsPmsReportWorkMapper extends BaseMapper<AdsPmsReportWork> {

    /**
     * 产量-日-基片
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> jpCompleteMaterialDay(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-日-深加工
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> sjgCompleteMaterialDay(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-周-基片
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> jpCompleteMaterialWeek(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-周-深加工
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> sjgCompleteMaterialWeek(@Param("start") LocalDate start,
                                                                     @Param("end") LocalDate end,
                                                                     @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-月-基片
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> jpCompleteMaterialMonth(@Param("start") LocalDate start,
                                                                      @Param("end") LocalDate end,
                                                                      @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-月-深加工
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> sjgCompleteMaterialMonth(@Param("start") LocalDate start,
                                                                      @Param("end") LocalDate end,
                                                                      @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-年-基片
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> jpCompleteMaterialYear(@Param("start") LocalDate start,
                                                                      @Param("end") LocalDate end,
                                                                      @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 产量-年-深加工
     *
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> sjgCompleteMaterialYear(@Param("start") LocalDate start,
                                                                       @Param("end") LocalDate end,
                                                                       @Param("workshopIdSet") Set<Integer> workshopIdSet);



    /**
     * 实际使用数：柱状图，取自磨边工位报工加工数，转换为平方米
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> actualUseMaterialDay(@Param("start") LocalDate start,
                                                                   @Param("end") LocalDate end,
                                                                   @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 实际使用数：柱状图，取自磨边工位报工加工数，转换为平方米
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> actualUseMaterialWeek(@Param("start") LocalDate start,
                                                                     @Param("end") LocalDate end,
                                                                     @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 实际使用数：柱状图，取自磨边工位报工加工数，转换为平方米
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> actualUseMaterialMonth(@Param("start") LocalDate start,
                                                                     @Param("end") LocalDate end,
                                                                     @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 实际使用数：柱状图，取自磨边工位报工加工数，转换为平方米
     * @param start         开始时间
     * @param end           结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String, Long, BigDecimal>> actualUseMaterialYear(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);
}




