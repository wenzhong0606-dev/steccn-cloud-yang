package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.FurtherProduceReportDataVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsCompleteStorageInfo
 */
@Mapper
public interface PmsCompleteStorageInfoMapper extends BaseMapper<PmsCompleteStorageInfo> {

    /**
     * 根据订单id获取完工入库分页列表
     *
     * @param page
     * @param produceOrderId
     * @return
     */
    Page<PmsCompleteStorageVO> pageByOrderId(Page page, @Param("orderId") Long produceOrderId);

    /**
     * 查询指定时间，物料集合在不同班次下的入库数量
     *
     * @param materialSet 物料集合
     * @param date        指定日期
     * @return
     */
    List<FurtherProduceReportDataVO> materialList(@Param("materialSet") Set<Long> materialSet, @Param("date") LocalDate date);

    /**
     * 同一类型下，是否存在箱号
     * @param typeId 类型id
     * @param caseNoSet 箱号集合
     * @return 条数
     */
    long existCaseNo(@Param("typeId") Long typeId, @Param("caseNoSet") Set<String> caseNoSet);
}




