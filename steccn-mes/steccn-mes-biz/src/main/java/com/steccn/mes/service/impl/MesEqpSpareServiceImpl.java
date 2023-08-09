package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.vo.R;
import com.steccn.mes.domain.MesEqpClassification;
import com.steccn.mes.domain.MesEqpSpare;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.mapper.MesEqpClassificationMapper;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.mapper.MesEqpSpareMapper;
import com.steccn.mes.vo.ErpMaterialVO;
import com.steccn.mes.vo.MesSpareDownloadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【mes_eqp_spare(备品备件表)】的数据库操作Service实现
* @createDate 2023-08-04 13:15:46
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEqpSpareServiceImpl extends ServiceImpl<MesEqpSpareMapper, MesEqpSpare>
    implements MesEqpSpareService{
    private final RemoteErpFeignApi remoteErpFeignApi;
    private final MesEqpClassificationMapper mesEqpClassificationMapper;

    @Override
    public List<MesSpareDownloadVO> downloadSpare() {
        List<MesEqpSpare> list = list(new LambdaQueryWrapper<MesEqpSpare>().eq(MesEqpSpare::getStatus, "0"));
        List<MesSpareDownloadVO> result = Lists.newArrayList();
        list.forEach(tmp->{
            MesSpareDownloadVO tmpResult = new MesSpareDownloadVO();
            BeanUtil.copyProperties(tmp,tmpResult);
            result.add(tmpResult);
        });
        return result;
    }

    @Override
    public Boolean synchronousSpare(ErpCustomerDTO dto) {
        List<ErpMaterialVO> erpMaterial = remoteErpFeignApi.getErpMaterial(dto).getData();
        if(CollUtil.isNotEmpty(erpMaterial)){
            erpMaterial.forEach(tmp->{
                //先查看是否有该物料
                MesEqpSpare one = getOne(new LambdaQueryWrapper<MesEqpSpare>()
                        .eq(MesEqpSpare::getCode, tmp.getFnumber())
                        .eq(MesEqpSpare::getStatus, "0"), false);
                if(Objects.nonNull(one)&&!one.getName().equals(tmp.getFname())){
                    MesEqpSpare model = new MesEqpSpare();
                    model.setId(one.getId());
                    model.setName(tmp.getFname());
                    model.setSpecifications(tmp.getFspecification());
                    updateById(model);
                }else {
                    MesEqpSpare model = new MesEqpSpare();
                    model.setCode(tmp.getFnumber());
                    model.setName(tmp.getFname());
                    model.setSpecifications(tmp.getFspecification());
                    model.setBatchType("0");
                    model.setState("0");
                    model.setStatus("0");
                    model.setCreateBy("erp");
                    model.setCreateDate(LocalDateTime.now());
                    model.setUpdateBy("erp");
                    model.setUpdateDate(LocalDateTime.now());
                    MesEqpClassification mesEqpClassification = mesEqpClassificationMapper.selectOne(new LambdaQueryWrapper<MesEqpClassification>()
                            .eq(MesEqpClassification::getClassificationCode, tmp.getFmaterialgroupno()));
                    if(Objects.isNull(mesEqpClassification)){
                        throw new BusinessException("没有分类类型");
                    }
                    model.setType(mesEqpClassification.getId());
                    save(model);
                }
            });
        }
        return true;
    }
}




