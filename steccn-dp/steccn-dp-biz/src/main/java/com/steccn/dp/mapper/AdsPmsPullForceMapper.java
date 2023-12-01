package com.steccn.dp.mapper;

import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsPmsPullForce;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ads_pms_pull_force(每日拉引量统计)】的数据库操作Mapper
 * @createDate 2023-11-29 16:09:13
 * @Entity com.steccn.dp.domain.AdsPmsPullForce
 */
@Mapper
public interface AdsPmsPullForceMapper extends BaseMapper<AdsPmsPullForce> {

    /**
     * 拉引量-月
     *
     * @param start         开始日期
     * @param end           结束日期
     * @param workshopIdSet 车间接口
     * @return
     */
    List<TupleDTO<String, BigDecimal>> pullForceDay(@Param("start") LocalDate start,
                                                     @Param("end") LocalDate end,
                                                     @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 拉引量-月
     *
     * @param start         开始日期
     * @param end           结束日期
     * @param workshopIdSet 车间接口
     * @return
     */
    List<TupleDTO<String, BigDecimal>> pullForceMonth(@Param("start") LocalDate start,
                                                    @Param("end") LocalDate end,
                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);
}




