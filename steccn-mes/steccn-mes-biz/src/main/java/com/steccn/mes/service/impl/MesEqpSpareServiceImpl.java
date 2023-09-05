package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.vo.R;
import com.steccn.mes.domain.JsSysDictData;
import com.steccn.mes.domain.MesEqpClassification;
import com.steccn.mes.domain.MesEqpSpare;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.mapper.MesEqpClassificationMapper;
import com.steccn.mes.service.JsSysDictDataService;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.mapper.MesEqpSpareMapper;
import com.steccn.mes.vo.ErpMaterialUnitVO;
import com.steccn.mes.vo.ErpMaterialVO;
import com.steccn.mes.vo.MesSpareDownloadVO;
import com.steccn.oa.constant.OaConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JsSysDictDataService jsSysDictDataService;

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
            List<MesEqpSpare> lists = list(new LambdaQueryWrapper<MesEqpSpare>()
                    .select(MesEqpSpare::getCode,MesEqpSpare::getName,MesEqpSpare::getId)
                    .eq(MesEqpSpare::getStatus, "0"));
            erpMaterial.forEach(tmp->{
                //先查看是否有该物料
                MesEqpSpare one = lists.stream().filter(v->v.getCode().equals(tmp.getFnumber())).findFirst().orElse(null);
                if(Objects.nonNull(one)&&!one.getName().equals(tmp.getFname())){
                    MesEqpSpare model = new MesEqpSpare();
                    model.setId(one.getId());
                    model.setName(tmp.getFname());
                    model.setSpecifications(tmp.getFspecification());
                    updateById(model);
                }else if(Objects.isNull(one)){
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
                    if(Objects.isNull(mesEqpClassification)&&!tmp.getFmaterialgroupid().equals(OaConstant.NO.toString())){
                        System.out.println(tmp.getFmaterialgroupno());
                        throw new BusinessException("没有分类类型");
                    }
                    if(!tmp.getFmaterialgroupid().equals(OaConstant.NO.toString())){
                        model.setType(mesEqpClassification.getId());
                    }
                    //单位
                    JsSysDictData measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                            .eq(JsSysDictData::getDictLabel, tmp.getFbaseunitname())
                            .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                    model.setUnit(measureUnit.getDictLabel());
                    model.setUnitId(Long.parseLong(measureUnit.getDictCode()));
                    save(model);
                }
            });
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean synchronousSpareUnit(ErpCustomerDTO dto) {
        List<ErpMaterialUnitVO> erpMaterial = remoteErpFeignApi.getErpMaterialUnit(dto).getData();
        if(CollUtil.isNotEmpty(erpMaterial)){
            erpMaterial.forEach(tmp -> {
                JsSysDictData measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                        .eq(JsSysDictData::getDictLabel, tmp.getFbaseunitname())
                        .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                MesEqpSpare model = new MesEqpSpare();
                model.setUnit(measureUnit.getDictLabel());
                model.setUnitId(Long.parseLong(measureUnit.getDictCode()));
                update(model,new LambdaQueryWrapper<MesEqpSpare>()
                        .eq(MesEqpSpare::getCode,tmp.getFnumber()));
            });
        }
        return true;
    }
}




