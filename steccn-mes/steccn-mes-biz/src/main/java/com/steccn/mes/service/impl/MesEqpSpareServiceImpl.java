package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.mes.config.SrmUrl;
import com.steccn.mes.domain.JsSysDictData;
import com.steccn.mes.domain.MesEqpClassification;
import com.steccn.mes.domain.MesEqpSpare;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.dto.SrmQueryDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.mes.mapper.MesEqpClassificationMapper;
import com.steccn.mes.service.JsSysDictDataService;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.mapper.MesEqpSpareMapper;
import com.steccn.mes.vo.ErpMaterialUnitVO;
import com.steccn.mes.vo.ErpMaterialVO;
import com.steccn.mes.vo.MesSpareDownloadVO;
import com.steccn.mes.vo.MesSpareSupplierVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.feign.RemoteSrmFeignApi;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private final RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    private final SrmUrl srmConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final RemoteSrmFeignApi remoteSrmFeignApi;

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
                //&&!one.getName().equals(tmp.getFname())
                if(Objects.nonNull(one)){
                    MesEqpSpare model = new MesEqpSpare();
                    model.setId(one.getId());
                    model.setName(tmp.getFname());
                    model.setSpecifications(tmp.getFspecification());
                    //以下为定时任务时使用
//                    model.setUpdateBy("erp");
//                    model.setUpdateDate(LocalDateTime.now());
//                    //操作长宽厚
//                    JsSysDictData measureUnit1 = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
//                            .eq(JsSysDictData::getDictLabel, tmp.getFvolumename())
//                            .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
//                    model.setLengthQuantity(tmp.getFlength());
//                    model.setLengthUnitId(Long.parseLong(measureUnit1.getDictCode()));
//                    model.setWideQuantity(tmp.getFwidth());
//                    model.setWideUnitId(Long.parseLong(measureUnit1.getDictCode()));
//                    model.setThickQuantity(tmp.getFland());
//                    model.setThickUnitId(Long.parseLong(measureUnit1.getDictCode()));
//                    MesEqpClassification mesEqpClassification = mesEqpClassificationMapper.selectOne(new LambdaQueryWrapper<MesEqpClassification>()
//                            .eq(MesEqpClassification::getClassificationCode, tmp.getFmaterialgroupno()));
//                    if(Objects.isNull(mesEqpClassification)&&!tmp.getFmaterialgroupid().equals(OaConstant.NO.toString())){
//                        System.out.println(tmp.getFmaterialgroupno());
//                        throw new BusinessException("没有分类类型");
//                    }
//                    if(!tmp.getFmaterialgroupid().equals(OaConstant.NO.toString())){
//                        model.setType(mesEqpClassification.getId());
//                    }
//                    //单位
//                    JsSysDictData measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
//                            .eq(JsSysDictData::getDictLabel, tmp.getFbaseunitname())
//                            .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
//                    model.setUnit(measureUnit.getDictLabel());
//                    model.setUnitId(Long.parseLong(measureUnit.getDictCode()));
                    //以上为定时任务时使用
                    updateById(model);
                }else if(Objects.isNull(one)){
                    MesEqpSpare model = new MesEqpSpare();
                    model.setCode(tmp.getFnumber());
                    model.setName(tmp.getFname());
                    model.setSpecifications(tmp.getFspecification());
                    model.setBatchType("0");
                    model.setState("1");
                    model.setStatus("0");
                    model.setCreateBy("erp");
                    model.setCreateDate(LocalDateTime.now());
                    model.setUpdateBy("erp");
                    model.setUpdateDate(LocalDateTime.now());
                    //操作长宽厚
                    String unitName;
                    if(StrUtil.isNotBlank(tmp.getFprounitname())){
                        unitName = tmp.getFprounitname();
                    }else {
                        unitName = tmp.getFvolumename();
                    }
                    JsSysDictData measureUnit1 = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                            .eq(JsSysDictData::getDictLabel, unitName)
                            .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                    //如果还是为空则加入
                    if(Objects.isNull(measureUnit1)){
                        JsSysDictData insertModel = new JsSysDictData();
                        insertModel.setParentCode("0");
                        insertModel.setParentCodes("0");
                        insertModel.setTreeSort(1565);
                        insertModel.setTreeSorts("0000001565,");
                        insertModel.setTreeLeaf("1");
                        insertModel.setTreeLevel(0);
                        insertModel.setTreeNames(unitName);
                        insertModel.setDictLabel(unitName);
                        insertModel.setDictValue(unitName);
                        insertModel.setDictType("measure_unit");
                        insertModel.setIsSys("1");
                        insertModel.setStatus("0");
                        insertModel.setCreateBy("system");
                        insertModel.setCreateDate(LocalDateTime.now());
                        insertModel.setUpdateBy("system");
                        insertModel.setUpdateDate(LocalDateTime.now());
                        insertModel.setCorpCode("0");
                        jsSysDictDataService.save(insertModel);
                        measureUnit1 = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                                .eq(JsSysDictData::getDictLabel, unitName)
                                .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                    }
                    model.setLengthQuantity(tmp.getFlength());
                    model.setLengthUnitId(Long.parseLong(measureUnit1.getDictCode()));
                    model.setWideQuantity(tmp.getFwidth());
                    model.setWideUnitId(Long.parseLong(measureUnit1.getDictCode()));
                    model.setThickQuantity(tmp.getFland());
                    model.setThickUnitId(Long.parseLong(measureUnit1.getDictCode()));
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
                    if(Objects.isNull(measureUnit)){
                        measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                                .eq(JsSysDictData::getDictLabel, tmp.getFprounitname())
                                .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                    }
                    model.setUnit(measureUnit.getDictLabel());
                    model.setUnitId(Long.parseLong(measureUnit.getDictCode()));
                    save(model);
                }
            });
        }
        return true;
    }

    @Override
    public Boolean synchronousSpareLength(ErpCustomerDTO dto) {
        List<ErpMaterialVO> erpMaterial = remoteErpFeignApi.getErpMaterial(dto).getData();
        if(CollUtil.isNotEmpty(erpMaterial)){
            List<MesEqpSpare> lists = list(new LambdaQueryWrapper<MesEqpSpare>()
                    .select(MesEqpSpare::getCode,MesEqpSpare::getName,MesEqpSpare::getId,MesEqpSpare::getUnit)
                    .eq(MesEqpSpare::getStatus, "0"));
            erpMaterial.forEach(tmp->{
                //先查看是否有该物料
                MesEqpSpare one = lists.stream().filter(v->v.getCode().equals(tmp.getFnumber())).findFirst().orElse(null);
                if(Objects.nonNull(one)){
                    MesEqpSpare model = new MesEqpSpare();
                    model.setId(one.getId());
                    model.setUpdateDate(LocalDateTime.now());
//                    JsSysDictData measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
//                            .eq(JsSysDictData::getDictLabel, tmp.getFvolumename())
//                            .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
//                    model.setLengthQuantity(tmp.getFlength());
//                    model.setLengthUnitId(Long.parseLong(measureUnit.getDictCode()));
//                    model.setWideQuantity(tmp.getFwidth());
//                    model.setWideUnitId(Long.parseLong(measureUnit.getDictCode()));
//                    model.setThickQuantity(tmp.getFland());
//                    model.setThickUnitId(Long.parseLong(measureUnit.getDictCode()));
                    //修改单位 如果生产单位和基本单位不同 则把基本单位改成生产单位
                    if(StrUtil.isNotBlank(tmp.getFprounitname())&&!one.getUnit().equals(tmp.getFprounitname())){
                        JsSysDictData measureUnit = jsSysDictDataService.getOne(new LambdaQueryWrapper<JsSysDictData>()
                                .eq(JsSysDictData::getDictLabel, tmp.getFprounitname())
                                .eq(JsSysDictData::getDictType, "measure_unit").last("LIMIT 1"));
                        if(Objects.isNull(measureUnit)){
                            JsSysDictData dicModel = new JsSysDictData();
                            dicModel.setParentCode("0");
                            dicModel.setParentCodes("0");
                            dicModel.setTreeSort(1565);
                            dicModel.setTreeSorts("0000001565,");
                            dicModel.setTreeLeaf("1");
                            dicModel.setTreeLevel(0);
                            dicModel.setDictLabel(tmp.getFprounitname());
                            dicModel.setDictValue(tmp.getFprounitname());
                            dicModel.setDictType("measure_unit");
                            dicModel.setIsSys("1");
                            dicModel.setTreeNames(tmp.getFprounitname());
                            dicModel.setStatus("0");
                            dicModel.setCreateBy("system");
                            dicModel.setUpdateBy("system");
                            dicModel.setCreateDate(new Date());
                            dicModel.setUpdateDate(new Date());
                            jsSysDictDataService.save(dicModel);
                            model.setUnit(dicModel.getDictLabel());
                            model.setUnitId(Long.parseLong(dicModel.getDictCode()));
                            updateById(model);
                        }else {
                            model.setUnit(measureUnit.getDictLabel());
                            model.setUnitId(Long.parseLong(measureUnit.getDictCode()));
                            updateById(model);
                        }
                    }
//                    updateById(model);
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

    @Override
    public Boolean synchronousSpareToQms() {
        List<MesEqpSpare> lists = list(new LambdaQueryWrapper<MesEqpSpare>()
                .eq(MesEqpSpare::getStatus, "0"));
        lists.forEach(tmp->{
            EqpSpareDto dto = new EqpSpareDto();
            BeanUtils.copyProperties(tmp,dto);
            dto.setId(Long.parseLong(tmp.getId()));
            Boolean result = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.synchronizeMaterial(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        });
        return true;
    }

    @Override
    public List<MesSpareSupplierVO> querySpareSupplier(String code) {
        String srmToken = stringRedisTemplate.opsForValue().get("SrmOauthToken::SimpleKey []");
        if(StrUtil.isBlank(srmToken)){
            srmToken = JSONObject.parseObject(remoteSrmFeignApi.getOauthToken()).getString("access_token");;
        }
        Map<String,String> headers = Maps.newHashMap();
        headers.put("Authorization",srmToken);
        SrmQueryDTO dto = new SrmQueryDTO();
        dto.setItemCodes(com.google.common.collect.Lists.newArrayList(code));
        String result = HttpUtil.createPost(srmConfig.getSrmIp()+"/hitf/v2p/rest/invoke/U1JNOk9BX1RPX1NSTV9JVEVNOnNybS1wdXJjaGFzZS1jb29wZXJhdGlvbi5vYS1pdGVtLXN1cHBsaWVyLXF1ZXJ5LmdldEl0ZW1TdXBwbGllcg==").addHeaders(headers).body(new cn.hutool.json.JSONObject(dto).toString()).execute().body();
        JSONObject payloadObj = JSONObject.parseObject(result).getJSONObject("payload");
        if(payloadObj.getString("responseStatus").equals("ERROR")){
            throw new BusinessException(payloadObj.getString("responseMessage"));
        }
        if(payloadObj.getJSONArray("restResponseDtlDTOList").getJSONObject(0).getString("responseStatus").equals("ERROR")){
            throw new BusinessException(payloadObj.getJSONArray("restResponseDtlDTOList").getJSONObject(0).getString("responseMessage"));
        }
        List<MesSpareSupplierVO> results = payloadObj.getJSONArray("itemSuppliers").getJSONObject(0).getJSONArray("suppliers").toJavaList(MesSpareSupplierVO.class);
        return results;
    }
}




