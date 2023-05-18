package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysOfficeDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.*;
import com.steccn.platform.vo.SysOfficeDeviceVO;

/**
* @author yequan
* @description 针对表【sys_office_device(工位设备配置)】的数据库操作Service
* @createDate 2023-05-18 18:02:43
*/
public interface SysOfficeDeviceService extends IService<SysOfficeDevice> {

    /**
     * 工位设备配置分页列表
     * @param dto
     * @return
     */
    Page<SysOfficeDeviceVO> pageList(SysOfficeDevicePageDTO dto);

    /**
     * 新增工位设备配置
     * @param dto
     * @return
     */
    Boolean add(SysOfficeDeviceAddDTO dto);

    /**
     * 编辑工位设备配置
     * @param dto
     * @return
     */
    Boolean edit(SysOfficeDeviceUpdateDTO dto);
}
