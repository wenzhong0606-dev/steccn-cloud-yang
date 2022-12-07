package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.mapper.EpProductUnitConsumeStandardMapper;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.ems.vo.EpPartitionVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.vo.QtSpcReportDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .eq(Objects.nonNull(dto.getProductThickness()), EpProductUnitConsumeStandard::getProductThick, dto.getProductThickness())
                //标准名称
                .like(Objects.nonNull(dto.getStandardName()), EpProductUnitConsumeStandard::getName, dto.getStandardName())
                //查询所有可展示的
                .eq(EpProductUnitConsumeStandard::getShowFlag, MpConstant.YES);

        //2.定义排序条件
        if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getName);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getEnergyType);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getProductType);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getStandardType);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getProductThick);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getUnitValue);
        }else if (EpManagerConstant.EP_ENERGY_TYPE_SORT_FIELD_INDATE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpProductUnitConsumeStandard::getValidStart);
        }else {
            condition.orderByDesc(EpProductUnitConsumeStandard::getCreateTime);
        }
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
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
        //排序
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
        //去数据库中根据名称查询产品单耗标准个数
        long size = count(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getName, dto.getName()));
        if (Objects.isNull(dto.getId())){
            Assert.isTrue(size ==0, "产品单耗标准的名称不能重复");
        }else {
            Assert.isTrue(size <=1, "产品单耗标准的名称不能重复");
        }

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
           List<EpProductUnitConsumeStandard> dataList= list(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                    .eq(EpProductUnitConsumeStandard::getStandardType, dto.getStandardType())
                    .eq(EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType())
                    .eq(EpProductUnitConsumeStandard::getProductType, dto.getProductType())
                    .eq(EpProductUnitConsumeStandard::getProductThick, dto.getProductThick())
                    //老的开始时间小于等于dto的有效期结束时间,表明时间有重叠
                    .lt(EpProductUnitConsumeStandard::getValidStart,dto.getValidEnd())
                    //老的结束时间小于等于dto的有效开始时间,表明时间有重叠
                    .gt(EpProductUnitConsumeStandard::getValidEnd,dto.getValidStart()));
           if (Objects.nonNull(dto.getId())){
               EpProductUnitConsumeStandard ep = getById(dto.getId());
               //编辑的时候移除对自身的判断
               dataList.remove(ep);
           }
            String names = new String();
            if (CollUtil.isNotEmpty(dataList)){
                for (EpProductUnitConsumeStandard data : dataList) {
                    names = data.getName()+" ";
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
        Assert.notNull(epStandard,"该产品单耗标准不存在,无法删除");
        epStandard.setShowFlag(MpConstant.NO);
        updateById(epStandard);
        return Boolean.TRUE;
    }
}




