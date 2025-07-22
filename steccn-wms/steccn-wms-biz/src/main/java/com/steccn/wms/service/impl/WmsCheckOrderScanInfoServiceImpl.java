package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.domain.WmsCheckOrderInfo;
import com.steccn.wms.domain.WmsCheckOrderScanInfo;
import com.steccn.wms.dto.WmsCheckOrderScanInfoDTO;
import com.steccn.wms.dto.WmsCheckOrderScanInfoListDTO;
import com.steccn.wms.service.WmsCheckOrderScanInfoService;
import com.steccn.wms.mapper.WmsCheckOrderScanInfoMapper;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import com.steccn.wms.vo.WmsCheckOrderScanInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple3;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 34872
 * @description 针对表【wms_check_order_scan_info(盘点扫描明细)】的数据库操作Service实现
 * @createDate 2024-06-07 12:46:50
 */
@Service
@RequiredArgsConstructor
public class WmsCheckOrderScanInfoServiceImpl extends ServiceImpl<WmsCheckOrderScanInfoMapper, WmsCheckOrderScanInfo>
        implements WmsCheckOrderScanInfoService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final WmsCheckOrderService wmsCheckOrderService;

    @Override
    public Boolean addScanCheckInfo(WmsCheckOrderScanInfoDTO dto) {
        Assert.isTrue(Objects.nonNull(dto), "扫描明细不可为空");
        WmsCheckOrder byId = wmsCheckOrderService.getById(dto.getCheckOrderId());
        if(byId.getStatus().equals(4) || byId.getStatus().equals(2)){
            throw new BusinessException("已归档状态和已完成状态的单据不能添加");
        }
        remove(new LambdaQueryWrapper<WmsCheckOrderScanInfo>().eq(WmsCheckOrderScanInfo::getCheckOrderId, dto.getCheckOrderId())
                .eq(WmsCheckOrderScanInfo::getCaseNo, dto.getCaseNo()));
        WmsCheckOrderScanInfo wmsCheckOrderScanInfo = new WmsCheckOrderScanInfo();
        BeanUtil.copyProperties(dto, wmsCheckOrderScanInfo);
        wmsCheckOrderScanInfo.setCheckBy(SecurityUtils.getUser().getUserId());
        wmsCheckOrderScanInfo.setCheckName(SecurityUtils.getUser().getName());
        wmsCheckOrderScanInfo.setCheckTime(LocalDateTime.now());
        return save(wmsCheckOrderScanInfo);
    }

    @Override
    public Boolean editScanCheckInfo(WmsCheckOrderScanInfoDTO dto) {
        Assert.isTrue(Objects.nonNull(dto), "扫描明细不可为空");
        WmsCheckOrderScanInfo wmsCheckOrderScanInfo = new WmsCheckOrderScanInfo();
        BeanUtil.copyProperties(dto, wmsCheckOrderScanInfo);
        wmsCheckOrderScanInfo.setCheckOrderId(SecurityUtils.getUser().getUserId());
        wmsCheckOrderScanInfo.setCheckName(SecurityUtils.getUser().getName());
        wmsCheckOrderScanInfo.setCheckTime(LocalDateTime.now());
        return updateById(wmsCheckOrderScanInfo);
    }

    @Override
    public Page<WmsCheckOrderScanInfoVO> listScanCheckInfo(WmsCheckOrderScanInfoListDTO dto) {
        Page<WmsCheckOrderScanInfoVO> resPage = new Page<>();
        Page<WmsCheckOrderScanInfo> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<WmsCheckOrderScanInfo>()
                .eq(WmsCheckOrderScanInfo::getCheckOrderId, dto.getCheckOrderId())
                .eq(WmsCheckOrderScanInfo::getShelf, dto.getShelf()));
        BeanUtils.copyProperties(page, resPage);
//        List<WmsCheckOrderScanInfo> list = list();
//        List<WmsCheckOrderScanInfoVO> res = BeanUtil.copyToList(list, WmsCheckOrderScanInfoVO.class);
        Set<Integer> officeIds = Sets.newHashSet();
        Set<Long> dictIds = Sets.newHashSet();
        if(CollUtil.isNotEmpty(resPage.getRecords())){
            resPage.getRecords().stream().forEach(e -> {
                officeIds.add(e.getWarehouse());
                officeIds.add(e.getShelf());
                dictIds.add(e.getUnitId());
            });
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            resPage.getRecords().stream().forEach(e -> {
                e.setShelfName(officeMap.get(e.getShelf()));
                e.setWarehouseName(officeMap.get(e.getWarehouse()));
                e.setUnitName(dictMap.get(e.getUnitId()));
            });
        }
        return resPage;
    }

    @Override
    public List<WmsCheckOrderAreaVO> areaCountList(Long checkOrderId, Set<Long> shelfIds) {
        List<WmsCheckOrderAreaVO> resList = Lists.newArrayList();
        Set<Integer> ids = shelfIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toSet());
        Map<Integer, String> shelfMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<WmsCheckOrderAreaVO> wmsCheckOrderAreaVOS = baseMapper.areaCountList(checkOrderId, shelfIds);
        Map<Integer, Integer> countMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(wmsCheckOrderAreaVOS)) {
            wmsCheckOrderAreaVOS.forEach(wmsCheckOrderAreaVO -> countMap.put(wmsCheckOrderAreaVO.getShelf(), wmsCheckOrderAreaVO.getCount()));
        }
        shelfIds.forEach(shelfId -> {
            Integer count = countMap.getOrDefault(Math.toIntExact(shelfId), 0);
            WmsCheckOrderAreaVO wmsCheckOrderAreaVO = new WmsCheckOrderAreaVO();
            wmsCheckOrderAreaVO.setShelf(Math.toIntExact(shelfId));
            wmsCheckOrderAreaVO.setShelfName(shelfMap.get(Math.toIntExact(shelfId)));
            wmsCheckOrderAreaVO.setCount(count);
            resList.add(wmsCheckOrderAreaVO);
        });

        return resList;
    }
}




