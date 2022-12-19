package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.WmsLogisticsTransportation;
import com.steccn.wms.service.WmsLogisticsTransportationService;
import com.steccn.wms.mapper.WmsLogisticsTransportationMapper;
import com.steccn.wms.vo.WmsLogisticsTransportationVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author W10
 * @description 针对表【wms_logistics_transportation(运输方式)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
public class WmsLogisticsTransportationServiceImpl extends ServiceImpl<WmsLogisticsTransportationMapper, WmsLogisticsTransportation>
        implements WmsLogisticsTransportationService {

    @Override
    public List<WmsLogisticsTransportationVO> listByLogisticsId(Long id) {
        List<WmsLogisticsTransportationVO> list = Lists.newArrayList();
        if (Objects.nonNull(id)){
            List<WmsLogisticsTransportation> logisticsTransportations = list(new LambdaQueryWrapper<WmsLogisticsTransportation>().in(WmsLogisticsTransportation::getLogisticsId, id));
            if (CollUtil.isNotEmpty(logisticsTransportations)){
                logisticsTransportations.forEach( way ->{
                    WmsLogisticsTransportationVO wmsLogisticsTransportationVO = new WmsLogisticsTransportationVO();
                    BeanUtil.copyProperties(way,wmsLogisticsTransportationVO);
                    list.add(wmsLogisticsTransportationVO);
                });
            }
        }
        return list;
    }
}




