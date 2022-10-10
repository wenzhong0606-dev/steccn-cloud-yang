package com.steccn.pms.service;

import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.CompleteStorageScanDTO;

/**
* @author yequan
* @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsCompleteStorageService extends IService<PmsCompleteStorage> {

    /**
     * 摄像头扫描箱号
     * @param caseNo 箱号
     * @return 完工入库扫描vo
     */
    CompleteStorageScanDTO scan(String caseNo);
}
