package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.WmsStockVO;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.MaterialStorageDTO;
import com.steccn.wms.dto.WmsInventoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【wms_inventory(仓储库存)】的数据库操作Mapper
 * @createDate 2022-12-09 11:18:09
 * @Entity com.steccn.wms.domain.WmsInventory
 */
@Mapper
public interface WmsInventoryMapper extends BaseMapper<WmsInventory> {

    /**
     * 增加锁定库存，减少可用库存
     *
     * @param infoList
     * @return
     */
    Boolean augmentLockQuantity(@Param("list") List<WmsInventoryDTO> infoList);


    /**
     * 减少锁定库存，增加可用库存
     *
     * @param infoList
     * @return
     */
    Boolean subtractLockQuantity(@Param("list") List<WmsInventoryDTO> infoList);

    /**
     * 先减少锁定库存，增加可用库存，再增加锁定库存，减少可用库存
     *
     * @param infoList
     * @return
     */
    Boolean subtractFirstAndThenAddLockQuantity(@Param("list") List<WmsInventoryDTO> infoList);

    /**
     * 减少库存中可用库存数量，减少占用库存数量,减少总库存
     *
     * @param infoList
     * @return
     */
    Boolean updateLockAndTotalQuantity(@Param("list") List<WmsInventoryDTO> infoList);

    /**
     * 获取物料在库存中的数量
     * @param list
     * @return
     */
    List<WmsInventory> listMaterialInventory(@Param("list")List<MaterialStorageDTO> list);


    List<WmsStockVO> getByShelfCodeOrMaterialsInventoryList(@Param("shelfCodes")Set<String> shelfs,
                                                            @Param("materialsList") Set<Long> materials);

}




