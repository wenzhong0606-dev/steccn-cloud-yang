package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysAppVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysAppVersionDTO;
import com.steccn.platform.vo.SysAppVersionVO;

/**
* @author yequan
* @description 针对表【sys_app_version(app的版本控制表)】的数据库操作Service
* @createDate 2023-01-31 14:02:33
*/
public interface SysAppVersionService extends IService<SysAppVersion> {

    /**
     * app版本分页列表
     * @param dto
     * @return
     */
    Page<SysAppVersionVO> pageList(SysAppVersionDTO dto);


    /**
     * app 版本新增
     * @param dto
     * @return
     */
    Boolean add(SysAppVersionDTO dto);

    /**
     * 获取最新版本信息
     * @return
     */
    SysAppVersionVO getLatestVersion();
}
