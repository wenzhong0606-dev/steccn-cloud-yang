package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.domain.PmsLaborProtectionRecord;
import com.steccn.pms.dto.PmsLaborMaterialReceiptDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
* @author Admin
* @description 针对表【pms_labor_protection_record(劳保记录表)】的数据库操作Mapper
* @createDate 2024-04-15 10:59:25
* @Entity com.steccn.pms.domain.PmsLaborProtectionRecord
*/
@Mapper
public interface PmsLaborProtectionRecordMapper extends BaseMapper<PmsLaborProtectionRecord> {

    /**
     * 获取一段时间内用户的领用记录
     * @param loginCode
     * @param startTime
     * @param endTime
     * @param materialId
     * @return
     */
    PmsLaborMaterialReceiptDTO selectReceiptNum(@Param("loginCode")String loginCode, @Param("start")LocalDateTime startTime, @Param("end")LocalDateTime endTime,@Param("materialId")Long materialId);
}




