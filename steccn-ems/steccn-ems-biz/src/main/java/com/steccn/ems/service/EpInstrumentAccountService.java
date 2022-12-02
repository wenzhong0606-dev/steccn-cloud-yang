package com.steccn.ems.service;

import com.steccn.ems.domain.EpInstrumentAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpInstrumentAccountDTO;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpInstrumentAccountService extends IService<EpInstrumentAccount> {

    /**
     * 新增仪表台账
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpInstrumentAccountDTO dto);
}
