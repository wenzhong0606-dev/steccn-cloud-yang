package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.qms.domain.QtCustomerComplain;
import com.steccn.qms.domain.QtDealWay;
import com.steccn.qms.domain.QtDefectProduct;
import com.steccn.qms.dto.QtDealWayDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.service.QtDealWayService;
import com.steccn.qms.mapper.QtDealWayMapper;
import com.steccn.qms.service.QtDefectProductService;
import com.steccn.qms.vo.QtDealWayVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qms_deal_way(处理方式表)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
public class QtDealWayServiceImpl extends ServiceImpl<QtDealWayMapper, QtDealWay>
        implements QtDealWayService {

    @Autowired
    @Lazy
    private QtCustomerComplainService qtCustomerComplainService;
    @Autowired
    @Lazy
    private QtDefectProductService qtDefectProductService;

    @Override
    public List<QtDealWayVO> wayList(Integer type) {
        List<QtDealWayVO> resList = null;
        List<QtDealWay> dbList = list(new LambdaQueryWrapper<QtDealWay>()
                .eq(QtDealWay::getType, type)
                .orderByAsc(QtDealWay::getCreateTime));
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, QtDealWayVO.class);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    public Boolean saveWay(QtDealWayDTO dto) {
        //处理方式类型不存在
        Set<Integer> typeSet = Sets.newHashSet(0, 1);
        Assert.isTrue(typeSet.contains(dto.getType()), "处理方式类型不存在");
        //编辑是否存在id
        if (Objects.nonNull(dto.getId())) {
            QtDealWay oldModel = getById(dto.getId());
            Assert.notNull(oldModel, "编辑的处理方式不存在");
        }
        //判断是否存在
        long count = count(new LambdaQueryWrapper<QtDealWay>()
                .eq(QtDealWay::getType, dto.getType())
                .eq(QtDealWay::getWay, dto.getWay())
                .ne(Objects.nonNull(dto.getId()), QtDealWay::getId, dto.getId()));
        Assert.isTrue(count == 0, "处理方式名称不可重复");
        //定义新对象
        QtDealWay model = new QtDealWay();
        BeanUtils.copyProperties(dto, model);
        return saveOrUpdate(model);
    }

    @Override
    public Boolean delete(Integer id) {
        QtDealWay oldModel = getById(id);
        Assert.notNull(oldModel, "处理方式不存在");
        //客诉
        if (oldModel.getType() == 0) {
            long count = qtCustomerComplainService.count(new LambdaQueryWrapper<QtCustomerComplain>()
                    .eq(QtCustomerComplain::getDealWay, id));
            Assert.isTrue(count == 0, "存在该处理方式的客诉，不允许删除");
        } else {
            long count = qtDefectProductService.count(new LambdaQueryWrapper<QtDefectProduct>()
                    .eq(QtDefectProduct::getDealWay, id));
            Assert.isTrue(count == 0, "存在该处理方式的不合格品处理记录，不允许删除");
        }
        return removeById(id);
    }

    @Override
    public Map<Integer, String> listByIdCh(Set<Integer> ids) {
        Map<Integer, String> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<QtDealWay> list = list(new LambdaQueryWrapper<QtDealWay>().in(QtDealWay::getId, ids));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(qtDealWay -> map.put(qtDealWay.getId(), qtDealWay.getWay()));
            }

        }
        return map;
    }
}




