package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.SysScreenConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysScreenConfigDTO;
import com.steccn.platform.vo.SysScreenConfigVO;
import com.steccn.qms.vo.QtCheckPlanVO;

/**
* @author Admin
* @description 针对表【sys_screen_config】的数据库操作Service
* @createDate 2024-04-22 15:28:35
*/
public interface SysScreenConfigService extends IService<SysScreenConfig> {
    /**
     * 新增
     * @param dto
     * @return
     */
    Boolean add(SysScreenConfigDTO dto);

    /**
     * 修改
     * @param dto
     * @return
     */
    Boolean updateData(SysScreenConfigDTO dto);

    /**
     * 删除数据
     * @param id
     * @return
     */
    Boolean deleteData(Integer id);

    /**
     * 分页
     * @param dto
     * @return
     */
    Page<SysScreenConfigVO> pageList(BasePageDTO dto);

    /**
     * 详情
     * @param id
     * @return
     */
    SysScreenConfigVO info(Integer id);
}
