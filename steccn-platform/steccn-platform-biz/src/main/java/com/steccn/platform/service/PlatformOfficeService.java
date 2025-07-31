package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformOffice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.vo.PlatformOfficeVO;

import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【platform_office(菜单表)】的数据库操作Service
* @createDate 2025-07-30 16:21:41
*/
public interface PlatformOfficeService extends IService<PlatformOffice> {
    /**
     * 新增机构
     * @param dto
     * @return
     */
    Boolean add(PlatformOfficeDTO dto);

    /**
     * 修改机构
     * @param dto
     * @return
     */
    Boolean update(PlatformOfficeDTO dto);

    /**
     * 删除
     * @return
     */
    Boolean deleteById(Set<Integer> ids);

    /**
     * 根据id获取子树
     * @return
     */
    PlatformOfficeVO getTreeById(Integer id);

    /**
     * 获取所有机构
     * @return
     */
    List<PlatformOfficeVO> getTreeList();
}
