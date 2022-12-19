package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.wms.domain.WmsLogisticsDetail;
import com.steccn.wms.service.WmsLogisticsDetailService;
import com.steccn.wms.mapper.WmsLogisticsDetailMapper;
import com.steccn.wms.vo.WmsLogisticsDetailVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author W10
 * @description 针对表【wms_logistics_detail】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsLogisticsDetailServiceImpl extends ServiceImpl<WmsLogisticsDetailMapper, WmsLogisticsDetail>
        implements WmsLogisticsDetailService {

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    public List<WmsLogisticsDetailVO> listByLogisticsId(Long id) {
        List<WmsLogisticsDetailVO> resList = Lists.newArrayList();
        if (Objects.nonNull(id)) {
            List<WmsLogisticsDetail>  list = list(new LambdaQueryWrapper<WmsLogisticsDetail>().in(WmsLogisticsDetail::getLogisticsId, id));
            if (CollUtil.isNotEmpty(list)) {
                List<Long> materialIds = Lists.newArrayList();
                list.forEach(info -> {
                    materialIds.add(info.getMaterialId());
                });
                Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
                EqpSpareVO eqpSpareVO = new EqpSpareVO();
                list.forEach(info -> {
                    EqpSpareVO orDefault = materialMap.getOrDefault(info.getMaterialId(), eqpSpareVO);
                    WmsLogisticsDetailVO wmsLogisticsDetailVO = new WmsLogisticsDetailVO();
                    BeanUtil.copyProperties(info,wmsLogisticsDetailVO);
                    wmsLogisticsDetailVO.setMaterialCode(orDefault.getCode());
                    wmsLogisticsDetailVO.setMaterialName(orDefault.getName());
                    resList.add(wmsLogisticsDetailVO);
                });
            }
        }
        return resList;
    }
}




