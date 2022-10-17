package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmCraftRoute;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.vo.PmCraftRouteProcessVO;
import com.steccn.pms.vo.PmCraftRouteVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pm_craft_route(生产工艺路线
)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmCraftRouteService extends IService<PmCraftRoute> {

    /**
     * 新增
     * @param dto
     * @return
     */
    R<Boolean> add(PmCraftRouteDTO dto);

    /**
     * 分页列表
     * @param dto
     * @return
     */
    Page<PmCraftRouteVO> pageList(PmCraftRouteDTO dto);

    /**
     * 编辑
     * @param dto
     * @return
     */
    R<Boolean> edit(PmCraftRouteDTO dto);


    /**
     * 删除
     * @param id
     * @return
     */
    R<Boolean> delete(Long id);

    /**
     * 校验当前物料下的版本
     * @param id
     * @param version
     * @return
     */
    R<Boolean> verifyVersion(Long id, String version);

    /**
     * 获取工艺路线下的工艺信息
     * @param processId
     * @param craftRouteId
     * @return
     */
    R<PmCraftRouteProcessVO> getProcessInfo(Long processId, String craftRouteId);

    /**
     * 根据工艺id获取版本信息
     * @param craftIds
     * @return
     */
    Map<Long,String> versionById(Set<Long> craftIds);
}
