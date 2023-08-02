package com.steccn.platform.service;

import com.steccn.platform.domain.SysOfficeErpJoin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.vo.SysErpOfficeVO;

/**
* @author Admin
* @description 针对表【sys_office_erp_join(路由跳转记录表)】的数据库操作Service
* @createDate 2023-07-28 16:39:07
*/
public interface SysOfficeErpJoinService extends IService<SysOfficeErpJoin> {
    /**
     * 根据系统中的机构id查询erp中的code
     * @param officeId
     * @return
     */
    SysErpOfficeVO getErpCodeById(Integer officeId);
}
