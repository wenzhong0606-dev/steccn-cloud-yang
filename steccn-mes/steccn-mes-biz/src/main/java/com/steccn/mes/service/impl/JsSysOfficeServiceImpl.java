package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.constant.MesManagerConstant;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.vo.ErpCustomerVO;
import com.steccn.mes.vo.MesCutomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setFullName(tmp.getFname());
                    update(officeModel,new LambdaQueryWrapper<JsSysOffice>().eq(JsSysOffice::getOfficeCode,tmp.getFnumber()));
                }else if(Objects.isNull(officeOne)){
                    //没有值则新增
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setOfficeCode(tmp.getFnumber());
                    officeModel.setParentCode(MesManagerConstant.customerGroup);
                    officeModel.setParentCodes("0,"+MesManagerConstant.customerGroup+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);
                    officeModel.setTreeSorts(customerPoint.getTreeSorts()+"0000000030,");
                    officeModel.setTreeLeaf(MesManagerConstant.NUM_1.toString());
                    officeModel.setTreeLevel(MesManagerConstant.NUM_1);
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setViewCode(tmp.getFnumber());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setFullName(tmp.getFname());
                    officeModel.setOfficeType(MesManagerConstant.CUSTOMER_TYPE);
                    officeModel.setStatus(MesManagerConstant.NUM_0.toString());
                    officeModel.setCreateBy(MesManagerConstant.ERP);
                    officeModel.setCreateDate(LocalDateTime.now());
                    officeModel.setUpdateBy(MesManagerConstant.ERP);
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setCorpCode(MesManagerConstant.NUM_0.toString());
                    save(officeModel);
                }
            }
        }
        return true;
    }

    @Override
    public List<MesCutomerVO> getAllCustomer(ErpCustomerDTO dto) {
        if(Objects.isNull(dto.getStartfmodifydate())||Objects.isNull(dto.getStopfmodifydate())){
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        List<MesCutomerVO> results = Lists.newArrayList();
        List<JsSysOffice> jsSysOffices = list(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName,JsSysOffice::getUpdateDate)
                .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0)
                .eq(JsSysOffice::getOfficeType, MesManagerConstant.CUSTOMER_TYPE)
                .between(JsSysOffice::getUpdateDate, dto.getStartfmodifydate(), dto.getStopfmodifydate()));
        if(CollUtil.isNotEmpty(jsSysOffices)){
            for(JsSysOffice tmp:jsSysOffices){
                MesCutomerVO result = new MesCutomerVO();
                result.setCustomerCode(tmp.getOfficeCode());
                result.setName(tmp.getOfficeName());
                result.setLastModifiedDateTime(tmp.getUpdateDate());
                results.add(result);
            }
        }
        return results;
    }

    @Override
    public Boolean synchronousSupplier(ErpCustomerDTO dto) {
        List<ErpCustomerVO> erpData = remoteErpFeignApi.getErpSupplier(dto).getData();
        if(CollUtil.isNotEmpty(erpData)){
            //拿到供应商节点
            JsSysOffice customerPoint = getOne(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getOfficeCode, MesManagerConstant.supplierGroup)
                    .last("LIMIT 1"));
            //拿到表中已有的供应商
            List<JsSysOffice> supplierList = list(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, MesManagerConstant.SUPPLIER_TYPE)
                    .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0.toString()));
            for(ErpCustomerVO tmp:erpData){
                JsSysOffice officeOne = supplierList.stream().filter(v -> v.getOfficeCode().equals(tmp.getFnumber())).findFirst().orElse(null);
                //名字不同的情况则需要修改
                if(Objects.nonNull(officeOne)&&!officeOne.getOfficeName().equals(tmp.getFname())){
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setFullName(tmp.getFname());
                    officeModel.setUpdateBy(MesManagerConstant.ERP);
                    update(officeModel,new LambdaQueryWrapper<JsSysOffice>().eq(JsSysOffice::getOfficeCode,tmp.getFnumber()));
                }else if(Objects.isNull(officeOne)){
                    //没有值则新增
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setOfficeCode(tmp.getFnumber());
                    officeModel.setParentCode(MesManagerConstant.supplierGroup);
                    officeModel.setParentCodes("0,"+MesManagerConstant.supplierGroup+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);
                    officeModel.setTreeSorts(customerPoint.getTreeSorts()+"0000000030,");
                    officeModel.setTreeLeaf(MesManagerConstant.NUM_1.toString());
                    officeModel.setTreeLevel(MesManagerConstant.NUM_1);
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setViewCode(tmp.getFnumber());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setFullName(tmp.getFname());
                    officeModel.setOfficeType(MesManagerConstant.SUPPLIER_TYPE);
                    officeModel.setStatus(MesManagerConstant.NUM_0.toString());
                    officeModel.setCreateBy(MesManagerConstant.ERP);
                    officeModel.setCreateDate(LocalDateTime.now());
                    officeModel.setUpdateBy(MesManagerConstant.ERP);
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setCorpCode(MesManagerConstant.NUM_0.toString());
                    save(officeModel);
                }
            }
        }
        return true;
    }
}




