package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import com.steccn.wms.mapper.WmsShelfUpStrategyMapper;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
public class WmsShelfUpStrategyServiceImpl extends ServiceImpl<WmsShelfUpStrategyMapper, WmsShelfUpStrategy>
    implements WmsShelfUpStrategyService{

    /***
     * 获取上架策略列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsShelfUpStrategy> getShelfUpStrategyList(WmsShelfUpStrategyDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        List<WmsShelfUpStrategy> wmsShelfUpStrategyVOList = baseMapper.getShelfUpStrategyList(page,  dto);
        return  page.setRecords(wmsShelfUpStrategyVOList);
    }

    /**
     * 新增或者编辑上架策略
     * @param dto
     * @return
     */
    @Override
    public Boolean addOrUpdateShelfUpStrategy(WmsShelfUpStrategy dto) {
        Integer result;
        if(Objects.isNull(dto.getId())){
            result = baseMapper.insert(dto);
        }else{
            WmsShelfUpStrategy wmsShelfUpStrategy = baseMapper.selectById(dto.getId());
            if(wmsShelfUpStrategy == null) return false;
            result = baseMapper.updateById(dto);
        }
        return result >0 ? true:false;
    }

    /**
     * 删除上架策略
     * @param dto
     * @return
     */
    @Override
    public Boolean delShelfUpStrategy(WmsShelfUpStrategy dto) {
        return  removeById(dto);
    }
}




