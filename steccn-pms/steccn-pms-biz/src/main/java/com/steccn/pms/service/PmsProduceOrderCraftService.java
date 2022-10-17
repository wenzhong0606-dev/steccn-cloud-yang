package com.steccn.pms.service;

import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProduceOrderCraftDTO;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;

import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【pms_produce_order_craft(生产订单-工艺信息)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProduceOrderCraftService extends IService<PmsProduceOrderCraft> {

    /**
     * 根据生产订单id保存工序信息
     * @param processList
     * @param id
     */
    void saveCrafts(List<PmsProduceOrderCraftDTO> processList,Long id);

    /**
     * 根据生产订单id获取工序信息
     * @param id
     * @return
     */
    List<PmsProduceOrderCraftVO> getCrafts(Long id);

    /**
     * 批量查询工序信息
     * @param processIds
     * @return
     */
    Map<Long,PmsProduceOrderCraftVO> listById(List<Long> processIds);

    /**
     * 删除工艺信息
     * @param id
     * @return
     */
    Boolean delete(Integer id);
}
