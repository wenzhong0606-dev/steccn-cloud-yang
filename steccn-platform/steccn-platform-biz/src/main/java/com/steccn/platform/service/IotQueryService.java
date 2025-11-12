package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.dto.IotDeviceDTO;
import com.steccn.platform.vo.IotIdNameVO;
import com.steccn.platform.vo.IotPointVO;

import java.util.List;

/**
 * 查询数采接口
 *
 * @author AndrewLuo
 * @date 2025/11/11
 */
public interface IotQueryService {
    /**
     * 查询设备分组
     * @return
     */
    List<IotIdNameVO> queryEqpGroup();

    /**
     * 查询设备信息
     * @param dto
     * @return
     */
    Page<IotIdNameVO> queryDeviceInfo(IotDeviceDTO dto);

    /**
     * 查询点位信息
     * @param dto
     * @return
     */
    Page<IotPointVO> queryPointInfo(IotDeviceDTO dto);
}
