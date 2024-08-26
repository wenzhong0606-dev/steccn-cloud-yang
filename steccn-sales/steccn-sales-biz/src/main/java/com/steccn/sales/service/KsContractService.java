package com.steccn.sales.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.sales.domain.KsContract;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Admin
* @description 针对表【ks_contract(合同)】的数据库操作Service
* @createDate 2024-08-26 14:17:37
*/
public interface KsContractService extends IService<KsContract> {
    QtLineChartBarDTO contractSum(DpPmsOperateDTO dto);
}
