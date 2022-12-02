package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import com.steccn.wms.mapper.WmsShelfUpStrategyMapper;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
public class WmsShelfUpStrategyServiceImpl extends ServiceImpl<WmsShelfUpStrategyMapper, WmsShelfUpStrategy>
    implements WmsShelfUpStrategyService{

    @Override
    public Page<WmsShelfUpStrategy> getShelfUpStrategyList(WmsShelfUpStrategyDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        List<WmsShelfUpStrategy> wmsShelfUpStrategyVOList = baseMapper.getShelfUpStrategyList(page,  dto);
        return  page.setRecords(wmsShelfUpStrategyVOList);
    }

    @Override
    public R<Boolean> addOrUpdateShelfUpStrategy(WmsShelfUpStrategy dto) {
        Integer result;
        if(dto.getId() == null ){
            dto.setCreateBy( SecurityUtils.getUser().getUserId());
            dto.setCreateName( SecurityUtils.getUser().getName());
            result = baseMapper.insert(dto);
        }else{
            WmsShelfUpStrategy wmsShelfUpStrategy = baseMapper.selectById(dto.getId());
            if(wmsShelfUpStrategy == null) return R.failure("未查询上架策略信息");
            dto.setUpdateBy( SecurityUtils.getUser().getUserId());
            dto.setUpdateName( SecurityUtils.getUser().getName());
            result = baseMapper.updateById(dto);
        }
        return result >0 ?R.success(true):R.failure(false);
    }

    @Override
    public R<Boolean> delShelfUpStrategy(WmsShelfUpStrategy dto) {
        WmsShelfUpStrategy wmsShelfUpStrategy = baseMapper.selectById(dto.getId());
        wmsShelfUpStrategy.setDelFlag(1);
        wmsShelfUpStrategy.setUpdateBy( SecurityUtils.getUser().getUserId());
        wmsShelfUpStrategy.setUpdateName( SecurityUtils.getUser().getName());
        int reulst = baseMapper.updateById(wmsShelfUpStrategy);
        return reulst>0?R.success(true):R.failure(false);
    }
}




