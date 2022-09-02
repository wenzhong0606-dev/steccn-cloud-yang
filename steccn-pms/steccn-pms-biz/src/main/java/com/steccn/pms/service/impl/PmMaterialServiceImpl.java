package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.mapper.PmMaterialMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pm_material(物料清单-物料表)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class PmMaterialServiceImpl extends ServiceImpl<PmMaterialMapper, PmMaterial>
    implements PmMaterialService{

}




