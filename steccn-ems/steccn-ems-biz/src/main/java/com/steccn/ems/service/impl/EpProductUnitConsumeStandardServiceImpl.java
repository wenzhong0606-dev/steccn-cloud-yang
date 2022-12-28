package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.RegularUtil;
import com.steccn.common.vo.StandardVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.mapper.EpProductUnitConsumeStandardMapper;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.ems.vo.EpInstrumentAccountPageVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.CollationKey;
import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_product_unit_consume_standard(产品单耗标准)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
@Slf4j
public class EpProductUnitConsumeStandardServiceImpl extends ServiceImpl<EpProductUnitConsumeStandardMapper, EpProductUnitConsumeStandard>
    implements EpProductUnitConsumeStandardService{
    @Autowired
    private  EpEnergyTypeService epEnergyTypeService;
    @Autowired
    private  RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private  RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private  EpPartitionServiceImpl epPartitionService;

    /**
     * 默认生产类型
     */
    private final Set<Long> DEFAULT_PRODUCT_SET = Sets.newHashSet(0L,1L);

    @Override
    public Page<EpProductUnitConsumeStandardVO> getEpProductUnitConsumeStandardList(EpProductUnitStandardPageDTO dto) {
        //自定义查询条件
        LambdaQueryWrapper<EpProductUnitConsumeStandard> condition = new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                //能源类型
                .eq(Objects.nonNull(dto.getEnergyType()), EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType())
                //标准类型
                .eq(Objects.nonNull(dto.getStandardType()), EpProductUnitConsumeStandard::getStandardType, dto.getStandardType())
                //产品类型
                .eq(Objects.nonNull(dto.getProductType()), EpProductUnitConsumeStandard::getProductType, dto.getProductType())
                //产品厚度
                .eq(Objects.nonNull(dto.getProductThick()), EpProductUnitConsumeStandard::getProductThick, dto.getProductThick())
                //标准名称
                .like(Objects.nonNull(dto.getName()), EpProductUnitConsumeStandard::getName, dto.getName())
                //查询所有可展示的
                .eq(EpProductUnitConsumeStandard::getShowFlag, MpConstant.YES);

        //3.根据条件查询数据库
        List<EpProductUnitConsumeStandard> standards = list(condition);
        //4.拷贝返回信息
        List<EpProductUnitConsumeStandardVO> standardVOS = BeanUtil.copyToList(standards, EpProductUnitConsumeStandardVO.class);
        //5.处理返回数据
        //自定义分页对象
        Page<EpProductUnitConsumeStandardVO> page = new Page<>();
        if (CollUtil.isEmpty(standards)){
            return page;
        }
        Set<Long> dictSet = Sets.newHashSet();
        Set<Integer> energyTypeSet = Sets.newHashSet();
        Set<Integer> officeSet = Sets.newHashSet();
        standardVOS.stream().filter(v ->Objects.nonNull(v)&& Objects.nonNull(v.getStandardType())
                        && Objects.nonNull(v.getMeasureUnit())
                        && Objects.nonNull(v.getEnergyType()))
                .forEach(s -> {
                    Collections.addAll(dictSet, s.getStandardType(), s.getMeasureUnit());
                    if (Objects.nonNull(s.getProductThick())){
                        dictSet.add(s.getProductThick());
                    }
                    if (CollUtil.isNotEmpty(s.getApplyOffice())){
                        s.getApplyOffice().forEach(q->{
                            officeSet.add(q);
                        });
                    }
                    energyTypeSet.add(s.getEnergyType());
                });
        Map<Integer, String> energyTypeMap= epEnergyTypeService.getBySetIds(energyTypeSet);
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
        Map<Integer, SysOfficeVO> officeVOMap = FeignOptional.of(remoteOfficeFeign.listById(officeSet)).get();
        standardVOS.forEach(s->{
            s.setStandardTypeName(dictMap.get(s.getStandardType()));
            if (Objects.nonNull(s)&&Objects.nonNull(s.getProductThick())){
                s.setProductThickName(dictMap.get(s.getProductThick()));
            }
            if (CollUtil.isNotEmpty(officeVOMap)&&CollUtil.isNotEmpty(s.getApplyOffice())){
                List<SysOfficeVO> sysOfficeVOS = Lists.newArrayList();
              s.getApplyOffice().forEach(v->{
                  SysOfficeVO sysOfficeVO = officeVOMap.get(v);
                  sysOfficeVOS.add(sysOfficeVO);
              });
                s.setApplyOfficeVO(sysOfficeVOS);
            }
            s.setMeasureUnitName(dictMap.get(s.getMeasureUnit()));
            s.setEnergyTypeName(energyTypeMap.get(s.getEnergyType()));
            s.setValidity(s.getValidStart().toString()+"~"+s.getValidEnd());
            if (Objects.nonNull(s.getEnergyType()) && DEFAULT_PRODUCT_SET.contains(s.getProductType())){
                s.setProductTypeName(EpManagerConstant.EpProductTypes.getProductType(s.getProductType()).getName());
            }
        });

        //排序+逻辑分页
        if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD.equals(dto.getSortField())){
            standardVOS= listToSortBySubName(standardVOS, dto.getSortField(), dto.getAscFlag());
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_INDATE.equals(dto.getSortField())){
            if (dto.getAscFlag()==Boolean.TRUE){
                standardVOS = standardVOS.stream().sorted(Comparator.comparing(EpProductUnitConsumeStandardVO::getValidStart)).collect(Collectors.toList());
            }else {
                standardVOS = standardVOS.stream().sorted(Comparator.comparing(EpProductUnitConsumeStandardVO::getValidStart).reversed()).collect(Collectors.toList());
            }
        }else {
            standardVOS = standardVOS.stream().sorted(Comparator.comparing(EpProductUnitConsumeStandardVO::getCreateTime)).collect(Collectors.toList());
        }
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
        //逻辑分页
        List<EpProductUnitConsumeStandardVO> collect = standardVOS.stream().skip(skip).limit(dto.getSize()).collect(Collectors.toList());

        page.setCurrent(dto.getCurrent());
        page.setTotal(standardVOS.size());
        page.setSize(dto.getSize());
        page.setRecords(collect);

        return page;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(EpProductUnitStandardDTO dto) {
        if (StringUtils.isNotBlank(dto.getUnitValue().toString())){
            String unit = dto.getUnitValue().toString();
            String lastUnit = deleteCharString0(unit, '.');
            if (lastUnit.length()>16){
               throw new BusinessException("单耗标准值的长度不能超过16位");
           }
        }
        if (dto.getStandardType()==1597504911367864320L){
            if (CollUtil.isEmpty(dto.getApplyOffice())){
                throw new BusinessException("应用部门不能为空");
            }
        }

        //去数据库中根据名称查询产品单耗标准个数
        long size = count(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getName, dto.getName())
                .eq(EpProductUnitConsumeStandard::getShowFlag,MpConstant.YES)
                .ne(Objects.nonNull(dto.getId()),EpProductUnitConsumeStandard::getId,dto.getId()));
            Assert.isTrue(size ==0, "产品单耗标准的名称不能重复");

        //创建domain实体类对象
        EpProductUnitConsumeStandard unitConsumeStandard = new EpProductUnitConsumeStandard();
        //复制传进来的dto
        BeanUtils.copyProperties(dto,unitConsumeStandard);
        if (Objects.isNull(dto.getId())){
            //新建的时候设置标准编码
            long sum = count();
            String code = "BZ"+String.format("%06d", sum+1);
            unitConsumeStandard.setCode(code);
        }
        //检测有效期是否有重叠部分,如果有提示“与现有标准【#现有标准的名称#】有效期有重叠”，无法保存
        if (Objects.nonNull(dto.getStandardType())
                &&Objects.nonNull(dto.getEnergyType())
                &&Objects.nonNull(dto.getProductType())
                &&Objects.nonNull(dto.getProductThick())){
            List<EpProductUnitConsumeStandard> dataList = list(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                    .eq(EpProductUnitConsumeStandard::getStandardType, dto.getStandardType())
                    .eq(EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType())
                    .eq(EpProductUnitConsumeStandard::getProductType, dto.getProductType())
                    .eq(EpProductUnitConsumeStandard::getProductThick, dto.getProductThick())
                  //  .eq(CollUtil.isNotEmpty(dto.getApplyOffice()), EpProductUnitConsumeStandard::getApplyOffice, dto.getApplyOffice())
                    //输入的结束时间小于等于dto的有效期开始时间,表明时间有重叠
                    .lt(EpProductUnitConsumeStandard::getValidStart, dto.getValidEnd().plusDays(1))
                    //输入的开始时间大于等于dto的有效结束时间,表明时间有重叠
                    .gt(EpProductUnitConsumeStandard::getValidEnd, dto.getValidStart().minusDays(1))
                    //可展示的
                    .eq(EpProductUnitConsumeStandard::getShowFlag, MpConstant.YES));
            if (Objects.nonNull(dto.getId())) {
                EpProductUnitConsumeStandard ep = getById(dto.getId());
                //编辑的时候移除对自身的判断
                dataList.remove(ep);
            }
            String names = new String();
            if (CollUtil.isNotEmpty(dataList)){
                    for (int i = 0; i < dataList.size(); i++) {
                        if (i==(dataList.size()-1)){
                            names+=dataList.get(i).getName();
                        }else {
                            names += dataList.get(i).getName()+",";
                        }
                    }
                throw new BusinessException("与现有标准"+names+"有效期有重叠");
            }
        }

        if (Objects.isNull(dto.getId())){
            save(unitConsumeStandard);
        }else {
            EpProductUnitConsumeStandard standard = getById(dto.getId());
            Assert.notNull(standard,"编辑的产品单耗标准不存在");
            if (MpConstant.NO==unitConsumeStandard.getShowFlag()){
                throw new BusinessException("该产品单耗标准已删除无法编辑");
            }
            updateById(unitConsumeStandard);
        }
        return Boolean.TRUE;
    }

    @Override
    public EpProductUnitConsumeStandardVO getProductUnitStandardVO(Integer id) {
        Assert.notNull(id,"id不能为空");
        //根据id查询单耗标准
        EpProductUnitConsumeStandard standard = getById(id);
        if (Objects.isNull(standard)) {
            return null;
        }
        if (MpConstant.NO == standard.getShowFlag()) {
            throw new BusinessException("该产品单耗标准已删除");
        }
        //设置返回实体类对象
        EpProductUnitConsumeStandardVO vo = new EpProductUnitConsumeStandardVO();
        BeanUtils.copyProperties(standard,vo);
        EpEnergyTypeVO epAddEnergyTypeVO = epEnergyTypeService.getEpAddEnergyTypeVO(standard.getEnergyType());
        //设置能源类型名称
        vo.setEnergyTypeName(epAddEnergyTypeVO.getName());
        //如果应用部门不为空,设置应用部门
        if (CollUtil.isNotEmpty(standard.getApplyOffice())){
            Map<Integer, SysOfficeVO> voMap = FeignOptional.of(remoteOfficeFeign.listById(standard.getApplyOffice())).get();
           List<SysOfficeVO> sysOfficeVOS = Lists.newArrayList();
            standard.getApplyOffice().forEach(v->{
                SysOfficeVO sysOfficeVO = voMap.get(v);
                sysOfficeVOS.add(sysOfficeVO);
            });
            vo.setApplyOfficeVO(sysOfficeVOS);
        }
        Set<Long> dictSet = Sets.newHashSet();
        Collections.addAll(dictSet,standard.getStandardType()
                ,standard.getProductThick(),standard.getMeasureUnit());
        if (CollUtil.isNotEmpty(dictSet)){
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
            vo.setStandardTypeName(dictMap.get(vo.getStandardType()));
            vo.setProductThickName(dictMap.get(vo.getProductThick()));
            vo.setMeasureUnitName(dictMap.get(vo.getMeasureUnit()));
        }
        //设置产品类型
            vo.setProductTypeName(EpManagerConstant.EpProductTypes.getProductType(vo.getProductType()).getName());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        EpProductUnitConsumeStandard epStandard = getById(id);
        Assert.notNull(epStandard,"该产品单耗标准不存在或已删除");
        epStandard.setShowFlag(MpConstant.NO);
        updateById(epStandard);
        return Boolean.TRUE;
    }

    @Override
    public List<StandardVO<BigDecimal>> validStandardList(Integer energyType, LocalDate start, LocalDate end) {
        //定义返回集合
        List<StandardVO<BigDecimal>> resList = Lists.newArrayList();

        if (Objects.nonNull(start) && Objects.nonNull(end) && Objects.nonNull(energyType)) {
            //获取有效日期范围内的单耗
            List<EpProductUnitConsumeStandard> dataList = list(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                    .eq(EpProductUnitConsumeStandard::getEnergyType, energyType)
                    .ge(EpProductUnitConsumeStandard::getValidStart, start)
                    .le(EpProductUnitConsumeStandard::getValidEnd, end));
            if (CollUtil.isNotEmpty(dataList)) {
                Set<Long> dictIdSet = Sets.newHashSet();
                dataList.forEach(ele -> {
                    StandardVO<BigDecimal> standardVO = new StandardVO<>();
                    BeanUtils.copyProperties(ele, standardVO);
                    if (Objects.nonNull(ele.getStandardType())) {
                        dictIdSet.add(ele.getStandardType());
                    }
                    if (Objects.nonNull(ele.getProductThick())) {
                        dictIdSet.add(ele.getProductThick());
                    }
                    standardVO.setValue(ele.getUnitValue());
                    resList.add(standardVO);
                });

                //获取数据字典map
                Map<Long, String> dictIdNameMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
                resList.forEach(ele -> {
                    String thickStr = dictIdNameMap.get(ele.getProductThick());
                    //提取字符串中的数值
                    ele.setThick(RegularUtil.extractValue(thickStr, null, (x) -> new BigDecimal(x)));
                    //部门标准，行业标准，公司标准
                    ele.setStandardTypeName(dictIdNameMap.get(ele.getStandardType()));
                    EpManagerConstant.EpProductTypes productType = EpManagerConstant.EpProductTypes.getProductType(ele.getProductType());
                    ele.setProductTypeName(Objects.nonNull(productType) ? productType.getName() : null);
                });
            }
        }
        return resList;
    }

    public String deleteCharString0(String sourceString, char chElemData) {
        String deleteString = "";
        for (int i = 0; i < sourceString.length(); i++) {
            if (sourceString.charAt(i) != chElemData) {
                deleteString += sourceString.charAt(i);
            }
        }
        return deleteString;
    }

    /**
     * 根据某字段首字母排序
     * @param list
     * @param sort
     * @param isOrder
     * @return
     */
    private List<EpProductUnitConsumeStandardVO> listToSortBySubName(List<EpProductUnitConsumeStandardVO> list, String sort, Boolean isOrder) {
        if (list == null || list.size() == 0) {
            return null;
        }
        Map<String, EpProductUnitConsumeStandardVO> map = new HashMap<String, EpProductUnitConsumeStandardVO>();
        List<String> names = new ArrayList<>();
        //储存该字段不为空的集合
        List<EpProductUnitConsumeStandardVO>  haveDataList = new ArrayList<>();
        //储存该字段为空的集合
        List<EpProductUnitConsumeStandardVO>  noDataList = new ArrayList<>();
        if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME.equals(sort)){
          list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
              if (Objects.nonNull(s.getName())&&StringUtils.isNotBlank(s.getName())){
                  haveDataList.add(s);
              }else {
                  noDataList.add(s);
              }
          });
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE.equals(sort)){
            list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
                if (Objects.nonNull(s.getEnergyTypeName())&&StringUtils.isNotBlank(s.getEnergyTypeName())){
                    haveDataList.add(s);
                }else {
                   noDataList.add(s);
                }
            });
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE.equals(sort)){
            list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
                if (Objects.nonNull(s.getStandardTypeName())&&StringUtils.isNotBlank(s.getStandardTypeName())){
                    haveDataList.add(s);
                }else {
                    noDataList.add(s);
                }
            });
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE.equals(sort)){
            list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
                if (Objects.nonNull(s.getProductTypeName())&&StringUtils.isNotBlank(s.getProductTypeName())){
                    haveDataList.add(s);
                }else {
                    noDataList.add(s);
                }
            });
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS.equals(sort)){
            list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
                if (Objects.nonNull(s.getProductThickName())&&StringUtils.isNotBlank(s.getProductThickName())){
                    haveDataList.add(s);
                }else {
                    noDataList.add(s);
                }
            });
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD.equals(sort)){
            list.stream().filter(s->Objects.nonNull(s)).forEach(s->{
                if (Objects.nonNull(s.getUnitValue())){
                    haveDataList.add(s);
                }else {
                    noDataList.add(s);
                }
            });
        }

        //创建返回数据集合
        List<EpProductUnitConsumeStandardVO> voList = new ArrayList<>();
        if (CollUtil.isNotEmpty(haveDataList)){
            for (int i = 0; i < list.size(); i++) {
                if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME.equals(sort)){
                    names.add(list.get(i).getName());
                    map.put(list.get(i).getName(), list.get(i));
                }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE.equals(sort)){
                    names.add(list.get(i).getEnergyTypeName()+i);
                    map.put(list.get(i).getEnergyTypeName()+i,list.get(i));
                }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE.equals(sort)){
                    names.add(list.get(i).getStandardTypeName()+i);
                    map.put(list.get(i).getStandardTypeName()+i,list.get(i));
                }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE.equals(sort)){
                    names.add(list.get(i).getProductTypeName()+i);
                    map.put(list.get(i).getProductTypeName()+i,list.get(i));
                }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS.equals(sort)){
                    names.add(list.get(i).getProductThickName()+i);
                    map.put(list.get(i).getProductThickName()+i,list.get(i));
                }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD.equals(sort)){
                    names.add(list.get(i).getUnitValue().toString()+i);
                    map.put(list.get(i).getProductThickName().toString()+i,list.get(i));
                }
            }
            if (isOrder){
                Collections.sort(names, new Comparator<String>() {
                    Collator collator = Collator.getInstance(Locale.CHINA);

                    @Override
                    public int compare(String o1, String o2) {
                        CollationKey key1 = collator.getCollationKey(o1);
                        CollationKey key2 = collator.getCollationKey(o2);
                        return key1.compareTo(key2);
                    }
                });
            }else {
                Collections.sort(names, new Comparator<String>() {
                    Collator collator = Collator.getInstance(Locale.CHINA);

                    @Override
                    public int compare(String o1, String o2) {
                        CollationKey key1 = collator.getCollationKey(o1);
                        CollationKey key2 = collator.getCollationKey(o2);
                        return key2.compareTo(key1);
                    }
                });
            }
            for (String name : names) {
                if (map.containsKey(name)) {
                    voList.add(map.get(name));
                }
            }
        }

      //封装该字段为空情况的集合
        if (CollUtil.isNotEmpty(noDataList)){
            noDataList.forEach(s->{
                voList.add(s);
            });
        }
        return voList;
    }
}




