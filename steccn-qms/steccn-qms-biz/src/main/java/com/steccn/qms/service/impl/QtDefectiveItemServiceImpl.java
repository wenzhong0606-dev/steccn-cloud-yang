package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.QtDefectiveItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author yequan
* @description 针对表【qt_defective_item(缺陷项表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
public class QtDefectiveItemServiceImpl extends ServiceImpl<QtDefectiveItemMapper, QtDefectiveItem>
    implements QtDefectiveItemService{

    @Override
    public Page<QtDefectiveItemVO> pageList(QtDefectiveItemPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtDefectiveItem> condition = new LambdaQueryWrapper<QtDefectiveItem>()
                .eq(Objects.nonNull(dto.getName()), QtDefectiveItem::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtDefectiveItem::getName);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtDefectiveItem::getStatus);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_UPDATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtDefectiveItem::getUpdateTime);
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtDefectiveItem::getCreateTime);
        } else {
            //默认按创建时间降序排列
            condition.orderBy(true, false, QtDefectiveItem::getCreateTime);
        }
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<QtDefectiveItemVO> resRecords = BeanUtil.copyToList(resPage.getRecords(), QtDefectiveItemVO.class);
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }
}




