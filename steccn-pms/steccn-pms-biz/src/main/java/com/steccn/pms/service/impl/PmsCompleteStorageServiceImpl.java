package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Service实现
* @createDate 2022-09-28 15:18:51
*/
@Service
public class PmsCompleteStorageServiceImpl extends ServiceImpl<PmsCompleteStorageMapper, PmsCompleteStorage>
    implements PmsCompleteStorageService{

    private PmsCompleteStorageInfoService completeStorageInfoService;

    @Override
    public CompleteStorageScanDTO scan(String caseNo) {
        //1.判断装箱单（PP-XXXXX）已扫描，入库单为SI-XXXXX

        //2.


        return null;
    }
}




