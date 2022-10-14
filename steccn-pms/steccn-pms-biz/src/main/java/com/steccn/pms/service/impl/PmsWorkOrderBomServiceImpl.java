package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.service.PmsWorkOrderBomService;
import com.steccn.pms.mapper.PmsWorkOrderBomMapper;
import com.steccn.pms.vo.PmsWorkOrderBomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yequan
 * @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsWorkOrderBomServiceImpl extends ServiceImpl<PmsWorkOrderBomMapper, PmsWorkOrderBom>
        implements PmsWorkOrderBomService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    /**
     * 保存物料信息
     *
     * @param materialList
     * @param id
     */
    @Override
    public void saveMaterial(List<PmsWorkOrderBomDTO> materialList, Long id) {
        //1.定义数据集合
        List<PmsWorkOrderBom> bomList = Lists.newArrayList();
        //2.处理数据
        materialList.forEach(material -> {
            PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
            BeanUtil.copyProperties(material, pmsWorkOrderBom);
            pmsWorkOrderBom.setWorkProduceId(id);
            bomList.add(pmsWorkOrderBom);
        });
        //3.批量保存
        saveBatch(bomList);
    }

    /**
     * 根据生产订单id查询物料信息
     *
     * @param id
     * @return
     */
    @Override
    public List<PmsWorkOrderBomVO> getMaterialList(Long id) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsWorkOrderBom> eq = new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, id);
        //2.获取数据
        List<PmsWorkOrderBom> list = list(eq);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        //3.处理数据
        Set<Long> ids = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        list.forEach(pmsWorkOrderBom -> {
            ids.add(pmsWorkOrderBom.getUnitId());
            materialIds.add(pmsWorkOrderBom.getModuleId());
        });
        //3.1.获取字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
        //3.2 获取物料数据
        Map<Long, EqpSpareVO> spareVOMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
        List<PmsWorkOrderBomVO> pmsWorkOrderBomVOList = Lists.newArrayList();
        list.forEach(pmsWorkOrderBom -> {
            PmsWorkOrderBomVO pmsWorkOrderBomVO = new PmsWorkOrderBomVO();
            BeanUtil.copyProperties(pmsWorkOrderBom, pmsWorkOrderBomVO);
            pmsWorkOrderBomVO.setName(spareVOMap.get(pmsWorkOrderBomVO.getModuleId()).getName());
            pmsWorkOrderBomVO.setCode(spareVOMap.get(pmsWorkOrderBomVO.getModuleId()).getCode());
            pmsWorkOrderBomVO.setUnit(dictMap.get(pmsWorkOrderBomVO.getUnitId()));
            pmsWorkOrderBomVOList.add(pmsWorkOrderBomVO);
        });
        return pmsWorkOrderBomVOList;
    }
}




