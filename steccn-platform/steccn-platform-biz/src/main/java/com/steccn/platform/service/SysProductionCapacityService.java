package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysProductionCapacity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysProductionCapacityAddDTO;
import com.steccn.platform.dto.SysProductionCapacityPageDTO;
import com.steccn.platform.vo.SysProductionCapacityVO;

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
     * @param dto
     * @return
     */
    Boolean addCapacity(SysProductionCapacityAddDTO dto);
}
