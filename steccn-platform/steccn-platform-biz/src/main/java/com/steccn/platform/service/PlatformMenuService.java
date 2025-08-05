package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.PlatformMenuDTO;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.vo.PlatformMenuVO;
import com.steccn.platform.vo.PlatformOfficeVO;

import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【platform_menu(菜单表)】的数据库操作Service
* @createDate 2025-08-05 13:32:56
*/
public interface PlatformMenuService extends IService<PlatformMenu> {
    /**
     * 新增菜单
     * @param dto
     * @return
     */
    Boolean add(PlatformMenuDTO dto);

    /**
     * 修改菜单
     * @param dto
     * @return
     */
    Boolean update(PlatformMenuDTO dto);

    /**
     * 删除
     * @return
     */
    Boolean deleteById(Set<Integer> ids);

    /**
     * 获取子树
     * @param id
     * @return
     */
    PlatformMenuVO getTreeById(Integer id);

    /**
     * 获取所有节点
     * @return
     */
    List<PlatformMenuVO> getTreeList();

    /**
     * 获取用户相关节点
     * @return
     */
    List<PlatformMenuVO> getUserTreeList();
}
