package com.steccn.qms.service;

import com.steccn.qms.domain.QtReportWarnUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.vo.QtReportWarnUserVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【qt_report_warn_user(spc报表卡片报警人)】的数据库操作Service
* @createDate 2023-02-16 15:50:52
*/
public interface QtReportWarnUserService extends IService<QtReportWarnUser> {

    /**
     * 保存spc报表卡片告警人员信息
     * @param warnUserIds
     * @param id
     */
    void saveWarnUser(List<Long> warnUserIds, Integer id);

    /**
     * 修改告警人员信息
     * @param warnUserIds
     * @param id
     */
    void updateWarnUser(List<Long> warnUserIds, Integer id);

    /**
     * 获取告警人员信息
     * @param id
     * @return
     */
    List<QtReportWarnUserVO> getWarnUser(Integer id);
}
