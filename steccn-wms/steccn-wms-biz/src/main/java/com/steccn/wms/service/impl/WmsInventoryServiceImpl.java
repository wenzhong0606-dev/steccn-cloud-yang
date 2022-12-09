package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.vo.PmBillOfMaterialVO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.mapper.WmsInventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author W10
 * @description 针对表【wms_inventory(仓储库存)】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory>
        implements WmsInventoryService {

    @Override
    public Page<WmsInventoryVO> pageList(WmsInventoryPageDTO dto) {
        Page<WmsInventoryVO> page = new Page<>();
        LambdaQueryWrapper<WmsInventory> like = new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getWarehouseId, dto.getWarehouseId())
                .like(WmsInventory::getMaterialCode, dto.getMaterialCode())
                .like(WmsInventory::getMaterialName, dto.getMaterialName())
                .like(WmsInventory::getCaseNo, dto.getCaseNo())
                .like(WmsInventory::getBatch, dto.getBatch());
        Page<WmsInventory> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), like);
        BeanUtil.copyProperties(resPage, page);
        List<WmsInventoryVO> res = BeanUtil.copyToList(resPage.getRecords(), WmsInventoryVO.class);
        page.setRecords(res);
        return page;
    }

    @Override
    public Boolean updateLockQuantity(List<WmsInventoryDTO> infoList, Integer state) {
        Assert.isTrue(CollUtil.isNotEmpty(infoList), "数据异常");
        //自定义sql 修改锁定库存信息
        Boolean aBoolean = null;
        if (WmsManagerConstant.InventoryEnum.ZERO.getCode().equals(state)) {
             aBoolean = this.baseMapper.augmentLockQuantity(infoList);
        } else if (WmsManagerConstant.InventoryEnum.ONE.getCode().equals(state)) {
            aBoolean = this.baseMapper.subtractLockQuantity(infoList);
        } else if (WmsManagerConstant.InventoryEnum.TWO.getCode().equals(state)){
            aBoolean = this.baseMapper.subtractFirstAndThenAddLockQuantity(infoList);
        }else if (WmsManagerConstant.InventoryEnum.THREE.getCode().equals(state)){
            aBoolean = this.baseMapper.updateLockAndTotalQuantity(infoList);
        }
        return aBoolean;
    }
}




