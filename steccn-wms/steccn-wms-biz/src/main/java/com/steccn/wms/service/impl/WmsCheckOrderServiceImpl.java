package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.mapper.WmsCheckOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author W10
* @description 针对表【wms_check_order(盘点单)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
public class WmsCheckOrderServiceImpl extends ServiceImpl<WmsCheckOrderMapper, WmsCheckOrder>
    implements WmsCheckOrderService{

}




