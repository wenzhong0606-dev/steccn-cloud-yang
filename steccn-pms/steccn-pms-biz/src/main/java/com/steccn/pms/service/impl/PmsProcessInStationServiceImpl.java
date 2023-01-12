package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.pms.domain.PmsProcessInStation;
import com.steccn.pms.dto.PmsProcessInStationDTO;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.mapper.PmsProcessInStationMapper;
import com.steccn.pms.vo.PmsProcessInStationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_process_in_station(进站信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsProcessInStationServiceImpl extends ServiceImpl<PmsProcessInStationMapper, PmsProcessInStation>
        implements PmsProcessInStationService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;

    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Page<PmsProcessInStationVO> inPage(PmsProcessInStationDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsProcessInStation> eq = new LambdaQueryWrapper<PmsProcessInStation>()
                .eq(Objects.nonNull(dto.getProcessId()),PmsProcessInStation::getProcessId, dto.getProcessId())
                .eq(Objects.nonNull(dto.getBatchNo()),PmsProcessInStation::getBatchNo, dto.getBatchNo())
                .eq(Objects.nonNull(dto.getStationId()),PmsProcessInStation::getStationId, dto.getStationId())
                .ge(Objects.nonNull(dto.getStartTime()), PmsProcessInStation::getInStationTime, dto.getStartTime())
                .le(Objects.nonNull(dto.getEndTime()), PmsProcessInStation::getInStationTime, dto.getStartTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsProcessInStationVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsProcessInStationVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> stationIds = Sets.newHashSet();
        res.forEach(pmsProcessInStationVO -> {
            dictIds.add(pmsProcessInStationVO.getUnitId());
            stationIds.add(pmsProcessInStationVO.getStationId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //查询机构数据
        Map<Integer, String> stationMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(stationIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        res.forEach(pmsProcessInStationVO -> {
            pmsProcessInStationVO.setStation(stationMap.get(pmsProcessInStationVO.getStationId()));
            pmsProcessInStationVO.setUnit(dictMap.get(pmsProcessInStationVO.getUnitId()));
        });
        return page.setRecords(res);
    }
}




