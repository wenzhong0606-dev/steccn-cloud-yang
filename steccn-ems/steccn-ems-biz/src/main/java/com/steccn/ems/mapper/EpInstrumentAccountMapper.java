package com.steccn.ems.mapper;

import com.steccn.ems.domain.EpInstrumentAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.ems.dto.EpDwMeterPartitionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Mapper
* @createDate 2022-11-29 19:27:28
* @Entity com.steccn.ems.domain.EpInstrumentAccount
*/
@Mapper
public interface EpInstrumentAccountMapper extends BaseMapper<EpInstrumentAccount> {

    /**
     * 获取所有的远传仪表
     * @return
     */
    List<EpDwMeterPartitionDTO> meterPartition();
}




