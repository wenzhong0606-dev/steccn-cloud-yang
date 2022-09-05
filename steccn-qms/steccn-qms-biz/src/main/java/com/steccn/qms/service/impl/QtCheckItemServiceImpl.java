package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.mapper.QtCheckItemMapper;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.vo.QtCheckItemPageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qt_check_item(检验项表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
public class QtCheckItemServiceImpl extends ServiceImpl<QtCheckItemMapper, QtCheckItem>
        implements QtCheckItemService {

    @Override
    public Page<QtCheckItemPageVO> pageList(QtCheckItemPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtCheckItem> condition = new LambdaQueryWrapper<QtCheckItem>()
                .eq(Objects.nonNull(dto.getName()), QtCheckItem::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckItem::getName);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckItem::getStatus);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_UPDATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckItem::getUpdateTime);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckItem::getCreateTime);
        } else {
            //默认按创建时间降序排列
            condition.orderBy(true, false, QtCheckItem::getCreateTime);
        }
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<QtCheckItemPageVO> resRecords = BeanUtil.copyToList(resPage.getRecords(), QtCheckItemPageVO.class);
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }
}




