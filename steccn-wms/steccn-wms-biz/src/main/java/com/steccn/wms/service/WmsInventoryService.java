package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.domain.WmsInventory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【wms_inventory(仓储库存)】的数据库操作Service
 * @createDate 2022-12-09 11:18:09
 */
public interface WmsInventoryService extends IService<WmsInventory> {


    /**
     * 库存分页列表
     *
     * @param dto
     * @return
     */
    Page<WmsInventoryVO> pageList(WmsInventoryPageDTO dto);

    /**
     * 调整占用库存信息
     *
     * @param infoList
     * @param state    0 增加占用库存  1:减少占用库存
     * @return
     */
    Boolean updateLockQuantity(List<WmsInventoryDTO> infoList, Integer state);

    /**
     * 添加库存(根据箱号，有则编辑库存，无则新增)
     *
     * @param infoList
     * @return
     */
    Boolean addInfo(List<WmsInventoryDTO> infoList);


    /**
     * 新增库存(新增)
     *
     * @param infoList
     * @return
     */
    Boolean add(List<WmsInventoryDTO> infoList);

    /**
     * 还原在途库存信息
     *
     * @param infoList
     * @return
     */
    Boolean restore(List<WmsInventoryDTO> infoList);

    /**
     * 编辑在途库存
     *
     * @param inventoryDTOList
     * @return
     */
    Boolean updateInventory(List<WmsInventoryDTO> inventoryDTOList);


    /**
     * 确认入库（减少在途库存,增加可用库存以及总库存）
     *
     * @param inventoryDTOList
     * @return
     */
    Boolean affirm(List<WmsInventoryDTO> inventoryDTOList);

    /**
     * 添加锁定库存，减少可用库存
     *
     * @param addList
     * @return
     */
    Boolean addLockQuantity(List<WmsInventoryDTO> addList);

    /**
     * 还原锁定库存数量
     *
     * @param deleteList
     * @return
     */
    Boolean restoreLockQuantity(List<WmsInventoryDTO> deleteList);

    /**
     * 还原锁定库存和可用库存并变更
     *
     * @param updateList
     * @return
     */
    Boolean editLockQuantity(List<WmsInventoryDTO> updateList);

    /**
     * 减少锁定库存 减少库存
     *
     * @param inventoryDTOList
     * @return
     */
    Boolean affirmLockQuantity(List<WmsInventoryDTO> inventoryDTOList);

    /**
     * 冻结库存
     *
     * @param dto
     * @return
     */
    Boolean freeze(WmsInventoryDTO dto);


    /**
     * 解冻库存呢
     *
     * @param dto
     * @return
     */
    Boolean unfreeze(WmsInventoryDTO dto);

    /**
     * 根据箱号获取仓库货架信息
     *
     * @param caseNos
     * @return
     */
    Map<String, CaseSiteDTO> stockByCaseNo(Set<String> caseNos);

    /**
     * 获取物料在库存中的库存数量
     *
     * @param list
     * @return
     */
    Map<Long, BigDecimal> listMaterialInventory(List<MaterialStorageDTO> list);

    /**
     * 获取物料在库存中的库存数量
     * @param materialIds
     * @return
     */
    Map<Long, MaterialInventoryDTO> materialInventory(Set<Long> materialIds);

    /**
     * 获取物料在库存中的库存数量
     * @param materialIds
     * @return
     */
    Map<Long,List<WmsInventory>> inventoryMap(Set<Long> materialIds);


    /**
     * 根据需求数量返回库存数量信息
     * @param list
     * @return
     */
    List<WmsStockInventoryDTO> materialConsume(List<MaterialStorageInfoDTO> list);


    /**
     * 校验箱号
     * @param wmsCheckCaseNoDTO
     * @return
     */
    Boolean checkCaseNo(WmsCheckCaseNoDTO wmsCheckCaseNoDTO);


    /**
     * 根据货架获取物料
     * @param set
     * @return
     */
    Map<Integer,List<WmsStockInventoryDTO>> materialByShelf(Set<Integer> set);


    /**
     * 根据仓库获取物料
     * @param set
     * @return
     */
    Map<Integer,List<WmsStockInventoryDTO>> materialByWarehouse(Set<Integer> set);


    /**
     * 扣除库存
     * @param wmsInventoryList
     * @return
     */
    Boolean deduct(List<WmsInventory> wmsInventoryList);

    /**
     * 根据仓库批量删除库存数据
     * @return
     */
    Boolean deleteData();

    /**
     * 物料汇总
     * @param dto
     * @return
     */
    Page<WmsInventoryVO> materialList(WmsInventoryPageDTO dto);


    /**
     * 仓库汇总
     * @param dto
     * @return
     */
    Page<WmsInventoryVO> warehouseList(WmsInventoryPageDTO dto);


    /**
     * 货架汇总
     * @param dto
     * @return
     */
    Page<WmsInventoryVO> shelfList(WmsInventoryPageDTO dto);

    /**
     * 根据箱号获取库存信息
     * @param caseNo
     * @return
     */
    Map<String, WmsInventory> getInventoryByCaseNo(List<String> caseNo);

    /**
     * 根据库存id获取库存信息
     * @param inventoryIds
     * @return
     */
    Map<Long, WmsInventory> inventoryIdMap(Set<Long> inventoryIds);
}
