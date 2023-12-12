package com.steccn.ems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.vo.EpMeterReadDetailsVO;
import com.steccn.ems.vo.MeterReadMapperVO;
import com.steccn.ems.vo.ReadCorrectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_meter_read(抄表)】的数据库操作Mapper
* @createDate 2022-11-30 15:05:32
* @Entity com.steccn.ems.domain.EpMeterRead
*/
@Mapper
public interface EpMeterReadMapper extends BaseMapper<EpMeterRead> {


    /**
     * 根据类型和抄表月份查询
     *
     * @param startDate
     * @param endDate
     * @param meterType
     * @return
     */
    List<MeterReadMapperVO> selectAllByReadMonth(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate, @Param("meterType") Integer meterType);

    /**
     * 根据仪表id和抄表日期查询
     *
     * @param readDate
     * @param meterId
     * @return
     */
    EpMeterReadDetailsVO selectByReadDateAndMeterId(@Param("readDate")String readDate,@Param("meterId") Integer meterId);

    /**
     * 查询每个仪表的最新两条数据
     *
     * @return
     */
    List<ReadCorrectVO> selectByMeterId();

    /**
     * 根据仪表类型查询数据
     *
     * @param meterType
     * @return
     */
    List<MeterReadMapperVO> selectAllByMeterType(@Param("meterType") Integer meterType);

    /**
     * 获取最近读数-求和
     * @param meterIdSet 仪表集合
     * @param lastDate 日期
     * @return
     */
    List<EpMeterRead> selectLastValue(@Param("meterIdSet") Set<Integer> meterIdSet, @Param("lastDate") LocalDate lastDate);

    /**
     * 获取最近读数-求最大值
     * @param meterIdSet 仪表集合
     * @param lastDate 日期
     * @return
     */
    List<EpMeterRead> selectLastValueMax(@Param("meterIdSet") Set<Integer> meterIdSet, @Param("lastDate") LocalDate lastDate);

}




