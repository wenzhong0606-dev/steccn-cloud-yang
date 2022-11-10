package com.steccn.pms.service;

import com.steccn.common.vo.BaseSelectVO;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;
import com.steccn.pms.vo.PmsWorkOrderBomVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsWorkOrderBomService extends IService<PmsWorkOrderBom> {

    /**
     * 保存物料列表
     *
     * @param materialList
     * @param id
     */
    void saveMaterial(List<PmsWorkOrderBomDTO> materialList, Long id);

    /**
     * 获取物料列表
     *
     * @param id
     * @return
     */
    List<PmsWorkOrderBomVO> getMaterialList(Long id);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 根据订单id获取物料信息
     *
     * @param craftSet
     * @return
     */
    Map<Long, List<PmsWorkOrderBomVO>> materials(Set<Long> craftSet);

    Boolean deleteBom(Set<Long> idSet, Long id);

    /**
     * 获取工序的物料
     * @param produceOrderIdSet 生产订单集合
     * @param processIdSet 工序集合
     * @return key 工序id value 物料id + 物料名称
     */
    Map<Long, List<BaseSelectVO<Long>>> getProcessMaterial(Set<Long> produceOrderIdSet, Set<Long> processIdSet);
}
