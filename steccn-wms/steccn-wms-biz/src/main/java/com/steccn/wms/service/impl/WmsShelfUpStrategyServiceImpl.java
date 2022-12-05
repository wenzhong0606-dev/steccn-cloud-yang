package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import com.steccn.wms.mapper.WmsShelfUpStrategyMapper;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
@RequiredArgsConstructor
public class WmsShelfUpStrategyServiceImpl extends ServiceImpl<WmsShelfUpStrategyMapper, WmsShelfUpStrategy>
    implements WmsShelfUpStrategyService{

    private final RemoteSpareFeignApi remoteSpareFeignApi;
    /***
     * 获取上架策略列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsShelfUpStrategy> getShelfUpStrategyList(WmsShelfUpStrategyDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        List<WmsShelfUpStrategy> wmsShelfUpStrategyVOList = baseMapper.getShelfUpStrategyList(page,  dto);
        for (WmsShelfUpStrategy up:wmsShelfUpStrategyVOList ) {
            List<Long> materialIds = new ArrayList<>();
            materialIds.add(up.getMaterialId());
            R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
            if(materialInfo.getData().size()>0) up.setMaterialUnitName(materialInfo.getData().get(up.getMaterialId()).getUnit());
        }
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




