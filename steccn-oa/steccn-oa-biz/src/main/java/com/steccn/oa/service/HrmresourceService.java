package com.steccn.oa.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.oa.domain.Hrmresource;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaUserIdAndName;

import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【hrmresource】的数据库操作Service
* @createDate 2022-09-01 18:37:02
*/
public interface HrmresourceService extends IService<Hrmresource> {
    /**
     * 通过工号查询id
     * @param workCode
     * @return
     */
    Long getIdByWorkCode(String workCode);

    /**
     * 根据名称模糊匹配人员信息
     * @param userName
     * @return
     */
    List<OaUserIdAndName> getUserByName(String userName);

    /**
     * 根据id获取用户的中文名称
     * @param idList
     * @return
     */
    Map<Long, String> listByIdCh(List<Long> idList);

    /**
     * 根据用户名列表查询id
     * @param userNames
     * @return
     */
    List<Integer> getIdByUserName(List<String> userNames);
}
