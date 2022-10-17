package com.steccn.pms.service;

import com.steccn.pms.domain.PmsWorkOrderBom;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.vo.PmsWorkOrderBomVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsWorkOrderBomService extends IService<PmsWorkOrderBom> {

    /**
     * 保存物料列表
     * @param materialList
     * @param id
     */
    void saveMaterial(List<PmsWorkOrderBomDTO> materialList, Long id);

    /**
     * 获取物料列表
     * @param id
     * @return
     */
    List<PmsWorkOrderBomVO> getMaterialList(Long id);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Integer id);
}
