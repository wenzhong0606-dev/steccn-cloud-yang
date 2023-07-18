package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.qms.domain.QtCustomerComplainDetail;
import com.steccn.qms.domain.QtCustomerComplainModule;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.service.QtCustomerComplainModuleService;
import com.steccn.qms.mapper.QtCustomerComplainModuleMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
* @author W10
* @description 针对表【qt_customer_complain_module(客诉记录组件明细表
)】的数据库操作Service实现
* @createDate 2023-06-29 10:17:31
*/
@Service
public class QtCustomerComplainModuleServiceImpl extends ServiceImpl<QtCustomerComplainModuleMapper, QtCustomerComplainModule>
    implements QtCustomerComplainModuleService{

    @Override
    public Boolean edit(QtCustomerComplainDTO qtCustomerComplainDTO) {
        List<QtCustomerComplainModule> list = Lists.newArrayList();
        Set<Long> idSet = Sets.newHashSet();
        //处理数据
        qtCustomerComplainDTO.getModules().forEach(module->{
            QtCustomerComplainModule customerComplainModule = new QtCustomerComplainModule();
            BeanUtil.copyProperties(module,customerComplainModule);
            if(Objects.nonNull(customerComplainModule.getCustomerComplainId())){
                idSet.add(customerComplainModule.getId());
            }
            customerComplainModule.setCustomerComplainId(qtCustomerComplainDTO.getId());
            list.add(customerComplainModule);
        });
        //删除多余的数据
        if (CollUtil.isNotEmpty(idSet)) {
            remove(new LambdaQueryWrapper<QtCustomerComplainModule>()
                    .eq(QtCustomerComplainModule::getCustomerComplainId, qtCustomerComplainDTO.getId())
                    .notIn(QtCustomerComplainModule::getId,idSet));
        }
        //批量修改新增
        return saveOrUpdateBatch(list);
    }
}




