package com.steccn.platform.service;

import com.steccn.platform.domain.IotTrendGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.vo.IotPointVO;
import com.steccn.platform.vo.IotTrendGroupVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【iot_trend_group(趋势组表)】的数据库操作Service
* @createDate 2025-11-12 14:02:30
*/
public interface IotTrendGroupService extends IService<IotTrendGroup> {
    /**
     * 新增趋势组
     * @param name
     * @return
     */
    Boolean add(String name);

    /**
     * 趋势组列表
     * @return
     */
    List<IotTrendGroupVO> IotTrendGroup();

    /**
     * 删除趋势组
     * @return
     */
    Boolean deleteGroup(Integer id);
}
