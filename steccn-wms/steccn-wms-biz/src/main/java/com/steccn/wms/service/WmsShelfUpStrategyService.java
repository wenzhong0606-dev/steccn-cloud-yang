package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsShelfUpStrategyService extends IService<WmsShelfUpStrategy> {

    /***
     * 获取上架策略列表
     * @param dto
     * @return
     */
    Page<WmsShelfUpStrategy> getShelfUpStrategyList( WmsShelfUpStrategyDTO dto);

    /***
     * 新增或者编辑上架策略
     * @param dto
     * @return
     */
    Boolean addOrUpdateShelfUpStrategy( WmsShelfUpStrategy dto);

    /***
     * 删除上架策略
     * @param dto
     * @return
     */
    Boolean delShelfUpStrategy( WmsShelfUpStrategy dto);
}
