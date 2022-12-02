package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.dto.EpInstrumentAccountDTO;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
public class EpInstrumentAccountServiceImpl extends ServiceImpl<EpInstrumentAccountMapper, EpInstrumentAccount>
    implements EpInstrumentAccountService{

    @Override
    public Boolean addOrEdit(EpInstrumentAccountDTO dto) {
        long size = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getDistantMapCode, dto.getDistantMapCode())
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
        getRepeatJudgment(dto.getId(), size,"远传表映射编码不能重复");
        long sizeMeterName = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterName, dto.getMeterName())
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
       getRepeatJudgment(dto.getId(),size,"仪表名称不能重复");
        if (Objects.nonNull(dto.getSite())){
            //查询电表下的所有安装位置有无重复
            long sizeSite = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getMeterType,dto.getMeterType())
                    .eq(EpInstrumentAccount::getSite, dto.getSite())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
        getRepeatJudgment(dto.getId(),sizeSite,"安装位置不能重复");
        }
        if (Objects.nonNull(dto.getAssetsCode())){
            //查询数据库中的资产编码
            long sizeAssetsCode = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getAssetsCode, dto.getAssetsCode())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
            getRepeatJudgment(dto.getId(),sizeAssetsCode,"资产编码不能重复");
        }
        EpInstrumentAccount epInstrumentAccount = new EpInstrumentAccount();
        BeanUtils.copyProperties(dto,epInstrumentAccount);
        if (Objects.isNull(dto.getId())){
            save(epInstrumentAccount);
        }else {
            updateById(epInstrumentAccount);
        }
        return Boolean.TRUE;
    }

    /**
     * 根据条件判断是否抛出异常
     * @param id
     * @param size
     * @param mistake
     */
    private void getRepeatJudgment(Integer id, long size,String mistake) {
        if (Objects.isNull(id)){
            Assert.isTrue(size ==0 , mistake);
        }else {
            //编辑时允许与自身重复
            Assert.isTrue(size <=1 , mistake);
        }
    }
}




