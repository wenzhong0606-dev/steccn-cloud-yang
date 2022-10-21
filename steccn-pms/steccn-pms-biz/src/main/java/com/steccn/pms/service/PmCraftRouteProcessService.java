package com.steccn.pms.service;

import com.steccn.pms.domain.PmCraftRouteModule;
import com.steccn.pms.domain.PmCraftRouteProcess;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.vo.PmCraftRouteProcessVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_craft_route_process(生产工艺路线-工序信息)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmCraftRouteProcessService extends IService<PmCraftRouteProcess> {

    /**
     * 保存工序
     * @param dto
     * @return
     */
    Boolean saveProcess(PmCraftRouteDTO dto);

    /**
     * 根据工艺路线id获取工序信息
     * @param craftRouteId
     * @return
     */
    List<PmCraftRouteProcessVO> list(Long craftRouteId);

    /**
     * 根据路线id删除工序信息
     * @param id
     * @return
     */
    Boolean deleteByRoteId(Long id);
}
