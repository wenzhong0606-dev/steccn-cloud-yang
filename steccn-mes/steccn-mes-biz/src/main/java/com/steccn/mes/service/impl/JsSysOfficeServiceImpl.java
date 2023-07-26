package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.constant.MesManagerConstant;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.vo.ErpCustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【js_sys_office(组织机构表)】的数据库操作Service实现
* @createDate 2023-07-26 14:25:51
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class JsSysOfficeServiceImpl extends ServiceImpl<JsSysOfficeMapper, JsSysOffice>
    implements JsSysOfficeService{
    private final RemoteErpFeignApi remoteErpFeignApi;

    @Override
    public Boolean synchronousCustomer(ErpCustomerDTO dto) {
        List<ErpCustomerVO> erpData = remoteErpFeignApi.getErpCustomer(dto).getData();
        if(CollUtil.isNotEmpty(erpData)){
            //拿到客户节点
            JsSysOffice customerPoint = getOne(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getOfficeCode, MesManagerConstant.customerGroup)
                    .last("LIMIT 1"));
            //拿到表中已有的客户
            List<JsSysOffice> customerList = list(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, MesManagerConstant.CUSTOMER_TYPE)
                    .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0.toString()));
            for(ErpCustomerVO tmp:erpData){
                JsSysOffice officeOne = customerList.stream().filter(v -> v.getOfficeCode().equals(tmp.getFnumber())).findFirst().orElse(null);
                //名字不同的情况则需要修改
                if(Objects.nonNull(officeOne)&&!officeOne.getOfficeName().equals(tmp.getFname())){
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setFullName(tmp.getFname());
                    update(officeModel,new LambdaQueryWrapper<JsSysOffice>().eq(JsSysOffice::getOfficeCode,tmp.getFnumber()));
                }else if(Objects.isNull(officeOne)){
                    //没有值则新增
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setOfficeCode(tmp.getFnumber());
                    officeModel.setParentCode(MesManagerConstant.customerGroup);
                    officeModel.setParentCodes("0,"+MesManagerConstant.customerGroup+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);

                    save(null);
                }
            }
        }
        return true;
    }
}




