package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.PmsCheckOrder;
import com.steccn.wms.service.PmsCheckOrderService;
import com.steccn.wms.mapper.PmsCheckOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pms_check_order(盘点单)】的数据库操作Service实现
* @createDate 2022-11-29 19:32:10
*/
@Service
public class PmsCheckOrderServiceImpl extends ServiceImpl<PmsCheckOrderMapper, PmsCheckOrder>
    implements PmsCheckOrderService{

}




