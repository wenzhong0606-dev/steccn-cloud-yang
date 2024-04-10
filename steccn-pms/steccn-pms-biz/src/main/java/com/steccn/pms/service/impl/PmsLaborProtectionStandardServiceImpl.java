package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.mes.feign.RemoteMesFeignApi;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsLaborProtectionStandard;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.dto.PmsLaborStandardDTO;
import com.steccn.pms.dto.PmsLaborStandardDetailDTO;
import com.steccn.pms.dto.PmsStandardListDTO;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import com.steccn.pms.service.PmsLaborProtectionStandardService;
import com.steccn.pms.mapper.PmsLaborProtectionStandardMapper;
import com.steccn.pms.vo.PmsLaborStandardDetailVO;
import com.steccn.pms.vo.PmsLaborStandardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【pms_labor_protection_standard(劳保标准表)】的数据库操作Service实现
* @createDate 2024-04-03 10:21:46
*/
@Service
@RequiredArgsConstructor
public class PmsLaborProtectionStandardServiceImpl extends ServiceImpl<PmsLaborProtectionStandardMapper, PmsLaborProtectionStandard>
    implements PmsLaborProtectionStandardService{
    private final PmsLaborProtectionStandardDetailService pmsLaborProtectionStandardDetailService;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteMesFeignApi remoteMesFeignApi;
    private final OaInfoService oaInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(PmsLaborStandardDTO dto) {
        //需要去除物料的重复值
        boolean hasDuplicateMaterialId = dto.getDetails().stream()
                .map(PmsLaborStandardDetailDTO::getMaterialId)
                .collect(Collectors.toSet())
                .size() < dto.getDetails().size();
        Assert.isFalse(hasDuplicateMaterialId,"同一个标准中不能有重复的物料");
        //保存主表
        PmsLaborProtectionStandard model = new PmsLaborProtectionStandard();
        BeanUtil.copyProperties(dto,model);
        save(model);
        //保存详情表
        List<PmsLaborProtectionStandardDetail> models = Lists.newArrayList();
        dto.getDetails().forEach(tmp->{
            PmsLaborProtectionStandardDetail detail = new PmsLaborProtectionStandardDetail();
            BeanUtil.copyProperties(tmp,detail);
            detail.setStandardId(model.getId());
            models.add(detail);
        });
        pmsLaborProtectionStandardDetailService.saveBatch(models);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(PmsLaborStandardDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("id不能为空");
        }
        //需要去除物料的重复值
        boolean hasDuplicateMaterialId = dto.getDetails().stream()
                .map(PmsLaborStandardDetailDTO::getMaterialId)
                .collect(Collectors.toSet())
                .size() < dto.getDetails().size();
        Assert.isFalse(hasDuplicateMaterialId,"同一个标准中不能有重复的物料");
        //修改主表
        PmsLaborProtectionStandard model = new PmsLaborProtectionStandard();
        BeanUtil.copyProperties(dto,model);
        updateById(model);
        //修改从表
        pmsLaborProtectionStandardDetailService.remove(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .eq(PmsLaborProtectionStandardDetail::getStandardId,dto.getId()));
        List<PmsLaborProtectionStandardDetail> models = Lists.newArrayList();
        dto.getDetails().forEach(tmp->{
            PmsLaborProtectionStandardDetail detail = new PmsLaborProtectionStandardDetail();
            BeanUtil.copyProperties(tmp,detail);
            detail.setStandardId(dto.getId());
            models.add(detail);
        });
        pmsLaborProtectionStandardDetailService.saveBatch(models);
        return true;
    }

    @Override
    public PmsLaborStandardVO info(Integer id) {
        PmsLaborStandardVO vo = new PmsLaborStandardVO();
        PmsLaborProtectionStandard standard = getById(id);
        BeanUtil.copyProperties(standard,vo);
        List<PmsLaborProtectionStandardDetail> standardDetail = pmsLaborProtectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .eq(PmsLaborProtectionStandardDetail::getStandardId, id));
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(standardDetail.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).collect(Collectors.toList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<PmsLaborStandardDetailVO> details = Lists.newArrayList();
        standardDetail.forEach(tmp->{
            PmsLaborStandardDetailVO detail = new PmsLaborStandardDetailVO();
            BeanUtil.copyProperties(tmp,detail);
            detail.setMaterialName(materialMap.get(tmp.getMaterialId()).getName());
            details.add(detail);
        });
        vo.setDetails(details);
        return vo;
    }

    @Override
    public Boolean batchDeleteById(PmsLaborStandardDTO id) {
        removeByIds(id.getIds());
        pmsLaborProtectionStandardDetailService.remove(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .in(PmsLaborProtectionStandardDetail::getStandardId,id));
        return true;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> listInfo() {
        List<PmsLaborProtectionStandard> data = list();
        Assert.notEmpty(data,"数据为空");
        SteccnTableDataDTO<Map<String, String>> result = new SteccnTableDataDTO<>();
        //先组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("num","序号",null));
        columns.add(new SteccnTableColumn("officeName","机构",null));
        //准备机构、物料、单位
        List<PmsLaborProtectionStandardDetail> detailData = pmsLaborProtectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .orderByDesc(PmsLaborProtectionStandardDetail::getStandardId));
        //机构
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(data.stream().flatMap(eq -> eq.getOfficeId().stream()).collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Long> materialIdList = detailData.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //收集unitId
        Map<Long, String> unitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(spareMap.values().stream().map(EqpSpareVO::getUnitId).collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SteccnTableColumn complexColumn = new SteccnTableColumn();
        StringBuilder tableName = new StringBuilder();
        tableName.append("劳保用品发放标准(");
        for (int i = 0; i <materialIdList.size(); i++){
            String unitName = unitMap.get(spareMap.get(materialIdList.get(i)).getUnitId());
            if(i==materialIdList.size()-1){
                tableName.append(unitName);
                tableName.append("/月)");
            }else {
                tableName.append(unitName);
                tableName.append("、");
            }

        }
        complexColumn.setProp("labor");
        complexColumn.setLabel(tableName.toString());
        List<SteccnTableColumn> complexColumnChildren = Lists.newArrayList();
        complexColumnChildren.add(new SteccnTableColumn("riskFactor","职业健康风险因素",null));
        materialIdList.forEach(tmp->{
            complexColumnChildren.add(new SteccnTableColumn(tmp.toString(),spareMap.get(tmp).getName(),null));
        });
        complexColumn.setChildren(complexColumnChildren);
        columns.add(complexColumn);
        //开始组数据
        List<Map<String, String>> tableList = Lists.newArrayList();
        int index = 1;
        for(PmsLaborProtectionStandard tmp:data){
            Map<String,String> column = Maps.newHashMap();
            column.put("id",tmp.getId().toString());
            //处理机构
            StringBuilder officeName = new StringBuilder();
            for(int i = 0; i < tmp.getOfficeId().size();i++){
                officeName.append(officeMap.get(tmp.getOfficeId().get(i)).getOfficeName());
                if(i!=tmp.getOfficeId().size() - 1){
                    officeName.append("、");
                }
            }
            column.put("num",String.valueOf(index));
            index++;
            column.put("officeName",officeName.toString());
            column.put("riskFactor", StrUtil.isBlank(tmp.getRiskFactor()) ? null:tmp.getRiskFactor());
            materialIdList.forEach(material->{
                PmsLaborProtectionStandardDetail detail = detailData.stream().filter(v -> v.getStandardId().equals(tmp.getId()) && v.getMaterialId().equals(material)).findFirst().orElse(null);
                String value;
                if(Objects.isNull(detail)){
                    value = null;
                }else {
                    value = detail.getAmount()+"/"+detail.getPeriod();
                }
                column.put(material.toString(),value);
            });
            tableList.add(column);
        }
        result.setTableList(tableList);
        result.setColumns(columns);
        return result;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> standardList(PmsStandardListDTO dto) {
        //需要拿到所有配置的数据
        List<PmsLaborProtectionStandard> standardList = list();
        //拿到所有详情
        List<PmsLaborProtectionStandardDetail> standardDetailList = pmsLaborProtectionStandardDetailService.list();
        //先过滤物料
        if(CollUtil.isNotEmpty(dto.getMaterialId())){
            standardDetailList = standardDetailList.stream().filter(v->dto.getMaterialId().contains(v.getMaterialId())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(standardDetailList)){
                Set<Integer> standardIdSet = standardDetailList.stream().map(PmsLaborProtectionStandardDetail::getStandardId).collect(Collectors.toSet());
                standardList = standardList.stream().filter(v->standardIdSet.contains(v.getId())).collect(Collectors.toList());
            }
        }
        //处理部门和岗位
        //拿到标准中的所有部门了岗位
        List<PmsLaborProtectionStandard> finalStandardList = standardList;
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(finalStandardList.stream().flatMap(eq -> eq.getOfficeId().stream()).collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<String> availPost = Sets.newHashSet();
        Set<String> availDep = Sets.newHashSet();
        //区分岗位和部门
        for(Map.Entry<String,SysOfficeVO> entry:officeMap.entrySet()){
            SysOfficeVO tmp = entry.getValue();
            if(tmp.getOfficeType().equals("6")){
                availPost.add(tmp.getOfficeCode());
            }else if(tmp.getOfficeType().equals("15")){
                //若是部门类型，则拿到其岗位数据
                availDep.add(tmp.getOfficeCode());
            }
        }
        if(CollUtil.isNotEmpty(availDep)){
            Map<String, List<String>> depPostMap = FeignOptional.of(() -> remoteOfficeFeign.depPostMap(availDep), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            availPost.addAll(depPostMap.values().stream().flatMap(List::stream).collect(Collectors.toSet()));
        }
        //拿到所有岗位下的人员和岗位所属机构
        Map<String, List<String>> employee = FeignOptional.of(() -> remoteMesFeignApi.getPostUser(availPost), FeignConstant.STECCN_MES_SERVICE_NAME).get();
        //拿到所有人员的入职时间
        Map<String, LocalDate> employeeTime = FeignOptional.of(() -> oaInfoService.getCompanyStartTime(employee.keySet()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //拿到所有入职超过1个月的人员
        Set<String> employeeId = employeeTime.entrySet().stream().filter(v -> v.getValue().plusMonths(1).isBefore(LocalDate.now()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        //过滤名称
        if(cn.hutool.core.util.StrUtil.isNotBlank(dto.getUserName())){
            employeeId =employeeId.stream().filter(v->v.contains(dto.getUserName())).collect(Collectors.toSet());
        }
        // TODO: 2024/4/10 获取用户所属部门和岗位 
        return null;
    }
}




