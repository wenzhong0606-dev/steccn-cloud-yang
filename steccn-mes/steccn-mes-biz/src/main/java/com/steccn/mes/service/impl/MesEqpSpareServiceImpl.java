package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.mes.domain.MesEqpSpare;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.mapper.MesEqpSpareMapper;
import com.steccn.mes.vo.MesSpareDownloadVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_spare(备品备件表)】的数据库操作Service实现
* @createDate 2023-08-04 13:15:46
*/
@Service
public class MesEqpSpareServiceImpl extends ServiceImpl<MesEqpSpareMapper, MesEqpSpare>
    implements MesEqpSpareService{

    @Override
    public List<MesSpareDownloadVO> downloadSpare() {
        List<MesEqpSpare> list = list(new LambdaQueryWrapper<MesEqpSpare>().eq(MesEqpSpare::getStatus, "0"));
        List<MesSpareDownloadVO> result = Lists.newArrayList();
        list.forEach(tmp->{
            MesSpareDownloadVO tmpResult = new MesSpareDownloadVO();
            BeanUtil.copyProperties(tmp,tmpResult);
            result.add(tmpResult);
        });
        return result;
    }
}




