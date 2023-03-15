package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.qms.domain.QtDealWay;
import com.steccn.qms.dto.QtDealWayDTO;
import com.steccn.qms.service.QtDealWayService;
import com.steccn.qms.mapper.QtDealWayMapper;
import com.steccn.qms.vo.QtDealWayVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qms_deal_way(处理方式表)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
public class QtDealWayServiceImpl extends ServiceImpl<QtDealWayMapper, QtDealWay>
        implements QtDealWayService {

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
        return null;
    }
}




