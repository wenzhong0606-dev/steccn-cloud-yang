package com.steccn.hikvision.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.hikvision.domain.SysHikPw;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveDTO;
import com.steccn.hikvision.dto.SysHikPwSaveListDTO;
import com.steccn.hikvision.dto.SysHikPwTreeDTO;
import com.steccn.hikvision.vo.CameraVO;
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
     *
     * @param list dto
     * @return 是否保存成功
     */
    Boolean pwSave(List<SysHikPwSaveDTO> list);

    /**
     * 根据角色查询详细的权限配置-平铺结构
     *
     * @param roleCode 角色code
     * @return list
     */
    List<SysHikPwDTO> info(String roleCode);

    /**
     * 获取用户的树
     *
     * @param username 用户名称
     * @return 树结构
     */
    List<CameraVO> tree(String username);
}
