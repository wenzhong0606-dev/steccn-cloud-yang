package com.steccn.dp.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.dp.domain.AdsPmsCompleteStorage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.qms.constant.QtManagerConstant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ads_pms_complete_storage(各车间产线每日入库统计)】的数据库操作Service
 * @createDate 2023-11-29 16:09:13
 */
public interface AdsPmsCompleteStorageService extends IService<AdsPmsCompleteStorage> {

    /**
     * 入库信息-深加工
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> sjgInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit);

    /**
     * 入库信息-基片
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> jpInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit);

    /**
     * 深加工还是基片
     * @param dto
     * @return
     */
    Map<String, BigDecimal> inStorageSum(DpPullForceDTO dto);
}
