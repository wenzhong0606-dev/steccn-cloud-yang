package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.MapUtil;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaDepartmentVO;
import com.steccn.oa.vo.OaJobTitleVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.*;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.service.SysConfigurationService;
import com.steccn.platform.vo.StationLineVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.domain.SysOffice;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.mapper.SysOfficeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author MongKnife
 * @description 针对表【sys_office(Mes组织机构表)】的数据库操作Service实现
 * @createDate 2022-09-02 16:25:59
 */
@Service
@RequiredArgsConstructor
public class SysOfficeServiceImpl extends ServiceImpl<SysOfficeMapper, SysOffice> implements SysOfficeService {
    private final OaInfoService oaInfoService;
    private final SysConfigurationService sysConfigurationService;


    @Override
    public List<SysOfficeVO> sysList(String officeType) {
        //根据类型查询所有的数据字典数据
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).eq(SysOffice::getOfficeType, officeType));
        //如果数据为空，直接返回空集合
        if (CollUtil.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        //QtManagerConstant.THE_CALCULATION_RESULTS返回一棵树
        return recursiveTree(dataList);
    }


    @Override
    public List<SysOfficeVO> treeData() {
        List<SysConfigurationDTO> mesOaDept = sysConfigurationService.infoByType("MES_OA_DEPT");
        //获取所有数据
        //机构类型
        List<String> officeTypeList = Lists.newArrayList();
        officeTypeList.add(SysOfficeConstant.PROVINCIAL_COMPANY);
        officeTypeList.add(SysOfficeConstant.MUNICIPAL_COMPANY);
        officeTypeList.add(SysOfficeConstant.LOGISTICS_DEPARTMENT);
        officeTypeList.add(SysOfficeConstant.PRODUCT_DEPARTMENT);
        officeTypeList.add(SysOfficeConstant.POST);
        officeTypeList.add(SysOfficeConstant.WORKSHOP);
        officeTypeList.add(SysOfficeConstant.PRODUCT_LINE);
        officeTypeList.add(SysOfficeConstant.SECTION);
        officeTypeList.add(SysOfficeConstant.STATION);
        officeTypeList.add(SysOfficeConstant.DEPARTMENT);
        officeTypeList.add(SysOfficeConstant.POST_GROUP);
        List<SysOffice> list = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .in(SysOffice::getOfficeType,officeTypeList));
        List<OaDepartmentVO> allDepartment = oaInfoService.getAllDepartment(new TupleDTO<>(LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.now())).getData();
        List<OaJobTitleVO> allJobTitle = oaInfoService.getAllJobTitle(new TupleDTO<>(LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.now())).getData();
        //如果数据为空,直接返回空集
        if (CollUtil.isEmpty(list)) {
            return Lists.newArrayList();
        }
        //倒入vo
        List<SysOfficeVO> vos = Lists.newArrayList();
        list.forEach(tmp->{
            SysOfficeVO vo = new SysOfficeVO();
            BeanUtil.copyProperties(tmp,vo);
            //塞入oa
            SysConfigurationDTO oaId = mesOaDept.stream().filter(v -> v.getContent().equals(tmp.getId().toString())).findFirst().orElse(null);
            if(Objects.nonNull(oaId)){
                vo.setOaId(Integer.parseInt(oaId.getRefContent()));
            }else if(!tmp.getOfficeType().equals(SysOfficeConstant.POST)){
                OaDepartmentVO oaDepartmentVO = allDepartment.stream().filter(v -> v.getDepartmentName().equals(vo.getOfficeName())).findFirst().orElse(null);
                if(Objects.nonNull(oaDepartmentVO)){
                    vo.setOaId(oaDepartmentVO.getId());
                }
            }else {
                OaJobTitleVO oaJobTitleVO = allJobTitle.stream().filter(v -> v.getJobTitleName().equals(vo.getOfficeName())).findFirst().orElse(null);
                if(Objects.nonNull(oaJobTitleVO)){
                    vo.setOaId(oaJobTitleVO.getId());
                }
            }
            vos.add(vo);
        });
        //构建部门Code与部门对象的映射关系
        Map<String, SysOfficeVO> departmentMap = Maps.newHashMap();
        for(SysOfficeVO vo:vos) {
            departmentMap.put(vo.getOfficeCode(), vo);
        }
        //构建树形结构
        List<SysOfficeVO> treeList = Lists.newArrayList();
        for(SysOfficeVO vo:vos){
            String parentCode = vo.getParentCode();
            if(parentCode == null || parentCode.equals(SysOfficeConstant.normal)){
                //根节点
                treeList.add(vo);
            }else {
                SysOfficeVO parentVO = departmentMap.get(parentCode);
                if(parentVO!=null){
                    parentVO.addChild(vo);
                }
            }
        }
        //递归返回一棵树
        return treeList;
    }

    @Override
    public List<SysOfficeVO> data() {
        //获取所有数据
        List<SysOffice> list = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal));
        //如果数据为空,直接返回空集
        if (CollUtil.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<SysOfficeVO> officeVOList = BeanUtil.copyToList(list, SysOfficeVO.class);
        return officeVOList;
    }

    @Override
    public List<SysOfficeVO> flatData(String officeType) {
        //根据类型查询所有的数据字典数据
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getOfficeType, officeType).eq(SysOffice::getStatus, SysOfficeConstant.normal).orderByAsc(SysOffice::getTreeSort));
        List<SysOfficeVO> vos = Lists.newArrayList();
        if (!CollUtil.isEmpty(dataList)) {
            dataList.forEach(tmp -> {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, sysOfficeVO);
                vos.add(sysOfficeVO);
            });
        }
        return vos;
    }

    @Override
    public String findNameById(Integer id) {
        SysOffice sysOffice = getById(id);
        if (sysOffice != null) {
            return sysOffice.getOfficeName();
        } else {
            return "";
        }
    }

    @Override
    public List<SysOfficeVO> findDataByPCodeAndType(SysOfficeParentDTO dto) {
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .and(wrapper -> wrapper.eq(SysOffice::getOfficeType, dto.getOfficeType()))
                .and(wrapper -> wrapper.eq(SysOffice::getParentCode, dto.getParentCode()))
                .orderByAsc(SysOffice::getTreeSort));
        List<SysOfficeVO> vos = Lists.newArrayList();
        if (!CollUtil.isEmpty(dataList)) {
            dataList.forEach(tmp -> {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, sysOfficeVO);
                vos.add(sysOfficeVO);
            });
        }
        return vos;
    }

    @Override
    public Map<Integer, SysOfficeVO> listById(Set<Integer> idList) {
        Map<Integer, SysOfficeVO> resMap = Maps.newHashMap();
        List<SysOffice> sysOfficeList = list(new LambdaQueryWrapper<SysOffice>()
                .in(SysOffice::getId, idList)
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .orderByAsc(SysOffice::getTreeSort));

        if (CollUtil.isNotEmpty(sysOfficeList)) {
            //对此接口进行功能增强,可以获取父节点的名称，id，名字
            Set<String> parentCodeSet = sysOfficeList.stream().map(SysOffice::getParentCode).collect(Collectors.toSet());
            Map<String, SysOffice> parentMap = Maps.newHashMap();
            SysOffice defaultSysOffice = new SysOffice();
            if (CollUtil.isNotEmpty(parentCodeSet)) {
                List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>()
                        .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                        .in(SysOffice::getOfficeCode, parentCodeSet));
                if (CollUtil.isNotEmpty(parentList)) {
                    parentList.forEach(v -> parentMap.put(v.getOfficeCode(), v));
                }
            }
            for (SysOffice sysOffice : sysOfficeList) {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(sysOffice, sysOfficeVO);
                SysOffice orDefault = parentMap.getOrDefault(sysOffice.getParentCode(), defaultSysOffice);
                sysOfficeVO.setParentId(orDefault.getId());
                sysOfficeVO.setParentName(orDefault.getOfficeName());
                resMap.put(sysOfficeVO.getId(), sysOfficeVO);
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, String> listByCh(Set<Integer> idList) {
        Map<Integer, String> resMap = Maps.newHashMap();
        List<SysOffice> sysOfficeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getId, idList).select(SysOffice::getId, SysOffice::getOfficeName));
        if (CollUtil.isNotEmpty(sysOfficeList)) {
            for (SysOffice sysOffice : sysOfficeList) {
                resMap.put(sysOffice.getId(), sysOffice.getOfficeName());
            }
        }
        return resMap;
    }

    @Override
    public Map<String, SysOfficeVO> listByCodeSet(Set<String> codeSet) {
        Map<String, SysOfficeVO> resMap = Maps.newHashMap();
        List<SysOffice> sysOfficeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getOfficeCode, codeSet));
        if (CollUtil.isNotEmpty(sysOfficeList)) {
            for (SysOffice sysOffice : sysOfficeList) {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(sysOffice, sysOfficeVO);
                resMap.put(sysOffice.getOfficeCode(), sysOfficeVO);
            }
        }
        return resMap;
    }

    @Override
    public List<SysOfficeVO> warehouseAndShelf(List<String> typeList) {
        //根据前端传过来的机构类型获取组织机构列表
        List<SysOffice> dbList = list(new LambdaQueryWrapper<SysOffice>().in(CollUtil.isNotEmpty(typeList), SysOffice::getOfficeType, typeList).eq(SysOffice::getStatus, SysOfficeConstant.normal).orderByAsc(SysOffice::getTreeSort));
        List<SysOfficeVO> rootVOList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dbList)) {

            //复制查询结果到新的集合
            List<SysOfficeVO> sysOfficeVOS = BeanUtil.copyToList(dbList, SysOfficeVO.class);
            //根据一堆元素 ，自己去查询根节点
            rootVOList = searchRoot(sysOfficeVOS);
            //从上往下递归查询
            for (SysOfficeVO root : rootVOList) {
                List<SysOfficeVO> children = getChildren(sysOfficeVOS, root.getOfficeCode(), root.getId(), root.getOfficeName());
                root.setChildren(children);
            }
        }
        return rootVOList;
    }

    @Override
    public List<SysOfficeVO> workShopLineStation(List<String> typeList) {
        //根据前端传过来的机构类型获取组织机构列表
        List<SysOffice> dbList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getOfficeType, typeList).eq(SysOffice::getStatus, SysOfficeConstant.normal).orderByAsc(SysOffice::getTreeSort));
        List<SysOfficeVO> rootVOList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dbList)) {
            //复制查询结果到新的集合
            List<SysOfficeVO> sysOfficeVOS = BeanUtil.copyToList(dbList, SysOfficeVO.class);
            //根据一堆元素 ，自己去查询根节点
            rootVOList = searchRoot(sysOfficeVOS);
            //维护车间产线工位的父子关系
            Map<String, String> relationMap = Maps.newHashMap();
            relationMap.put(SysOfficeConstant.PRODUCT_LINE, SysOfficeConstant.WORKSHOP);
            relationMap.put(SysOfficeConstant.STATION, SysOfficeConstant.PRODUCT_LINE);

            rootVOList = rootVOList.stream().filter(v -> SysOfficeConstant.WORKSHOP.equals(v.getOfficeType())).collect(Collectors.toList());
            //从上往下递归查询
            for (SysOfficeVO root : rootVOList) {
                //根节点是车间
                List<SysOfficeVO> children = getChildrenStrict(relationMap, sysOfficeVOS, root);
                root.setChildren(children);
            }
        }
        return rootVOList;
    }

    /**
     * 根据一堆元素 ，自己去查询根节点
     *
     * @param dbList 元素集合
     * @return
     */
    private List<SysOfficeVO> searchRoot(List<SysOfficeVO> dbList) {
        List<SysOfficeVO> rootList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dbList)) {
            for (SysOfficeVO sysOffice : dbList) {
                //当前节点的父节点不存于数据集合，当前节点便是根节点
                long count = dbList.stream().filter(v -> v.getOfficeCode().equals(sysOffice.getParentCode())).count();
                if (count == 0) {
                    rootList.add(sysOffice);
                }
            }
        }
        return rootList;
    }

    /**
     * 获取子节点
     */
    /**
     * @param childrenList
     * @param parentCode
     * @return
     */
    public List<SysOfficeVO> getChildren(List<SysOfficeVO> childrenList, String parentCode, Integer id, String name) {
        ArrayList<SysOfficeVO> result = new ArrayList<>();
        //遍历集合设置子节点
        for (SysOfficeVO sysOfficeVO : childrenList) {
            if (sysOfficeVO.getParentCode().equals(parentCode)) {
                sysOfficeVO.setParentId(id);
                sysOfficeVO.setParentName(name);
                //递归方法找到子节点
                List<SysOfficeVO> children = getChildren(childrenList, sysOfficeVO.getOfficeCode(), sysOfficeVO.getId(), sysOfficeVO.getOfficeName());
                sysOfficeVO.setChildren(children);
                result.add(sysOfficeVO);
            }
        }
        return result;
    }

    /**
     * 获取子节点
     */
    /**
     * @param childrenList
     * @param parent       父节点
     * @return
     */
    public List<SysOfficeVO> getChildrenStrict(Map<String, String> relationMap, List<SysOfficeVO> childrenList, SysOfficeVO parent) {
        ArrayList<SysOfficeVO> result = new ArrayList<>();
        //遍历集合设置子节点
        for (SysOfficeVO sysOfficeVO : childrenList) {
            if (sysOfficeVO.getParentCode().equals(parent.getOfficeCode()) && parent.getOfficeType().equals(relationMap.get(sysOfficeVO.getOfficeType()))) {
                sysOfficeVO.setParentId(parent.getId());
                sysOfficeVO.setParentName(parent.getOfficeName());
                //递归方法找到子节点
                List<SysOfficeVO> children = getChildren(childrenList, sysOfficeVO.getOfficeCode(), sysOfficeVO.getId(), sysOfficeVO.getOfficeName());
                sysOfficeVO.setChildren(children);
                result.add(sysOfficeVO);
            }
        }
        return result;
    }

    @Override
    public SysOfficeVO shelfWarehouse(Integer id) {
        SysOffice shelf = getById(id);
        SysOffice sysOffice = parentSysOffice(shelf);
        if (Objects.nonNull(sysOffice)) {
            return BeanUtil.copyProperties(sysOffice, SysOfficeVO.class);
        }
        return null;
    }

    @Override
    public SysOfficeVO shelfWarehouseInfo(Integer id) {
        SysOffice shelfWarehouseInfo = getById(id);
        return BeanUtil.copyProperties(shelfWarehouseInfo, SysOfficeVO.class);
    }

    @Override
    public Set<Integer> typeAndChildrenFlat(String officeType) {
        Set<Integer> resSet = Sets.newHashSet();
        List<SysOffice> rootList = list(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getOfficeType, officeType)
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .select(SysOffice::getId, SysOffice::getOfficeCode));
        if (CollUtil.isNotEmpty(rootList)) {
            resSet.addAll(rootList.stream().map(SysOffice::getId).collect(Collectors.toSet()));
            childrenCycle(resSet, rootList);
        }
        return resSet;
    }

    /**
     * 孩子递归
     *
     * @param dataSet    数据集合
     * @param parentList 父节点
     */
    private void childrenCycle(Set<Integer> dataSet, List<SysOffice> parentList) {
        if (CollUtil.isNotEmpty(parentList)) {
            Set<String> parentCodeSet = parentList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toSet());
            List<SysOffice> childrenList = list(new LambdaQueryWrapper<SysOffice>()
                    .in(SysOffice::getParentCode, parentCodeSet)
                    .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                    .select(SysOffice::getId, SysOffice::getOfficeCode));
            if (CollUtil.isNotEmpty(childrenList)) {
                dataSet.addAll(childrenList.stream().map(SysOffice::getId).collect(Collectors.toSet()));
                childrenCycle(dataSet, childrenList);
            }
        }
    }


    @Override
    public List<SysOfficeVO> listMultiFilter(SysOfficeTypeKeyDTO dto) {
        List<SysOfficeVO> vos = new ArrayList<>();
        LambdaQueryWrapper<SysOffice> lqw = new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getOfficeType, dto.getType()).like(StrUtil.isNotBlank(dto.getKey()), SysOffice::getOfficeName, dto.getKey());
        if (CollUtil.isNotEmpty(dto.getKeyList())) {
            lqw.and(qr -> {
                for (int i = 0; i < dto.getKeyList().size(); i++) {
                    if (i != 0) {
                        qr.or();
                    }
                    qr.like(SysOffice::getOfficeName, dto.getKeyList().get(i));
                }
            });
        }
        List<SysOffice> officeList = list(lqw);
        if (CollUtil.isNotEmpty(officeList)) {
            officeList.forEach(tmp -> {
                SysOfficeVO vo = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, vo);
                vos.add(vo);
            });
        }
        return vos;
    }

    @Override
    public Map<String, SysOfficeVO> listByName(Set<String> names) {
        Map<String, SysOfficeVO> resMap = Maps.newHashMap();
        List<SysOffice> sysOfficeList = list(new LambdaQueryWrapper<SysOffice>()
                .in(SysOffice::getOfficeName, names)
                .eq(SysOffice::getStatus, SysOfficeConstant.normal));
        if (CollUtil.isNotEmpty(sysOfficeList)) {
            //对此接口进行功能增强,可以获取父节点的名称，id，名字
            Set<String> parentCodeSet = sysOfficeList.stream().map(SysOffice::getParentCode).collect(Collectors.toSet());
            Map<String, SysOffice> parentMap = Maps.newHashMap();
            SysOffice defaultSysOffice = new SysOffice();
            if (CollUtil.isNotEmpty(parentCodeSet)) {
                List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>()
                        .in(SysOffice::getOfficeCode, parentCodeSet)
                        .eq(SysOffice::getStatus, SysOfficeConstant.normal));
                if (CollUtil.isNotEmpty(parentList)) {
                    parentList.forEach(v -> parentMap.put(v.getOfficeCode(), v));
                }
            }
            for (SysOffice sysOffice : sysOfficeList) {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(sysOffice, sysOfficeVO);
                SysOffice orDefault = parentMap.getOrDefault(sysOffice.getParentCode(), defaultSysOffice);
                sysOfficeVO.setParentId(orDefault.getId());
                sysOfficeVO.setParentName(orDefault.getOfficeName());
                resMap.put(sysOfficeVO.getOfficeName(), sysOfficeVO);
            }
        }
        return resMap;
    }

    @Override
    public Boolean saveOffice(SysOfficeDTO sysOfficeDTO) {
        SysOffice sysOffice = new SysOffice();
        BeanUtil.copyProperties(sysOfficeDTO, sysOffice);
        return save(sysOffice);
    }

    @Override
    public Boolean delete(SysOfficeDTO sysOfficeDTO) {
        //根据机构编码查询
        SysOffice office = getOne(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getOfficeCode, sysOfficeDTO.getOfficeCode())
                .eq(SysOffice::getOfficeType, sysOfficeDTO.getOfficeType()));
        if (Objects.nonNull(office)) {
            office.setStatus(SysOfficeConstant.DEL_FLAG);
            return updateById(office);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean updateOffice(SysOfficeDTO sysOfficeDTO) {
        SysOffice sysOffice = new SysOffice();
        BeanUtil.copyProperties(sysOfficeDTO, sysOffice);
        return update(sysOffice, new LambdaUpdateWrapper<SysOffice>().eq(SysOffice::getOfficeCode, sysOffice.getOfficeCode()));
    }

    @Override
    public Map<Integer, Integer> parentType(Set<Integer> officeIdSet, String type) {
        //定义返回集合
        Map<Integer, Integer> resMap = Maps.newHashMap();
        //如果组织机构id为空，则直接返回空map
        if (CollUtil.isNotEmpty(officeIdSet)) {
            //查询出所有的组织机构数据
            List<SysOffice> dbList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal));
            //定义组织机构的idMap
            Map<Integer, SysOffice> idMap = Maps.newHashMap();
            //定义组织机构的codeMap
            Map<String, SysOffice> codeMap = Maps.newHashMap();
            //往idMap和codeMap里面插入数据
            dbList.forEach(v -> {
                idMap.put(v.getId(), v);
                codeMap.put(v.getOfficeCode(), v);
            });
            //逐个遍历，查询每个officeId产线父节点
            for (Integer officeId : officeIdSet) {
                //获取当前officeId的节点数据
                SysOffice sysOffice = idMap.get(officeId);
                //如果节点数据不为空
                if (Objects.nonNull(sysOffice)) {
                    Integer parentLineId = getParentType(type, sysOffice, codeMap);
                    resMap.put(officeId, parentLineId);
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, List<Integer>> childType(Set<Integer> officeIdSet, String type) {
        //定义返回集合
        Map<Integer, List<Integer>> resMap = Maps.newHashMap();
        //如果组织机构id为空，则直接返回空map
        if (CollUtil.isNotEmpty(officeIdSet)) {
            //查询出所有的组织机构数据
            List<SysOffice> dbList = list(new LambdaQueryWrapper<SysOffice>()
                    .notIn(SysOffice::getOfficeType, Lists.newArrayList(SysOfficeConstant.SUPPLIER, SysOfficeConstant.CUSTOMER))
                    .eq(SysOffice::getStatus, SysOfficeConstant.normal));
            //定义组织机构的idMap
            Map<Integer, SysOffice> idMap = Maps.newHashMap();
            //定义组织机构的codeMap
            Map<String, SysOffice> codeMap = Maps.newHashMap();
            Map<String, Set<String>> directChildMap = Maps.newHashMap();
            dbList.forEach(ele -> {
                MapUtil.computeSet(directChildMap, ele.getParentCode(), ele.getOfficeCode());
                //往idMap和codeMap里面插入数据
                codeMap.put(ele.getOfficeCode(), ele);
                idMap.put(ele.getId(), ele);
            });

            //逐个遍历，查询每个officeId产线父节点
            for (Integer officeId : officeIdSet) {
                //获取当前officeId的节点数据
                SysOffice sysOffice = idMap.get(officeId);
                //如果节点数据不为空
                Set<String> allChildSet = getAllChild(sysOffice, directChildMap, codeMap);
                if (CollUtil.isNotEmpty(allChildSet)) {
                    allChildSet.forEach(ele -> {
                        SysOffice childOffice = codeMap.get(ele);
                        if (Objects.nonNull(childOffice) && type.equals(childOffice.getOfficeType())) {
                            MapUtil.compute(resMap, sysOffice.getId(), childOffice.getId());
                        }
                    });
                }
            }
        }
        return resMap;
    }


    /**
     * 获取节点的所有孩子
     *
     * @param sysOffice      当前节点
     * @param directChildMap 直接孩子
     * @param codeMap        所有节点的数据
     * @return list
     */
    private Set<String> getAllChild(SysOffice sysOffice, Map<String, Set<String>> directChildMap, Map<String, SysOffice> codeMap) {
        Set<String> resCodeSet = Sets.newHashSet();
        //如果是产线节点，则直接返回
        if (Objects.nonNull(sysOffice)) {
            Set<String> childCodeSet = directChildMap.get(sysOffice.getOfficeCode());
            if (CollUtil.isNotEmpty(childCodeSet)) {
                resCodeSet.addAll(childCodeSet);
                childCodeSet.forEach(ele -> {
                    Set<String> allChild = getAllChild(codeMap.get(ele), directChildMap, codeMap);
                    resCodeSet.addAll(allChild);
                });
            }
        }
        return resCodeSet;
    }


    /**
     * 获取产线父节点
     *
     * @param type    类型
     * @param current 当前节点
     * @param codeMap 所有节点的数据
     * @return list
     */
    private Integer getParentType(String type, SysOffice current, Map<String, SysOffice> codeMap) {
        //如果是产线节点，则直接返回
        if (type.equals(current.getOfficeType())) {
            return current.getId();
        } else {
            //如果当前节点的父亲节点为空，则表示查询到底了，没有
            SysOffice parent = codeMap.get(current.getParentCode());
            if (Objects.nonNull(parent)) {
                return getParentType(type, parent, codeMap);
            }
        }
        return null;
    }

    @Override
    public List<SysOfficeVO> getCustomer(String name) {
        List<SysOfficeVO> resultList = Lists.newArrayList();
        List<SysOffice> list = list(new LambdaUpdateWrapper<SysOffice>().eq(SysOffice::getOfficeType, SysOfficeConstant.CUSTOMER).like(StrUtil.isNotBlank(name), SysOffice::getOfficeName, name).last("LIMIT 100"));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(tmp -> {
                SysOfficeVO vo = new SysOfficeVO();
                BeanUtil.copyProperties(tmp, vo);
                resultList.add(vo);
            });
        }
        return resultList;
    }

    @Override
    public Map<Integer, List<Integer>> childWorkshop(List<Integer> officeIdSet) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        if (CollUtil.isNotEmpty(officeIdSet)) {
            List<SysOffice> list = list(new LambdaQueryWrapper<SysOffice>()
                    .in(SysOffice::getId, officeIdSet)
                    .eq(SysOffice::getStatus, SysOfficeConstant.normal));
            if (CollUtil.isNotEmpty(list)) {
                List<String> codes = list.stream().map(SysOffice::getOfficeCode).collect(Collectors.toList());
                Map<String, List<SysOffice>> collect = list(new LambdaQueryWrapper<SysOffice>()
                        .in(SysOffice::getParentCode, codes)
                        .eq(SysOffice::getStatus, SysOfficeConstant.normal))
                        .stream().collect(Collectors.groupingBy(SysOffice::getParentCode));
                for (SysOffice sysOffice : list) {
                    List<SysOffice> childList = collect.get(sysOffice.getOfficeCode());
                    if (CollUtil.isNotEmpty(childList)) {
                        List<Integer> ids = childList.stream().map(SysOffice::getId).collect(Collectors.toList());
                        map.put(sysOffice.getId(), ids);
                    }
                }
            }
        }
        return map;
    }

    @Override
    public List<SysOfficeVO> getWorkShopChildren(Integer type) {
        //0表示基片
        String workshop = "";
        if(type.equals(0)){
            workshop = "基片";
        }else {
            workshop = "深加工";
        }
        List<SysOffice> workShopList = list(new LambdaQueryWrapper<SysOffice>()
                .select(SysOffice::getOfficeName,SysOffice::getId,SysOffice::getOfficeCode)
                .eq(SysOffice::getOfficeType, SysOfficeConstant.WORKSHOP)
                 //状态（0正常 1删除 2停用）
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .like(SysOffice::getOfficeName, workshop));
        List<String> parentCodes = workShopList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toList());
        List<SysOffice> productList = list(new LambdaQueryWrapper<SysOffice>()
                .select(SysOffice::getOfficeName, SysOffice::getId, SysOffice::getParentCode, SysOffice::getTreeSort)
                .eq(SysOffice::getOfficeType, SysOfficeConstant.PRODUCT_LINE)
                //状态（0正常 1删除 2停用）
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .in(SysOffice::getParentCode,parentCodes));
        Map<String, List<SysOffice>> productLineCollect = productList.stream().collect(Collectors.groupingBy(SysOffice::getParentCode));
        List<SysOfficeVO> result = Lists.newArrayList();
        workShopList.forEach(tmp->{
            SysOfficeVO vo = new SysOfficeVO();
            vo.setId(tmp.getId());
            vo.setOfficeName(tmp.getOfficeName());
            List<SysOfficeVO> children = Lists.newArrayList();
            productLineCollect.get(tmp.getOfficeCode()).stream()
                    .sorted(Comparator.comparing(SysOffice::getTreeSort))
                    .forEach(childrenTmp -> {
                SysOfficeVO childrenVo = new SysOfficeVO();
                childrenVo.setId(childrenTmp.getId());
                childrenVo.setOfficeName(childrenTmp.getOfficeName());
                children.add(childrenVo);
            });
            vo.setChildren(children);
            result.add(vo);
        });
        return result;
    }

    @Override
    public List<SysOfficeVO> listByParentIdAndType(SysOfficeParentDTO dto) {
        List<SysOfficeVO> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(dto.getParentIds())) {
            List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getId, dto.getParentIds()));
            Set<String> collect = officeList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(collect)){
                List<SysOffice> offices = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getParentCode, collect).eq(SysOffice::getOfficeType, dto.getOfficeType()));
                list = BeanUtil.copyToList(offices, SysOfficeVO.class);
            }
        }
        return list;
    }

    @Override
    public List<SysOfficeVO> listByParentIds(Set<Long> ids) {
        Assert.notEmpty(ids,"id不能为空");
        List<SysOfficeVO> results = Lists.newArrayList();
        List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getId, ids));
        if(CollUtil.isNotEmpty(parentList)){
            Set<String> officeCodeSets = parentList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toSet());
            List<SysOffice> childrenList = list(new LambdaQueryWrapper<SysOffice>()
                    .in(SysOffice::getParentCode, officeCodeSets)
                    .eq(SysOffice::getOfficeType,9)
                    .eq(SysOffice::getStatus, OaConstant.NO)
                    .orderByAsc(SysOffice::getTreeSort));
            if(CollUtil.isNotEmpty(childrenList)){
                results = BeanUtil.copyToList(childrenList, SysOfficeVO.class);
            }
        }
        return results;
    }

    @Override
    public List<SysOfficeVO> multiTree(SysOfficeMultiTreeDTO dto) {
        List<SysOfficeVO> sysOfficeVOList = typeDataList(dto.getOfficeTypeList());
        List<SysOfficeVO> collect = sysOfficeVOList.stream().filter(ele -> dto.getRootCodeList().contains(ele.getOfficeCode())).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SysOfficeVO> listWorkshop(Integer type) {
        List<SysOfficeVO> resList = Lists.newArrayList();
        //0表示基片
        String workshop = "";
        if(type.equals(0)){
            workshop = "基片";
        }else {
            workshop = "深加工";
        }
        List<SysOffice> workShopList = list(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getOfficeType, SysOfficeConstant.WORKSHOP)
                //状态（0正常 1删除 2停用）
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .like(SysOffice::getOfficeName, workshop));
        List<String> parentCodes = workShopList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toList());
        List<SysOffice> productList = list(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getOfficeType, SysOfficeConstant.PRODUCT_LINE)
                //状态（0正常 1删除 2停用）
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .in(SysOffice::getParentCode,parentCodes));
        List<String> stationParentCodes = productList.stream().map(SysOffice::getOfficeCode).collect(Collectors.toList());
        //工位
        List<SysOffice> stationList = list(new LambdaQueryWrapper<SysOffice>()
                .eq(SysOffice::getOfficeType, SysOfficeConstant.STATION)
                .eq(SysOffice::getStatus, SysOfficeConstant.normal)
                .in(SysOffice::getParentCode, stationParentCodes));
      resList.addAll(BeanUtil.copyToList(workShopList,SysOfficeVO.class));
      resList.addAll(BeanUtil.copyToList(productList,SysOfficeVO.class));
      resList.addAll(BeanUtil.copyToList(stationList,SysOfficeVO.class));
      return resList;
    }

    @Override
    public Map<Integer, SysOfficeVO> depMapOrPostMap(Set<String> codes) {
        Map<Integer, SysOfficeVO> resultMap = Maps.newHashMap();
        List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>()
                .in(SysOffice::getOfficeCode,codes));
        if(CollUtil.isNotEmpty(officeList)){
            for(SysOffice tmp:officeList){
                if(tmp.getOfficeType().equals("6")){
                    //如果是岗位则查询机构
                    String parentCodes = tmp.getParentCodes();
                    parentCodes = parentCodes.substring(0,parentCodes.length()-1);
                    List<String> parentCodeList = Arrays.asList(parentCodes.split(","));
                    List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>()
                            .select(SysOffice::getId,SysOffice::getOfficeName,SysOffice::getOfficeType)
                            .in(SysOffice::getOfficeCode, parentCodeList));
                    for(SysOffice parent:parentList){
                        if(parent.getOfficeType().equals("15")){
                            SysOfficeVO officeVO = new SysOfficeVO();
                            BeanUtil.copyProperties(parent,officeVO);
                            resultMap.put(tmp.getId(),officeVO);
                            break;
                        }
                    }
                }else if(tmp.getOfficeType().equals("15")){
                    //如果是机构则查询岗位

                }
            }

        }
        return null;
    }


    /**
     * 根据组织机构类型获取数据
     * @param typeList 类型集合
     * @return
     */
    private List<SysOfficeVO> typeDataList(List<String> typeList) {
        if (typeList.size() == 3 && typeList.contains(SysOfficeConstant.WORKSHOP) && typeList.contains(SysOfficeConstant.PRODUCT_LINE) && typeList.contains(SysOfficeConstant.STATION)) {
            return workShopLineStation(Lists.newArrayList(SysOfficeConstant.WORKSHOP, SysOfficeConstant.PRODUCT_LINE, SysOfficeConstant.STATION));
        } else if (typeList.size() == 2 && typeList.contains(SysOfficeConstant.WORKSHOP) && typeList.contains(SysOfficeConstant.PRODUCT_LINE)) {
            return workShopLineStation(Lists.newArrayList(SysOfficeConstant.WORKSHOP, SysOfficeConstant.PRODUCT_LINE));
        } else if (typeList.size() == 2 && typeList.contains(SysOfficeConstant.PRODUCT_LINE) && typeList.contains(SysOfficeConstant.STATION)) {
            return workShopLineStation(Lists.newArrayList(SysOfficeConstant.PRODUCT_LINE, SysOfficeConstant.STATION));
        } else {
            return warehouseAndShelf(typeList);
        }
    }

    @Override
    public SysOfficeVO parentOffice(Integer childId) {
        SysOffice child = getById(childId);
        if (Objects.nonNull(child) && StrUtil.isNotBlank(child.getParentCode())) {
            SysOffice parent = getOne(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).eq(SysOffice::getOfficeCode, child.getParentCode()), false);
            if (Objects.nonNull(parent)) {
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                BeanUtils.copyProperties(parent, sysOfficeVO);
                return sysOfficeVO;
            }
        }
        return null;
    }

    @Override
    public StationLineVO stationScan(Integer id) {
        SysOffice station = getById(id);
        Assert.notNull(station, "数据为空");
        StationLineVO resVo = new StationLineVO();
        resVo.setStationId(station.getId());
        resVo.setStationName(station.getOfficeName());
        Map<String, SysOfficeVO> sysOfficeVOMap = listByCodeSet(Sets.newHashSet(station.getParentCode()));
        if (Objects.nonNull(sysOfficeVOMap)) {
            SysOfficeVO line = sysOfficeVOMap.get(station.getParentCode());
            resVo.setProductLineId(line.getId());
            resVo.setProductLineName(line.getOfficeName());
        }
        return resVo;
    }

    @Override
    public List<SysOfficeVO> listByParentId(SysOfficeParentDTO dto) {
        //1.自定义sql查询数据
        List<SysOfficeVO> list = this.baseMapper.listByParentId(dto.getId(), dto.getOfficeType());
        if (CollUtil.isNotEmpty(list)) {
            return list;
        }
        return null;
    }

    @Override
    public List<SysOfficeParentVO> getListSysOfficeList(SysOfficeParentIdsDTO dto) {

        //1.自定义sql查询数据
        List<SysOfficeVO> list = baseMapper.listByParentIds(dto.getOfficeParentIds(), SysOfficeConstant.PRODUCT_LINE);

        List<SysOfficeParentVO> sysOfficeParentVOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            list.stream().forEach(s -> {
                SysOfficeParentVO sysOfficeParentVO = new SysOfficeParentVO();
                sysOfficeParentVO.setOfficeName(s.getOfficeName());
                sysOfficeParentVO.setId(s.getId());
                sysOfficeParentVOS.add(sysOfficeParentVO);
            });
            return sysOfficeParentVOS;
        }
        return null;
    }

    @Override
    public Map<Integer, SysOfficeVO> parentOfficeMap(List<Integer> childId) {
        Map<Integer, SysOfficeVO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(childId)) {
            List<SysOfficeVO> sysOfficeVOList = this.baseMapper.listByChildIds(new ArrayList<>(childId));
            if (CollUtil.isNotEmpty(sysOfficeVOList)) {
                for (SysOfficeVO sysOfficeVO : sysOfficeVOList) {
                    resMap.put(sysOfficeVO.getId(), sysOfficeVO);
                }
            }
        }
        return resMap;
    }

    /**
     * 根据office类型和名字查
     *
     * @param dto
     * @return
     */
    @Override
    public List<SysOfficeVO> findByTypeAndName(SysOfficeDTO dto) {
        List<SysOfficeVO> vos = new ArrayList<>();
        List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).eq(SysOffice::getOfficeType, dto.getOfficeType()).like(SysOffice::getOfficeName, dto.getOfficeName()));
        if (CollUtil.isNotEmpty(officeList)) {
            officeList.forEach(tmp -> {
                SysOfficeVO vo = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, vo);
                vos.add(vo);
            });
        }
        return vos;
    }

    /**
     * 根据机构类型以及关键字返回SysOfficeVO集合信息
     *
     * @param dto
     * @return
     */
    @Override
    public List<SysOfficeVO> getListByTypeAndKey(SysOfficeTypeKeyDTO dto) {
        List<SysOfficeVO> vos = new ArrayList<>();
        List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getOfficeType, dto.getType()).like(StrUtil.isNotBlank(dto.getKey()), SysOffice::getOfficeName, dto.getKey()));
        if (CollUtil.isNotEmpty(officeList)) {
            officeList.forEach(tmp -> {
                SysOfficeVO vo = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, vo);
                vos.add(vo);
            });
        }
        return vos;
    }

    /**
     * 根据机构类型以及关键字返回SysOfficeVO集合信息
     *
     * @param dto
     * @return
     */
    @Override
    public List<SysOfficeVO> getListByTypeAndNameList(SysOfficeTypeKeyDTO dto) {
        List<SysOfficeVO> vos = new ArrayList<>();
        List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getOfficeType, dto.getType()).in(CollUtil.isNotEmpty(dto.getKeyList()), SysOffice::getOfficeName, dto.getKeyList()));
        if (CollUtil.isNotEmpty(officeList)) {
            officeList.forEach(tmp -> {
                SysOfficeVO vo = new SysOfficeVO();
                BeanUtils.copyProperties(tmp, vo);
                vos.add(vo);
            });
        }
        return vos;
    }

    @Override
    public Map<Integer, SysOfficeVO> parentInfo(Set<Integer> childIdList) {

        Map<Integer, SysOfficeVO> resMap = Maps.newHashMap();

        List<SysOfficeVO> sysOfficeVOList = this.baseMapper.listParentInfo(childIdList);
        if (CollUtil.isNotEmpty(sysOfficeVOList)) {
            for (SysOfficeVO sysOfficeVO : sysOfficeVOList) {
                resMap.put(sysOfficeVO.getChildrenId(), sysOfficeVO);
            }
        }
        return resMap;

    }

    @Override
    public String getByIdSysOfficeInfo(Long id) {
        SysOffice sysOffice = getById(id);
        return sysOffice == null || !SysOfficeConstant.normal.equals(sysOffice.getStatus()) ? "" : sysOffice.getOfficeCode();
    }

    /**
     * 父节点
     *
     * @param sysOffice 结构信息
     * @return
     */
    private SysOffice parentSysOffice(SysOffice sysOffice) {
        //如果对象不为空，且对象的类型是货架，进入递归查询
        if (Objects.nonNull(sysOffice) && SysOfficeConstant.SHELF_TYPE.equals(sysOffice.getOfficeType())) {
            SysOffice model = getOne(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).eq(SysOffice::getOfficeCode, sysOffice.getParentCode()));
            return parentSysOffice(model);
        }
        //①对象为空 ②对象的类型不是货架，可能是仓库，也可能是别的
        if (Objects.isNull(sysOffice) || !SysOfficeConstant.WAREHOUSE_TYPE.equals(sysOffice.getOfficeType())) {
            return null;
        }
        return sysOffice;
    }


    /**
     * 根据list递归构造一颗树
     *
     * @return
     */
    private List<SysOfficeVO> recursiveTree(List<SysOffice> dataList) {
        //定义集合
        List<SysOfficeVO> resList = new ArrayList<>();
        //获取所有根节点-按照sortNum排序
        List<SysOffice> rootList = dataList.stream().filter(v -> Objects.isNull(v.getParentCode()) || v.getParentCode().equals("0")).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(rootList)) {
            rootList.sort(Comparator.comparing(SysOffice::getTreeSort));
            //把根结点从data中删除
            dataList.removeAll(rootList);
            //遍历根结点
            for (SysOffice root : rootList) {
                SysOfficeVO parent = new SysOfficeVO();
                BeanUtils.copyProperties(root, parent);
                resList.add(parent);
                //查询vo的子级，并将子级放到vo中
                searchSysDictCh(parent, dataList);
            }
        }
        return resList;
    }

    /**
     * 递归查询
     *
     * @param parentVo
     * @param dataList
     */
    private void searchSysDictCh(SysOfficeVO parentVo, List<SysOffice> dataList) {
        //获取当前结点的子级
        List<SysOffice> children = dataList.stream().filter(v -> Objects.nonNull(parentVo.getId()) && parentVo.getId().equals(v.getParentCode())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(children)) {
            children.sort(Comparator.comparing(SysOffice::getTreeSort));
            List<SysOfficeVO> childrenVoList = BeanUtil.copyToList(children, SysOfficeVO.class);
            //父亲结点挂孩子
            parentVo.setChildren(childrenVoList);
            //从集合中删除孩子
            dataList.removeAll(children);
            //孩子结点继续去挂自己的孩子
            for (SysOfficeVO item : childrenVoList) {
                searchSysDictCh(item, dataList);
            }
        }
    }

}




