package com.steccn.ems.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.service.EpEnergyMoneyService;
import com.steccn.ems.mapper.EpEnergyMoneyMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【ep_energy_money(能耗-峰平谷)】的数据库操作Service实现
* @createDate 2022-12-06 16:20:20
*/
@Service
public class EpEnergyMoneyServiceImpl extends ServiceImpl<EpEnergyMoneyMapper, EpEnergyMoney>
    implements EpEnergyMoneyService{

}




