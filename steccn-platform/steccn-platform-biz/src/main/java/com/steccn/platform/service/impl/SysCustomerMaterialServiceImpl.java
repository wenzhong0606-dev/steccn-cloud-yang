package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysCustomerMaterial;
import com.steccn.platform.dto.SysCustomerMaterialDTO;
import com.steccn.platform.mapper.SysCustomerMaterialMapper;
import com.steccn.platform.service.SysCustomerMaterialService;
import com.steccn.platform.vo.SysCustomerMaterialPageVO;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @description 针对表【sys_customer_material(客户物料表)】的数据库操作Service实现
 * @createDate 2023-03-03 10:04:02
 */
@Service
public class SysCustomerMaterialServiceImpl extends ServiceImpl<SysCustomerMaterialMapper, SysCustomerMaterial>
        implements SysCustomerMaterialService {

    @Override
    public Page<SysCustomerMaterialPageVO> pageList(SysCustomerMaterialDTO dto) {
        return this.baseMapper.pageList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
    }
}




