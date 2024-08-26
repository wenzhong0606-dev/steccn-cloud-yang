package com.steccn.sales.mapper;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.sales.domain.KsContract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【ks_contract(合同)】的数据库操作Mapper
* @createDate 2024-08-26 14:17:37
* @Entity com.steccn.sales.domain.KsContract
*/
@Mapper
public interface KsContractMapper extends BaseMapper<KsContract> {
    /**
     * 按天分类
     * @param start 开始
     * @param end 结束
     * @return
     */
    List<TupleDTO<String,BigDecimal>> contractCountDay(@Param("start") LocalDate start,
                                                                 @Param("end") LocalDate end);
    /**
     * 按月分类
     * @param start 开始
     * @param end 结束
     * @return
     */
    List<TupleDTO<String,BigDecimal>> contractCountMonth(@Param("start") LocalDate start,
                                                       @Param("end") LocalDate end);

    /**
     * 按周分类
     * @param start 开始
     * @param end 结束
     * @return
     */
    List<TupleDTO<String,BigDecimal>> contractCountWeek(@Param("start") LocalDate start,
                                                       @Param("end") LocalDate end);

    /**
     * 按年分类
     * @param start 开始
     * @param end 结束
     * @return
     */
    List<TupleDTO<String,BigDecimal>> contractCountYear(@Param("start") LocalDate start,
                                                       @Param("end") LocalDate end);
}




