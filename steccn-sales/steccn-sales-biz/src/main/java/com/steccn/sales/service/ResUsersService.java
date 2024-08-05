package com.steccn.sales.service;

import com.steccn.portal.vo.SteccnTokenVO;
import com.steccn.sales.domain.ResUsers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.sales.vo.ResUserVO;

/**
* @author Admin
* @description 针对表【res_users】的数据库操作Service
* @createDate 2024-08-05 09:47:57
*/
public interface ResUsersService extends IService<ResUsers> {
    SteccnTokenVO login(String username, String password, String clientNo, Integer loginStatus);

    ResUserVO userInfo(String userName);
}
