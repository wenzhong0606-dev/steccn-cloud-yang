package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProcessOutStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.vo.PmsProcessOutStationVO;

/**
* @author yequan
* @description 针对表【pms_process_out_station(出站信息)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProcessOutStationService extends IService<PmsProcessOutStation> {


    /**
     * 生产订单出站分页列表
     * @param dto
     * @return
     */
    Page<PmsProcessOutStationVO> outPage(PmsProcessOutStationPageDTO dto);
}
