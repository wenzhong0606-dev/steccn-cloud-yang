package com.steccn.pms.service;

import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsCompleteStorageInfoService extends IService<PmsCompleteStorageInfo> {

    /**
     * 入库单已存在箱号
     * @param caseNoSet 箱号set
     * @return 是否
     */
    Boolean existCaseNo(Set<String> caseNoSet);
}
