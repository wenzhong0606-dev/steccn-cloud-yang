package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.mapper.SysDictMapper;
import com.steccn.platform.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_dict(数据字典表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict>
        implements SysDictService {


    @Override
    public List<SysDictVO> sysList(String type) {
        //1.根据类型code查询出所有的数据字典数据
        List<SysDict> dataList = list(new LambdaQueryWrapper<SysDict>().eq(SysDict::getType, type));
        //2.如果数据为空，直接返回空集合
        if (CollUtil.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        //3.递归返回一棵树
        return recursiveTree(dataList);
    }

    @Override
    public List<SysDictVO> sysListFilter(String type, String name) {
        //1.根据类型code查询出所有的数据字典数据
        List<SysDict> dataList = list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, type)
                .like(StrUtil.isNotBlank(name), SysDict::getName, name));
        //2.如果数据为空，直接返回空集合
        if (CollUtil.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        //3.返回平铺结构
        dataList.sort(Comparator.comparing(SysDict::getSort));
        return BeanUtil.copyToList(dataList, SysDictVO.class);
    }

    @Override
    public Map<Long, SysDictVO> listByIdList(List<Long> idList) {
        Map<Long, SysDictVO> resMap = Maps.newHashMap();
        Collection<SysDict> sysDictList = listByIds(idList);
        if (CollUtil.isEmpty(sysDictList)) {
            return resMap;
        }
        List<SysDictVO> sysDictVOList = BeanUtil.copyToList(sysDictList, SysDictVO.class);
        for (SysDictVO sysDictVO : sysDictVOList) {
            resMap.put(sysDictVO.getId(), sysDictVO);
        }
        return resMap;
    }

    @Override
    public Boolean exist(String type, Long id) {
        SysDict model = getById(id);
        if (Objects.nonNull(model)) {
            return type.equals(model.getType());
        }
        return Boolean.FALSE;
    }

    @Override
    public SysDictVO getByCode(String code, String type) {
        SysDict sysDict = baseMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getCode, code).eq(SysDict::getType, type));
        if (Objects.isNull(sysDict)) {
            return null;
        }
        SysDictVO sysDictVO = new SysDictVO();
        BeanUtils.copyProperties(sysDict, sysDictVO);
        return sysDictVO;
    }

    @Override
    public Map<String, SysDictVO> listByCode(SysDictDTO sysDictDTO) {
        Map<String, SysDictVO> resMap = Maps.newHashMap();
        Collection<SysDict> sysDictList = baseMapper.listByCodes(sysDictDTO);
        if (CollUtil.isEmpty(sysDictList)) {
            return resMap;
        }
        List<SysDictVO> sysDictVOList = BeanUtil.copyToList(sysDictList, SysDictVO.class);
        for (SysDictVO sysDictVO : sysDictVOList) {
            resMap.put(sysDictVO.getCode(), sysDictVO);
        }
        return resMap;
    }

    @Override
    public Map<Long, String> listByIdCh(Set<Long> idList) {
        Map<Long, String> resMap = Maps.newHashMap();
        List<SysDict> sysDictList = list(new LambdaQueryWrapper<SysDict>().in(SysDict::getId, idList).select(SysDict::getId, SysDict::getName));
        if (CollUtil.isNotEmpty(sysDictList)) {
            List<SysDictVO> sysDictVOList = BeanUtil.copyToList(sysDictList, SysDictVO.class);
            for (SysDictVO sysDictVO : sysDictVOList) {
                resMap.put(sysDictVO.getId(), sysDictVO.getName());
            }
        }
        return resMap;
    }


    /**
     * 根据list递归构造一颗树
     *
     * @return
     */
    private List<SysDictVO> recursiveTree(List<SysDict> dataList) {
        //1.判断是否存在树结构
        long count = dataList.stream().filter(v -> v.getParentId() != 0).count();
        if (count == 0) {
            dataList.sort(Comparator.comparing(SysDict::getSort));
            return BeanUtil.copyToList(dataList, SysDictVO.class);
        }
        //定义集合
        List<SysDictVO> resList = new ArrayList<>();
        //获取所有根节点-按照sortNum排序
        List<SysDict> rootList = dataList.stream().filter(v -> Objects.isNull(v.getParentId()) || v.getParentId() == 0).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(rootList)) {
            rootList.sort(Comparator.comparing(SysDict::getSort));
            //把根结点从data中删除
            dataList.removeAll(rootList);
            //遍历根结点
            for (SysDict root : rootList) {
                SysDictVO parent = new SysDictVO();
                BeanUtils.copyProperties(root, parent);
                resList.add(parent);
                //查询vo的孩子，并将孩子放到vo中
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
    private void searchSysDictCh(SysDictVO parentVo, List<SysDict> dataList) {
        //获取当前结点的孩子
        List<SysDict> children = dataList.stream().filter(v -> Objects.nonNull(parentVo.getId()) && parentVo.getId().equals(v.getParentId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(children)) {
            children.sort(Comparator.comparing(SysDict::getSort));
            List<SysDictVO> childrenVoList = BeanUtil.copyToList(children, SysDictVO.class);
            //父亲结点挂孩子
            parentVo.setChildren(childrenVoList);
            //从集合中删除孩子
            dataList.removeAll(children);
            //孩子结点继续去挂自己的孩子
            for (SysDictVO item : childrenVoList) {
                searchSysDictCh(item, dataList);
            }
        }
    }
}




