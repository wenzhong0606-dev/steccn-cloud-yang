package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.wms.domain.WmsShelfDownStrategy;
import com.steccn.wms.dto.WmsShelfDownStrategyDTO;
import com.steccn.wms.mapper.WmsShelfDownStrategyMapper;
import com.steccn.wms.service.WmsShelfDownStrategyService;
import com.steccn.wms.vo.WmsShelfDownStrategyVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_shelf_down_strategy(下架策略)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
@RequiredArgsConstructor
public class WmsShelfDownStrategyServiceImpl extends ServiceImpl<WmsShelfDownStrategyMapper, WmsShelfDownStrategy>
    implements WmsShelfDownStrategyService{

    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public Page<WmsShelfDownStrategyVO> getShelfDownStrategyList(WmsShelfDownStrategyDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<WmsShelfDownStrategy> lambdaQueryWrapper = new LambdaQueryWrapper<WmsShelfDownStrategy>();
        lambdaQueryWrapper.eq(WmsShelfDownStrategy::getDelFlag,0);
        if (StringUtils.isNotEmpty(dto.getKeyWord())) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(WmsShelfDownStrategy::getMaterialCode, dto.getKeyWord())
                    .or().like(WmsShelfDownStrategy::getMaterialName, dto.getKeyWord()));
        }
        lambdaQueryWrapper.orderByDesc(WmsShelfDownStrategy::getCreateTime);
        Page resPage = page(page, lambdaQueryWrapper);

        List<WmsShelfDownStrategyVO> wmsShelfDownStrategyList = new ArrayList<>();
        resPage.getRecords().forEach(vo->{
            WmsShelfDownStrategyVO wmsShelfDownStrategyVO = new WmsShelfDownStrategyVO();
            BeanUtils.copyProperties(vo,wmsShelfDownStrategyVO);
            //下架类型 和 货架排序获取
            List<Long> soldOutTypeIds = new ArrayList<>();
            soldOutTypeIds.add(wmsShelfDownStrategyVO.getSoldOutType());
            soldOutTypeIds.add(wmsShelfDownStrategyVO.getShelfOrder());
            R<Map<Long, SysDictVO>> sysDictVO = remoteDictFeignApi.listById(soldOutTypeIds);
            wmsShelfDownStrategyVO.setSoldOutTypeName(sysDictVO.getData().get(wmsShelfDownStrategyVO.getSoldOutType()).getName());
            wmsShelfDownStrategyVO.setShelfOrderName(sysDictVO.getData().get(wmsShelfDownStrategyVO.getShelfOrder()).getName());
            wmsShelfDownStrategyList.add(wmsShelfDownStrategyVO);
        });
        resPage.setRecords(wmsShelfDownStrategyList);
        return resPage;
    }

    @Override
    public R<Boolean> addOrUpdateShelfDownStrategy(WmsShelfDownStrategy dto) {
        Integer result;
        if(dto.getId() == null ){
            dto.setCreateBy( SecurityUtils.getUser().getUserId());
            dto.setCreateName( SecurityUtils.getUser().getName());
            result = baseMapper.insert(dto);
        }else{
            WmsShelfDownStrategy wmsShelfDownStrategy = baseMapper.selectById(dto.getId());
            if(wmsShelfDownStrategy == null) return R.failure("未查询到下架策略信息");
            dto.setUpdateBy( SecurityUtils.getUser().getUserId());
            dto.setUpdateName( SecurityUtils.getUser().getName());
            result = baseMapper.updateById(dto);
        }
        return result >0 ?R.success(true):R.failure(true);
    }

    @Override
    public R<Boolean> delShelfDownStrategy(WmsShelfDownStrategy dto) {
        WmsShelfDownStrategy wmsShelfDownStrategy = baseMapper.selectById(dto.getId());
        wmsShelfDownStrategy.setDelFlag(1);
        wmsShelfDownStrategy.setUpdateBy( SecurityUtils.getUser().getUserId());
        wmsShelfDownStrategy.setUpdateName( SecurityUtils.getUser().getName());
        int reulst = baseMapper.updateById(wmsShelfDownStrategy);
        return reulst>0?R.success(true):R.failure(false);
    }
}




