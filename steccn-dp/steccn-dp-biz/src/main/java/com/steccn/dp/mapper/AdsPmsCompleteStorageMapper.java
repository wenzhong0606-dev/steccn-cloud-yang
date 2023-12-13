package com.steccn.dp.mapper;

import com.steccn.common.dto.TripleDTO;
import com.steccn.dp.domain.AdsPmsCompleteStorage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ads_pms_complete_storage(各车间产线每日入库统计)】的数据库操作Mapper
* @createDate 2023-11-29 16:09:13
* @Entity com.steccn.dp.domain.AdsPmsCompleteStorage
*/
@Mapper
public interface AdsPmsCompleteStorageMapper extends BaseMapper<AdsPmsCompleteStorage> {

    /**
     * 完工入库-日
     * @param start 开始时间
     * @param end 结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialDay(@Param("start") LocalDate start,
                                                     @Param("end") LocalDate end,
                                                     @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 完工入库-周
     * @param start 开始时间
     * @param end 结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialWeek(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 完工入库-月
     * @param start 开始时间
     * @param end 结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialMonth(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);

    /**
     * 完工入库-年
     * @param start 开始时间
     * @param end 结束时间
     * @param workshopIdSet 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialYear(@Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end,
                                                                    @Param("workshopIdSet") Set<Integer> workshopIdSet);
}




