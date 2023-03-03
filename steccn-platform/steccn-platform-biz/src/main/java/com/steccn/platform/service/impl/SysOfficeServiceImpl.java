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
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentIdsDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.vo.StationLineVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.domain.SysOffice;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.mapper.SysOfficeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author MongKnife
 * @description 针对表【sys_office(Mes组织机构表)】的数据库操作Service实现
 * @createDate 2022-09-02 16:25:59
 */
@Service

public class SysOfficeServiceImpl extends ServiceImpl<SysOfficeMapper, SysOffice> implements SysOfficeService {

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
        //获取所有数据
        List<SysOffice> list = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal));
        //如果数据为空,直接返回空集
        if (CollUtil.isEmpty(list)) {
            return Lists.newArrayList();
        }
        //递归返回一棵树
        return recursiveTree(list);
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
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getOfficeType, officeType).eq(SysOffice::getStatus, 0));
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
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).and(wrapper -> wrapper.eq(SysOffice::getOfficeType, dto.getOfficeType())).and(wrapper -> wrapper.eq(SysOffice::getParentCode, dto.getParentCode())));
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
        Collection<SysOffice> sysOfficeList = listByIds(idList);
        if (CollUtil.isNotEmpty(sysOfficeList)) {
            //对此接口进行功能增强,可以获取父节点的名称，id，名字
            Set<String> parentCodeSet = sysOfficeList.stream().map(SysOffice::getParentCode).collect(Collectors.toSet());
            Map<String, SysOffice> parentMap = Maps.newHashMap();
            SysOffice defaultSysOffice = new SysOffice();
            if (CollUtil.isNotEmpty(parentCodeSet)) {
                List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getOfficeCode, parentCodeSet));
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
        List<SysOffice> dbList = list(new LambdaQueryWrapper<SysOffice>().in(CollUtil.isNotEmpty(typeList), SysOffice::getOfficeType, typeList).eq(SysOffice::getStatus, SysOfficeConstant.normal));
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
        List<SysOffice> rootList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getOfficeType, officeType).select(SysOffice::getId, SysOffice::getOfficeCode));
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
            List<SysOffice> childrenList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getParentCode, parentCodeSet).select(SysOffice::getId, SysOffice::getOfficeCode));
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
        List<SysOffice> sysOfficeList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getOfficeName, names));
        if (CollUtil.isNotEmpty(sysOfficeList)) {
            //对此接口进行功能增强,可以获取父节点的名称，id，名字
            Set<String> parentCodeSet = sysOfficeList.stream().map(SysOffice::getParentCode).collect(Collectors.toSet());
            Map<String, SysOffice> parentMap = Maps.newHashMap();
            SysOffice defaultSysOffice = new SysOffice();
            if (CollUtil.isNotEmpty(parentCodeSet)) {
                List<SysOffice> parentList = list(new LambdaQueryWrapper<SysOffice>().in(SysOffice::getOfficeCode, parentCodeSet));
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
        SysOffice office = getOne(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getOfficeCode, sysOfficeDTO.getOfficeCode()));
        if (Objects.nonNull(office)) {
            removeById(office.getId());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateOffice(SysOfficeDTO sysOfficeDTO) {
        SysOffice sysOffice = new SysOffice();
        BeanUtil.copyProperties(sysOfficeDTO, sysOffice);
        return update(sysOffice, new LambdaUpdateWrapper<SysOffice>().eq(SysOffice::getOfficeCode, sysOffice.getOfficeCode()));
    }

    @Override
    public SysOfficeVO parentOffice(Integer childId) {
        SysOffice child = getById(childId);
        if (Objects.nonNull(child) && StrUtil.isNotBlank(child.getParentCode())) {
            SysOffice parent = getOne(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).eq(SysOffice::getOfficeCode, child.getParentCode()));
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
        List<SysOffice> officeList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getStatus, SysOfficeConstant.normal).in(SysOffice::getOfficeType, dto.getType()).like(SysOffice::getOfficeName, dto.getKey()));
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
        return sysOffice == null ? "" : sysOffice.getOfficeCode();
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




