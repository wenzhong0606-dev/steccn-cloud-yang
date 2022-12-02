package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.vo.WmsCheckOrderVO;

/**
* @author W10
* @description 针对表【wms_check_order(盘点单)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsCheckOrderService extends IService<WmsCheckOrder> {

    /***
     * 获取盘点管理列表
     * @param dto
     * @return
     */
    Page<WmsCheckOrderVO> getCheckOrderList(WmsCheckOrderDTO dto);

    /***
     * 新增或者编辑盘点管理
     * @param dto
     * @return
     */
    Boolean addOrUpdateCheckOrder(WmsCheckOrder dto);

    /***
     * 删除盘点管理
     * @param dto
     * @return
     */
    Boolean delCheckOrder( WmsCheckOrder dto);
}
