package com.steccn.platform.service;

import com.steccn.platform.domain.IotTrendGroupDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.IotTrendDetailDTO;
import com.steccn.platform.vo.IotTrendDetailVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【iot_trend_group_detail(趋势组详情表)】的数据库操作Service
* @createDate 2025-11-12 16:06:04
*/
public interface IotTrendGroupDetailService extends IService<IotTrendGroupDetail> {
    /**
     * 保存趋势组点位信息
     * @param dto
     * @return
     */
    Boolean saveInfo(List<IotTrendDetailDTO> dto);

    /**
     * 信息点位
     * @param id
     * @return
     */
    List<IotTrendDetailVO> infos(Integer id);
}
