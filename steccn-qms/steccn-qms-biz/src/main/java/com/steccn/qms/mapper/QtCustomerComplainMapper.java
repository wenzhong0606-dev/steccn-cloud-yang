package com.steccn.qms.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.steccn.qms.domain.QtCustomerComplain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.qms.dto.QtCustomerComplaintReportResDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【qms_customer_complain(客诉记录表)】的数据库操作Mapper
 * @createDate 2023-03-15 10:16:54
 * @Entity com.steccn.qms.domain.QmsCustomerComplain
 */
@Mapper
public interface QtCustomerComplainMapper extends MPJBaseMapper<QtCustomerComplain> {

    /**
     * 客诉报表
     *
     * @param start            开始日期
     * @param end              结束日期
     * @param customerIdList   客户主键集合
     * @param materialIdList   物料主键集合
     * @param complainTypeList 客诉类型集合
     * @return
     */
    List<QtCustomerComplaintReportResDTO> report(@Param("start") LocalDate start,
                                                 @Param("end") LocalDate end,
                                                 @Param("customerIdList") List<Integer> customerIdList,
                                                 @Param("materialIdList") List<Long> materialIdList,
                                                 @Param("complainTypeList") List<Integer> complainTypeList);
}




