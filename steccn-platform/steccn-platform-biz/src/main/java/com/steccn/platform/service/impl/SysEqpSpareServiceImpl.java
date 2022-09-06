package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.mapper.SysEqpSpareMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<SpareDTO> listSpares(String name) {
        return sysEqpSpareMapper.listSpares(name);
    }
}




