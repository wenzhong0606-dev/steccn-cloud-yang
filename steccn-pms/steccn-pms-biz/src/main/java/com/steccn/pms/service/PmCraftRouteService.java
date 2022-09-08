package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmCraftRoute;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.vo.PmCraftRouteVO;

/**
* @author yequan
* @description 针对表【pm_craft_route(生产工艺路线
)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmCraftRouteService extends IService<PmCraftRoute> {

    R<Boolean> add(PmCraftRouteDTO dto);

    Page<PmCraftRouteVO> pageList(PmCraftRouteDTO dto);

    R<Boolean> edit(PmCraftRouteDTO dto);

    R<Boolean> delete(Integer id);
}
