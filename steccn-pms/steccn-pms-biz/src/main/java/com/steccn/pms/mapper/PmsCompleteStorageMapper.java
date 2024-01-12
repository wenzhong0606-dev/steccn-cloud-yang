package com.steccn.pms.mapper;

import com.steccn.common.dto.TripleDTO;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteStorage
*/
@Mapper
public interface PmsCompleteStorageMapper extends BaseMapper<PmsCompleteStorage> {


    /**
     * 完工入库-日
     * @param start 开始时间
     * @param end 结束时间
     * @param lineIdList 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialDay(@Param("start") LocalDateTime start,
                                                                  @Param("end") LocalDateTime end,
                                                                  @Param("lineIdList") Set<Integer> lineIdList);

    /**
     * 完工入库-周
     * @param start 开始时间
     * @param end 结束时间
     * @param lineIdList 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialWeek(@Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end,
                                                                   @Param("lineIdList") Set<Integer> lineIdList);

    /**
     * 完工入库-月
     * @param start 开始时间
     * @param end 结束时间
     * @param lineIdList 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialMonth(@Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end,
                                                                    @Param("lineIdList") Set<Integer> lineIdList);

    /**
     * 完工入库-年
     * @param start 开始时间
     * @param end 结束时间
     * @param lineIdList 产线集合
     * @return
     */
    List<TripleDTO<String,Long, BigDecimal>> inStorageMaterialYear(@Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end,
                                                                   @Param("lineIdList") Set<Integer> lineIdList);
}




