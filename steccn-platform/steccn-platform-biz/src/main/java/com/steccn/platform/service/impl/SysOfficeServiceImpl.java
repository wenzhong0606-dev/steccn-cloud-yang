package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.setccn.platfrom.vo.SysOfficeVO;
import com.steccn.platform.domain.SysOffice;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.mapper.SysOfficeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author MongKnife
* @description 针对表【sys_office(Mes组织机构表)】的数据库操作Service实现
* @createDate 2022-09-02 16:25:59
*/
@Service

public class SysOfficeServiceImpl extends ServiceImpl<SysOfficeMapper, SysOffice>
    implements SysOfficeService{

    // TODO: 2022/9/6
    @Override
    public List<SysOfficeVO> sysList(String officeType) {
        //根据类型查询所有的数据字典数据
        List<SysOffice> dataList = list(new LambdaQueryWrapper<SysOffice>().eq(SysOffice::getOfficeType, officeType));
        //如果数据为空，直接返回空集合
        if (CollUtil.isEmpty(dataList)){
            return Lists.newArrayList();
        }
        //递归返回一棵树
        return recursiveTree(dataList);
    }


    @Override
    public List<SysOfficeVO> treeData() {
        //获取所有数据
        List<SysOffice> list = list();
        //如果数据为空,直接返回空集
        if (CollUtil.isEmpty(list)){
            return Lists.newArrayList();
        }
        //递归返回一棵树
        return recursiveTree(list);
    }

    @Override
    public List<SysOfficeVO> data() {
        //获取所有数据
        List<SysOffice> list = list();
        //如果数据为空,直接返回空集
        if (CollUtil.isEmpty(list)){
            return Lists.newArrayList();
        }
        List<SysOfficeVO> officeVOList = BeanUtil.copyToList(list, SysOfficeVO.class);
        return officeVOList;
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




