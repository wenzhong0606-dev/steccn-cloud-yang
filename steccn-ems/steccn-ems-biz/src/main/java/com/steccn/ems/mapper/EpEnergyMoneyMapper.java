package com.steccn.ems.mapper;

import com.steccn.ems.domain.EpEnergyMoney;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【ep_energy_money(能耗-峰平谷)】的数据库操作Mapper
* @createDate 2022-12-06 16:20:20
* @Entity com.steccn.ems.domain.EpEnergyMoney
*/
@Mapper
public interface EpEnergyMoneyMapper extends BaseMapper<EpEnergyMoney> {

}




