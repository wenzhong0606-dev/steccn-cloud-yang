package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.service.PmsCompleteDeliveryInfoService;
import com.steccn.pms.mapper.PmsCompleteDeliveryInfoMapper;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery_info(出库详情)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteDeliveryInfoServiceImpl extends ServiceImpl<PmsCompleteDeliveryInfoMapper, PmsCompleteDeliveryInfo>
        implements PmsCompleteDeliveryInfoService {

    @Override
    public Page<PmsCompleteDeliveryVO> listByOrderId(PmsCompleteDeliveryDTO dto) {
        return this.baseMapper.listByOrderId(new Page(dto.getCurrent(), dto.getSize()), dto.getProduceOrderId());
    }
}




