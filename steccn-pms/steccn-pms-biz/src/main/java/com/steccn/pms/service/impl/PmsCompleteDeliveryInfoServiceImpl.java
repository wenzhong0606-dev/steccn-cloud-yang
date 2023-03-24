package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.dto.FeedQuantityDTO;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.service.PmsCompleteDeliveryInfoService;
import com.steccn.pms.mapper.PmsCompleteDeliveryInfoMapper;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<FeedQuantityDTO, BigDecimal> listQuantity(List<FeedQuantityDTO> feedList) {
        List<FeedQuantityDTO> feedQuantityDTOS = this.baseMapper.listQuantity(feedList);
        Map<FeedQuantityDTO, BigDecimal> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(feedQuantityDTOS)) {
            feedQuantityDTOS.forEach(feedQuantityDTO -> {
                        map.put(feedQuantityDTO, feedQuantityDTO.getQuantity());
                    }
            );
        }
        //1.自定义sql 获取投料信息
        return map;
    }

    @Override
    public BigDecimal getQuantity(Long id) {
        return baseMapper.getQuantity(id);
    }
}




