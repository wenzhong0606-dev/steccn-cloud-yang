package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.mapper.WmsInventoryMapper;
import org.springframework.stereotype.Service;

/**
* @author W10
* @description 针对表【wms_inventory(仓储库存)】的数据库操作Service实现
* @createDate 2022-12-09 11:18:09
*/
@Service
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory>
    implements WmsInventoryService{

}




