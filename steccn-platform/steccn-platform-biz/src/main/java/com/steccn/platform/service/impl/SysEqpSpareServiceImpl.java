package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.mapper.SysEqpSpareMapper;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author W10
* @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Service实现
* @createDate 2022-09-02 16:22:51
*/
@Service
@RequiredArgsConstructor
public class SysEqpSpareServiceImpl extends ServiceImpl<SysEqpSpareMapper, SysEqpSpare>
    implements SysEqpSpareService{

    private  final SysEqpSpareMapper sysEqpSpareMapper;
    @Override
    public List<EqpSpareVO> listSpares(String name) {
        return sysEqpSpareMapper.listSpares(name);
    }

    @Override
    public List<EqpSpareVO> listSpares(String name, String code) {
        //定义筛选条件
        LambdaQueryWrapper<SysEqpSpare> like = new LambdaQueryWrapper<SysEqpSpare>().eq(Objects.nonNull(code) && !code.equals(""), SysEqpSpare::getCode, code)
                .like(Objects.nonNull(name), SysEqpSpare::getName, name);
        List<SysEqpSpare> list = list(like);
        List<EqpSpareVO> eqpSpareVOS = BeanUtil.copyToList(list, EqpSpareVO.class);
        return eqpSpareVOS;
    }

    @Override
    public Map<Long, EqpSpareVO> searchSpares(String name, String code) {
        Map<Long, EqpSpareVO> resMap = Maps.newHashMap();
        List<EqpSpareVO> eqpSpareVOS = listSpares(name, code);
        if (CollUtil.isEmpty(eqpSpareVOS)) {
            return resMap;
        }

        for (EqpSpareVO eqpSpareVO : eqpSpareVOS) {
            resMap.put(eqpSpareVO.getId(), eqpSpareVO);
        }
        return resMap;
    }

    @Override
    public EqpSpareVO getByCode(String code) {
        return sysEqpSpareMapper.getByCode(code);
    }
}




