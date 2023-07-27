package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.qms.domain.QtCustomerComplainDetail;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import com.steccn.qms.service.QtCustomerComplainDetailService;
import com.steccn.qms.mapper.QtCustomerComplainDetailMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【qms_customer_complain_detail(客诉记录明细表)】的数据库操作Service实现
* @createDate 2023-03-15 10:16:54
*/
@Service
public class QtCustomerComplainDetailServiceImpl extends ServiceImpl<QtCustomerComplainDetailMapper, QtCustomerComplainDetail>
    implements QtCustomerComplainDetailService {

    @Override
    public Boolean edit(QtCustomerComplainDTO qtCustomerComplainDTO) {


        if (Objects.nonNull(qtCustomerComplainDTO.getDetails())) {
            List<QtCustomerComplainDetail> list = Lists.newArrayList();
            Set<Long> idSet = Sets.newHashSet();
            //处理数据
            qtCustomerComplainDTO.getDetails().forEach(detail->{
                QtCustomerComplainDetail customerComplainDetail = new QtCustomerComplainDetail();
                BeanUtil.copyProperties(detail,customerComplainDetail);
                customerComplainDetail.setCustomerComplainId(qtCustomerComplainDTO.getId());
                if (Objects.nonNull(customerComplainDetail.getCustomerComplainId())){
                    list.add(customerComplainDetail);
                    idSet.add(customerComplainDetail.getId());
                } else {
                    list.add(customerComplainDetail);
                }
            });
            //删除多余的数据
            if (CollUtil.isNotEmpty(idSet)) {
                remove(new LambdaQueryWrapper<QtCustomerComplainDetail>()
                        .eq(QtCustomerComplainDetail::getCustomerComplainId, qtCustomerComplainDTO.getId())
                        .notIn(QtCustomerComplainDetail::getId,idSet));
            }
            //批量修改新增
            return saveOrUpdateBatch(list);
        }else {
            return remove(new LambdaQueryWrapper<QtCustomerComplainDetail>().in(QtCustomerComplainDetail::getCustomerComplainId, qtCustomerComplainDTO.getId()));
        }

    }
}




