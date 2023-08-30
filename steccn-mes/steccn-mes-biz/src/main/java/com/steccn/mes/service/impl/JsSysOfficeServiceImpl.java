package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.mes.constant.MesManagerConstant;
import com.steccn.mes.domain.JsSysCustomerDetail;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.mapper.JsSysCustomerDetailMapper;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.vo.ErpCustomerVO;
import com.steccn.mes.vo.ErpStockVO;
import com.steccn.mes.vo.ErpSupplierVO;
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

    private final JsSysCustomerDetailMapper jsSysCustomerDetailMapper;

    private final JsSysOfficeMapper jsSysOfficeMapper;

    @Override
    @Transactional
    public Boolean synchronousCustomer(ErpCustomerDTO dto) {
        JSONObject jsonObject = JSONObject.parseObject(remoteErpFeignApi.getErpCustomer(dto));
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ErpCustomerVO> erpData = Lists.newArrayList();
        if(jsonArray.size() > 0){
            for(int i = 0; i < jsonArray.size(); i++){
                ErpCustomerVO erpCustomerVO = jsonArray.getJSONObject(i).toJavaObject(ErpCustomerVO.class);
                System.out.println(i);
                List<String> fContactList = Lists.newArrayList();
                JSONArray contactArray = jsonArray.getJSONObject(i).getJSONArray("FCONTACT");
                if(contactArray.size() > 0){
                    for(int j = 0;j < fContactList.size();j++){
                        fContactList.add(contactArray.getJSONObject(i).getString("FCONTACTNAME"));
                    }
                }
                erpCustomerVO.setFCONTACTLIST(fContactList);
                erpData.add(erpCustomerVO);
            }
        }
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
                JsSysOffice officeOne = customerList.stream().filter(v -> v.getOfficeCode().equals(tmp.getFNUMBER())).findFirst().orElse(null);
                //名字不同的情况则需要修改
                if(Objects.nonNull(officeOne)&&!officeOne.getOfficeName().equals(tmp.getFNAME())){
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFNAME());
                    officeModel.setOfficeName(tmp.getFNAME());
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setFullName(tmp.getFNAME());
                    update(officeModel,new LambdaQueryWrapper<JsSysOffice>().eq(JsSysOffice::getOfficeCode,tmp.getFNUMBER()));
                }else if(Objects.isNull(officeOne)){
                    //没有值则新增
                    JsSysOffice officeModel = new JsSysOffice();
                    officeModel.setOfficeCode(tmp.getFNUMBER());
                    officeModel.setParentCode(MesManagerConstant.customerGroup);
                    officeModel.setParentCodes("0,"+MesManagerConstant.customerGroup+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);
                    officeModel.setTreeSorts(customerPoint.getTreeSorts()+"0000000030,");
                    officeModel.setTreeLeaf(MesManagerConstant.NUM_1.toString());
                    officeModel.setTreeLevel(MesManagerConstant.NUM_1);
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFNAME());
                    officeModel.setViewCode(tmp.getFNUMBER());
                    officeModel.setOfficeName(tmp.getFNAME());
                    officeModel.setFullName(tmp.getFNAME());
                    officeModel.setOfficeType(MesManagerConstant.CUSTOMER_TYPE);
                    officeModel.setStatus(MesManagerConstant.NUM_0.toString());
                    officeModel.setCreateBy(MesManagerConstant.ERP);
                    officeModel.setCreateDate(LocalDateTime.now());
                    officeModel.setUpdateBy(MesManagerConstant.ERP);
                    officeModel.setUpdateDate(LocalDateTime.now());
                    officeModel.setCorpCode(MesManagerConstant.NUM_0.toString());
                    save(officeModel);
                }
                jsSysCustomerDetailMapper.delete(new LambdaQueryWrapper<JsSysCustomerDetail>()
                        .eq(JsSysCustomerDetail::getOfficeCode,tmp.getFNUMBER()));
                JsSysCustomerDetail detailModel = new JsSysCustomerDetail();
                detailModel.setOfficeCode(tmp.getFNUMBER());
                detailModel.setCustomerStatus(tmp.getFDOCUMENTSTATUS());
                detailModel.setForbidStatus(tmp.getFFORBIDSTATUS());
                detailModel.setOrgName(tmp.getORGNAME());
                detailModel.setApproverName(tmp.getFAPPROVERIDFNAME());
                detailModel.setApproveDate(tmp.getFAPPROVEDATE());
                detailModel.setGroupName(tmp.getFGROUPFNAMEW());
                detailModel.setRegisterAddress(tmp.getFREGISTERADDRESS());
                detailModel.setTaxRegisterCode(tmp.getFTAXREGISTERCODE());
                detailModel.setInvoiceTitle(tmp.getFINVOICETITLE());
                detailModel.setInvoiceBankName(tmp.getFINVOICEBANKNAME());
                detailModel.setSocialCreCode(tmp.getFSOCIALCRECODE());
                detailModel.setInvoiceBankAccount(tmp.getFINVOICEBANKACCOUNT());
                detailModel.setInvoiceTel(tmp.getFINVOICETEL());
                detailModel.setInvoiceAddress(tmp.getFINVOICEADDRESS());
                detailModel.setContactList(tmp.getFCONTACTLIST());
                detailModel.setUpdateBy(MesManagerConstant.ERP);
                detailModel.setUpdateDate(LocalDateTime.now());
                detailModel.setCreateBy(MesManagerConstant.ERP);
                detailModel.setCreateDate(LocalDateTime.now());
                jsSysCustomerDetailMapper.insert(detailModel);
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
        List<MesCutomerVO> resultTmp = jsSysOfficeMapper.selectJoinList(MesCutomerVO.class,new MPJLambdaWrapper<JsSysOffice>()
                .selectAs(JsSysOffice::getOfficeCode,MesCutomerVO::getCustomerCode)
                .selectAs(JsSysOffice::getOfficeName,MesCutomerVO::getName)
                .selectAs(JsSysOffice::getUpdateDate,MesCutomerVO::getLastModifiedDateTime)
                .selectAs(JsSysCustomerDetail::getCustomerStatus,MesCutomerVO::getCustomerStatus)
                .selectAs(JsSysCustomerDetail::getForbidStatus,MesCutomerVO::getForbidStatus)
                .selectAs(JsSysCustomerDetail::getOrgName,MesCutomerVO::getOrgName)
                .selectAs(JsSysCustomerDetail::getApproverName,MesCutomerVO::getApproverName)
                .selectAs(JsSysCustomerDetail::getApproveDate,MesCutomerVO::getApproveDate)
                .selectAs(JsSysCustomerDetail::getGroupName,MesCutomerVO::getGroupName)
                .selectAs(JsSysCustomerDetail::getRegisterAddress,MesCutomerVO::getRegisterAddress)
                .selectAs(JsSysCustomerDetail::getTaxRegisterCode,MesCutomerVO::getTaxRegisterCode)
                .selectAs(JsSysCustomerDetail::getInvoiceTitle,MesCutomerVO::getInvoiceTitle)
                .selectAs(JsSysCustomerDetail::getInvoiceBankName,MesCutomerVO::getInvoiceBankName)
                .selectAs(JsSysCustomerDetail::getSocialCreCode,MesCutomerVO::getSocialCreCode)
                .selectAs(JsSysCustomerDetail::getInvoiceBankAccount,MesCutomerVO::getInvoiceBankAccount)
                .selectAs(JsSysCustomerDetail::getInvoiceTel,MesCutomerVO::getInvoiceTel)
                .selectAs(JsSysCustomerDetail::getInvoiceAddress,MesCutomerVO::getInvoiceAddress)
                .selectAs(JsSysCustomerDetail::getContactList,MesCutomerVO::getContactList)
                .selectAs(JsSysCustomerDetail::getContactName,MesCutomerVO::getContactName)
                .leftJoin(JsSysCustomerDetail.class,JsSysCustomerDetail::getOfficeCode,JsSysOffice::getOfficeCode)
                .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0)
                .eq(JsSysOffice::getOfficeType, MesManagerConstant.CUSTOMER_TYPE)
                .between(JsSysOffice::getUpdateDate, dto.getStartfmodifydate(), dto.getStopfmodifydate()));
        if(CollUtil.isNotEmpty(resultTmp)){
            results = resultTmp;
        }
        return results;
    }

    @Override
    public Boolean synchronousSupplier(ErpCustomerDTO dto) {
        List<ErpSupplierVO> erpData = remoteErpFeignApi.getErpSupplier(dto).getData();
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
            for(ErpSupplierVO tmp:erpData){
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

    @Override
    public Boolean synchronousStock(ErpCustomerDTO dto) {
        List<ErpStockVO> erpData = remoteErpFeignApi.getStock(dto).getData();
        if(CollUtil.isNotEmpty(erpData)){
            //拿到仓库分组节点
            JsSysOffice customerPoint = getOne(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getOfficeCode, MesManagerConstant.STOCK_GROUP)
                    .last("LIMIT 1"));
            //拿到表中已有的供应商
            List<JsSysOffice> supplierList = list(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, MesManagerConstant.STOCK_TYPE)
                    .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0.toString()));
            for(ErpStockVO tmp:erpData){
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
                    officeModel.setParentCode(MesManagerConstant.STOCK_GROUP);
                    officeModel.setParentCodes("0,"+MesManagerConstant.STOCK_GROUP+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);
                    officeModel.setTreeSorts(customerPoint.getTreeSorts()+"0000000030,");
                    officeModel.setTreeLeaf(MesManagerConstant.NUM_1.toString());
                    officeModel.setTreeLevel(MesManagerConstant.NUM_1);
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setViewCode(tmp.getFnumber());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setFullName(tmp.getFname());
                    officeModel.setOfficeType(MesManagerConstant.STOCK_TYPE);
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
    public Boolean synchronousStock(ErpCustomerDTO dto) {
        List<ErpStockVO> erpData = remoteErpFeignApi.getStock(dto).getData();
        if(CollUtil.isNotEmpty(erpData)){
            //拿到仓库分组节点
            JsSysOffice customerPoint = getOne(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getOfficeCode, MesManagerConstant.STOCK_GROUP)
                    .last("LIMIT 1"));
            //拿到表中已有的供应商
            List<JsSysOffice> supplierList = list(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, MesManagerConstant.STOCK_TYPE)
                    .eq(JsSysOffice::getStatus, MesManagerConstant.NUM_0.toString()));
            for(ErpStockVO tmp:erpData){
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
                    officeModel.setParentCode(MesManagerConstant.STOCK_GROUP);
                    officeModel.setParentCodes("0,"+MesManagerConstant.STOCK_GROUP+",");
                    officeModel.setTreeSort(MesManagerConstant.NUM_30);
                    officeModel.setTreeSorts(customerPoint.getTreeSorts()+"0000000030,");
                    officeModel.setTreeLeaf(MesManagerConstant.NUM_1.toString());
                    officeModel.setTreeLevel(MesManagerConstant.NUM_1);
                    officeModel.setTreeNames(customerPoint.getOfficeName()+"/"+tmp.getFname());
                    officeModel.setViewCode(tmp.getFnumber());
                    officeModel.setOfficeName(tmp.getFname());
                    officeModel.setFullName(tmp.getFname());
                    officeModel.setOfficeType(MesManagerConstant.STOCK_TYPE);
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




