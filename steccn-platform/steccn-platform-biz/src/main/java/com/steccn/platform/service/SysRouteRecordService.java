package com.steccn.platform.service;

import com.steccn.platform.domain.SysRouteRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysRouteRecordDTO;
import com.steccn.platform.vo.SysRouteRecordVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【sys_route_record(路由跳转记录表)】的数据库操作Service
* @createDate 2023-04-19 11:18:38
*/
public interface SysRouteRecordService extends IService<SysRouteRecord> {
    /**
     * 保存json记录
     * @param dto
     * @return
     */
    Boolean saveRecord(SysRouteRecordDTO dto);

    /**
     * 获取记录
     * @return
     */
    List<SysRouteRecordVO> getRouteRecord();
}
