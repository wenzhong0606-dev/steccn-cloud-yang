package com.steccn.hikvision.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.hikvision.domain.SysHikPw;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author yequan
* @description 针对表【sys_hik_pw】的数据库操作Service
* @createDate 2023-07-04 15:00:54
*/
public interface SysHikPwService extends IService<SysHikPw> {

    /**
     * 权限保存
     * @param sysHikPwDTO dto
     * @return 是否保存成功
     */
    Boolean pwSave(List<SysHikPwSaveListDTO> sysHikPwDTOList);
}
