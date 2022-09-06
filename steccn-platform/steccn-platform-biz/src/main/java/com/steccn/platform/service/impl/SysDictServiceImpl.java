package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.setccn.platfrom.vo.SysDictVO;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.mapper.SysDictMapper;
import com.steccn.platform.service.SysDictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_dict(数据字典表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
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




