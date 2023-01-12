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
import com.steccn.pms.domain.PmsProcessOutStation;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.service.PmsProcessOutStationService;
import com.steccn.pms.mapper.PmsProcessOutStationMapper;
import com.steccn.pms.vo.PmsProcessOutStationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_process_out_station(出站信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsProcessOutStationServiceImpl extends ServiceImpl<PmsProcessOutStationMapper, PmsProcessOutStation>
        implements PmsProcessOutStationService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;

    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Page<PmsProcessOutStationVO> outPage(PmsProcessOutStationPageDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsProcessOutStation> eq = new LambdaQueryWrapper<PmsProcessOutStation>()
                .eq(PmsProcessOutStation::getProcessId, dto.getProcessId())
                .eq(PmsProcessOutStation::getBatchNo, dto.getBatchNo())
                .eq(PmsProcessOutStation::getStationId, dto.getStationId())
                .ge(Objects.nonNull(dto.getStartTime()), PmsProcessOutStation::getOutStationTime, dto.getStartTime())
                .le(Objects.nonNull(dto.getEndTime()), PmsProcessOutStation::getOutStationTime, dto.getStartTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsProcessOutStationVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsProcessOutStationVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> stationIds = Sets.newHashSet();
        res.forEach(pmsProcessOutStationVO -> {
            dictIds.add(pmsProcessOutStationVO.getUnitId());
            stationIds.add(pmsProcessOutStationVO.getStationId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //查询机构数据
        Map<Integer, String> stationMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(stationIds)).get();
        res.forEach(pmsProcessOutStationVO -> {
            pmsProcessOutStationVO.setStation(stationMap.get(pmsProcessOutStationVO.getStationId()));
            pmsProcessOutStationVO.setUnit(dictMap.get(pmsProcessOutStationVO.getUnitId()));
        });
        return page.setRecords(res);
    }
}




