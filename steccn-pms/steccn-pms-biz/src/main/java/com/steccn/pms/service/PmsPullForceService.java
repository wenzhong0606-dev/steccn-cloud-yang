package com.steccn.pms.service;

import com.steccn.pms.domain.PmsPullForce;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service
 * @createDate 2022-11-02 10:10:08
 */
public interface PmsPullForceService extends IService<PmsPullForce> {

    /**
     * 拉引量列表
     *
     * @param workshopId
     * @return
     */
    List<PmsPullForceVO> list(Integer workshopId, String monthDate);

    /**
     * 拉引量详情
     *
     * @param workshopId
     * @return
     */
    List<PmsPullForceDetailsVO> details(Integer workshopId, String pullForceDate);

    /**
     * 拉引量批量编辑
     *
     * @param dtoList
     * @return
     */
    Boolean editBatch(List<PmsPullForceEditDTO> dtoList);


    /**
     * 获取一个产线2个班次的拉引量
     *
     * @param productLineId 产线id
     * @param date          日期
     * @param classGroupIdList 班次id集合
     * @return list
     */
    Map<Long, PmsPullForce> pullForceDay(Integer productLineId, LocalDate date, List<Long> classGroupIdList);

}
