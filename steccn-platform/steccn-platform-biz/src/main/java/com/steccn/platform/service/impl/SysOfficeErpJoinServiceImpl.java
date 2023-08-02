package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.domain.SysOfficeErpJoin;
import com.steccn.platform.service.SysOfficeErpJoinService;
import com.steccn.platform.mapper.SysOfficeErpJoinMapper;
import com.steccn.platform.vo.SysErpOfficeVO;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【sys_office_erp_join(路由跳转记录表)】的数据库操作Service实现
* @createDate 2023-07-28 16:39:07
*/
@Service
public class SysOfficeErpJoinServiceImpl extends ServiceImpl<SysOfficeErpJoinMapper, SysOfficeErpJoin>
    implements SysOfficeErpJoinService{

    @Override
    public SysErpOfficeVO getErpCodeById(Integer officeId) {
        SysOfficeErpJoin one = getOne(new LambdaQueryWrapper<SysOfficeErpJoin>()
                .eq(SysOfficeErpJoin::getOfficeId, officeId).last("LIMIT 1"));
        if(Objects.isNull(one)){
            throw new BusinessException("没有找到erp中对应的code");
        }
        SysErpOfficeVO vo = new SysErpOfficeVO();
        BeanUtil.copyProperties(one,vo);
        return vo;
    }
}




