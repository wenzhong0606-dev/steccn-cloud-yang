package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsCompleteDeliveryMapper;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteDeliveryServiceImpl extends ServiceImpl<PmsCompleteDeliveryMapper, PmsCompleteDelivery>
        implements PmsCompleteDeliveryService {


    @Autowired
    private PmsProduceOrderCraftService pmsProduceOrderCraftService;
    @Autowired
    private PmsCompleteDeliveryInfoService pmsCompleteDeliveryInfoService;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;


    @Override
    public Page<PmsCompleteDeliveryVO> pageList(PmsCompleteDeliveryDTO dto) {
        //1.自定义Mapper查询投料信息
        Page<PmsCompleteDeliveryVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询详情信息
        Page<PmsCompleteDeliveryVO> page = pmsCompleteDeliveryInfoService.listByOrderId(dto);
        //2.处理数据
        List<Long> materialIds = Lists.newArrayList();
        Set<Integer> office = Sets.newHashSet();
        Set<Long> ids = Sets.newHashSet();
        List<Long> processIds = Lists.newArrayList();
        List<PmsCompleteDeliveryVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(pmsCompleteDeliveryVO -> {
                materialIds.add(pmsCompleteDeliveryVO.getMaterialId());
                ids.add(pmsCompleteDeliveryVO.getUnitId());
                if (Objects.nonNull(pmsCompleteDeliveryVO.getWarehouseId())) {
                    office.add(pmsCompleteDeliveryVO.getWarehouseId());
                }
                if (Objects.nonNull(pmsCompleteDeliveryVO.getShelfId())) {
                    office.add(pmsCompleteDeliveryVO.getShelfId());
                }
            });
            //获取单位数据
            Map<Long, String> dictData = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
            //获取物料信息
            Map<Long, EqpSpareVO> materialData = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
            //获取仓库货架信息
            Map<Integer, String> officeMapData = FeignOptional.of(remoteOfficeFeign.listByIdCh(office)).get();
            //获取工序信息
            Map<Long, PmsProduceOrderCraftVO> processMap = pmsProduceOrderCraftService.listById(processIds);
            if (CollUtil.isNotEmpty(records)) {
                records.forEach(
                        pmsCompleteDeliveryVO -> {
                            //处理单位数据
                            pmsCompleteDeliveryVO.setUnit(dictData.get(pmsCompleteDeliveryVO.getUnitId()));
                            //处理工序数据
                            if (CollUtil.isNotEmpty(processMap)) {
                                pmsCompleteDeliveryVO.setProcessName(processMap.get(pmsCompleteDeliveryVO.getProcessId()).getName());
                            }
                            //处理仓库货架信息
                            if (CollUtil.isNotEmpty(officeMapData)) {
                                pmsCompleteDeliveryVO.setWarehouseName(officeMapData.get(pmsCompleteDeliveryVO.getWarehouseId()));
                                pmsCompleteDeliveryVO.setShelfName(officeMapData.get(pmsCompleteDeliveryVO.getShelfId()));
                            }
                            //处理物料数据
                            if (CollUtil.isNotEmpty(materialData)) {
                                pmsCompleteDeliveryVO.setMaterialName(materialData.get(pmsCompleteDeliveryVO.getMaterialName()).getName());
                                pmsCompleteDeliveryVO.setMaterialCode(materialData.get(pmsCompleteDeliveryVO.getMaterialCode()).getCode());
                            }
                        }
                );
            }
        }
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> feedPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询投料信息
        Page<PmsCompleteDeliveryVO> page = pageList(dto);
        //2.处理数据
        Set<Long> unitIds = Sets.newHashSet();
        Set<Integer> officeIds = Sets.newHashSet();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                unitIds.add(pmsCompleteDeliveryVO.getUnitId());
                officeIds.add(pmsCompleteDeliveryVO.getShelfId());
                officeIds.add(pmsCompleteDeliveryVO.getWarehouseId());
            });
            //查询字典数据
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(unitIds)).get();
            //查询机构数据
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIds)).get();
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                pmsCompleteDeliveryVO.setUnit(dictMap.get(pmsCompleteDeliveryVO.getUnitId()));
                pmsCompleteDeliveryVO.setWarehouseName(officeMap.get(pmsCompleteDeliveryVO.getWarehouseId()));
            });
        }
        return page;
    }
}




