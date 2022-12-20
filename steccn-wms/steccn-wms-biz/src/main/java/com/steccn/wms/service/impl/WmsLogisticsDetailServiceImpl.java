package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.wms.domain.WmsLogisticsDetail;
import com.steccn.wms.domain.WmsLogisticsOrder;
import com.steccn.wms.dto.WmsLogisticsDetailDTO;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.service.WmsLogisticsDetailService;
import com.steccn.wms.mapper.WmsLogisticsDetailMapper;
import com.steccn.wms.vo.WmsLogisticsDetailVO;
import com.steccn.wms.vo.WmsLogisticsOrderVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public List<WmsLogisticsDetailVO> listByLogisticsId(Long id) {
        List<WmsLogisticsDetailVO> resList = Lists.newArrayList();
        if (Objects.nonNull(id)) {
            List<WmsLogisticsDetail> list = list(new LambdaQueryWrapper<WmsLogisticsDetail>().in(WmsLogisticsDetail::getLogisticsId, id));
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
                    BeanUtil.copyProperties(info, wmsLogisticsDetailVO);
                    wmsLogisticsDetailVO.setMaterialCode(orDefault.getCode());
                    wmsLogisticsDetailVO.setMaterialName(orDefault.getName());
                    resList.add(wmsLogisticsDetailVO);
                });
            }
        }
        return resList;
    }

    @Override
    public Page<WmsLogisticsDetailVO> receiptInfo(WmsLogisticsDetailDTO dto) {
        LambdaQueryWrapper<WmsLogisticsDetail> eq = new LambdaQueryWrapper<WmsLogisticsDetail>().eq(WmsLogisticsDetail::getLogisticsId, dto.getLogisticsId());
        Page<WmsLogisticsDetail> page = page(new Page<>(dto.getCurrent(), dto.getSize()), eq);
        Page<WmsLogisticsDetailVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        List<WmsLogisticsDetailVO> list = BeanUtil.copyToList(page.getRecords(), WmsLogisticsDetailVO.class);
        if (CollUtil.isNotEmpty(list)) {
            List<Long> materialIds = Lists.newArrayList();
            Set<Long> dictIds = Sets.newHashSet();
            list.forEach(detailVO -> {
                materialIds.add(detailVO.getMaterialId());
                dictIds.add(detailVO.getWeightUnit());
                dictIds.add(detailVO.getVolumeUnit());
                dictIds.add(detailVO.getUnitId());
            });
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
            list.forEach(detailVO -> {
                detailVO.setUnpackFlag(0);
                detailVO.setVolumeUnitName(dictMap.get(detailVO.getVolumeUnit()));
                detailVO.setWeightUnitName(dictMap.get(detailVO.getWeightUnit()));
                detailVO.setMaterialCode(materialMap.get(detailVO.getMaterialId()).getCode());
                detailVO.setMaterialName(materialMap.get(detailVO.getMaterialId()).getName());
                detailVO.setUnitName(dictMap.get(detailVO.getUnitId()));
            });
        }
        return resPage.setRecords(list);
    }

    @Override
    public Boolean edit(WmsLogisticsOrderDTO dto) {
        List<Long> ids = Lists.newArrayList();
        List<WmsLogisticsDetail> detailList = Lists.newArrayList();
        dto.getList().forEach(detail -> {
            WmsLogisticsDetail wmsLogisticsDetail = new WmsLogisticsDetail();
            BeanUtil.copyProperties(detail, wmsLogisticsDetail);
            if (Objects.nonNull(detail.getId())) {
                //编辑
                ids.add(detail.getId());
            } else {
                //新增
                wmsLogisticsDetail.setLogisticsId(dto.getId());
            }
            detailList.add(wmsLogisticsDetail);
        });
        LambdaQueryWrapper<WmsLogisticsDetail> wrapper = new LambdaQueryWrapper<WmsLogisticsDetail>()
                .eq(WmsLogisticsDetail::getLogisticsId, dto.getId())
                .notIn(CollUtil.isNotEmpty(ids), WmsLogisticsDetail::getId, ids);
        remove(wrapper);
        return saveOrUpdateBatch(detailList);
    }
}




