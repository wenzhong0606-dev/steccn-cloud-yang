package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysProductionCapacity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysProductionCapacityAddDTO;
import com.steccn.platform.dto.SysProductionCapacityInfoDTO;
import com.steccn.platform.dto.SysProductionCapacityPageDTO;
import com.steccn.platform.vo.SysProductionCapacityDetailVO;
import com.steccn.platform.vo.SysProductionCapacityInfoVO;
import com.steccn.platform.vo.SysProductionCapacityVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_production_capacity(标准产能表)】的数据库操作Service
 * @createDate 2023-12-19 16:50:56
 */
public interface SysProductionCapacityService extends IService<SysProductionCapacity> {

    /**
     * 标准产能的分页列表
     *
     * @param dto 请求类
     * @return
     */
    Page<SysProductionCapacityVO> capacityPage(SysProductionCapacityPageDTO dto);

    /**
     * 新增标准产能
     *
     * @param dto
     * @return
     */
    Boolean addCapacity(SysProductionCapacityAddDTO dto);

    /**
     * 删除标准产能
     *
     * @param id 主键
     * @return
     */
    Boolean deleteCapacity(Integer id);

    /**
     * 标准产能的明细
     * @param id 主键
     * @return
     */
    List<SysProductionCapacityInfoVO> infoCapacity(Integer id);

    /**
     * 标准产能明细更新
     * @param list 数据
     * @return
     */
    Boolean updateCapacity(List<SysProductionCapacityInfoDTO> list);

    /**
     * 查询工位指定日期区间的标准产能
     *
     * @param stationIdSet 工位集合
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<SysProductionCapacityDetailVO> capacityStation(Set<Integer> stationIdSet, LocalDate start, LocalDate end);
}
