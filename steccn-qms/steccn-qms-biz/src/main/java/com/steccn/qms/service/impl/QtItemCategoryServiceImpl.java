package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtItemCategorySaveDTO;
import com.steccn.qms.mapper.QtItemCategoryMapper;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.vo.QtItemCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qt_item_category(类型表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@Slf4j
public class QtItemCategoryServiceImpl extends ServiceImpl<QtItemCategoryMapper, QtItemCategory>
        implements QtItemCategoryService {

    @Autowired
    private QtCheckItemService qtCheckItemService;

    @Autowired
    private QtDefectiveItemService qtDefectiveItemService;

    @Override
    public List<QtItemCategoryVO> getCategoryList(Integer type) {
        //1.定义返回集合
        List<QtItemCategoryVO> resList = Lists.newArrayList();
        //2.从数据库获取数据
        List<QtItemCategory> dbList = list(new LambdaQueryWrapper<QtItemCategory>()
                .eq(QtItemCategory::getType, type)
                .orderByDesc(QtItemCategory::getCreateTime));
        //3.不为空，返回数据集合
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, QtItemCategoryVO.class);
        }
        return resList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveCategory(QtItemCategorySaveDTO dto) {
        //1.去数据库查询是否存在[编辑传id，新增不传id]
        int count = count(new LambdaQueryWrapper<QtItemCategory>()
                .ne(Objects.nonNull(dto.getId()), QtItemCategory::getId, dto.getId())
                .eq(QtItemCategory::getName, dto.getName())
                .eq(QtItemCategory::getType, dto.getType()));
        //2.如果存在则抛异常
        if (count > 0) {
            //获取枚举元素
            QtManagerConstant.QtItemCategoryEnum eleByType = QtManagerConstant.QtItemCategoryEnum.getEleByType(dto.getType());
            throw new BusinessException(eleByType.getName() + "类型名称不能重复");
        }
        //3.不存在，则插入数据库
        QtItemCategory model = new QtItemCategory();
        BeanUtils.copyProperties(dto, model);
        //名称需要去掉前后的空格
        model.setName(dto.getName().trim());
        if (Objects.isNull(dto.getId())) {
            model.setCreateBy(SecurityUtils.getUser().getUserId());
            model.setCreateName(SecurityUtils.getUser().getName());
        }
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = save(model);
        return save;
    }

    @Override
    public QtItemCategoryVO info(Integer id) {
        //1.定义返回vo
        QtItemCategoryVO resVo = null;
        //2.根据id查询数据库数据
        QtItemCategory model = getById(id);
        //3.不为空，则返回拷贝的数据
        if (Objects.nonNull(model)) {
            resVo = new QtItemCategoryVO();
            BeanUtils.copyProperties(model, resVo);
        }
        return resVo;
    }

    @Override
    public Boolean delete(Integer id) {
        //1.判断类别是否存在
        QtItemCategory model = getById(id);
        if (Objects.isNull(model)) {
            return Boolean.TRUE;
        }
        //2.判断类别类型为检验项还是缺陷项
        if (QtManagerConstant.QtItemCategoryEnum.CHECK.getType().equals(model.getType())) {
            int count = qtCheckItemService.count(new LambdaQueryWrapper<QtCheckItem>()
                    .eq(QtCheckItem::getCategoryId, id));
            if (count > 0) {
                throw new BusinessException("该类别包含至少1个检验项，不允许删除");
            }
        } else if (QtManagerConstant.QtItemCategoryEnum.DEFECTIVE.getType().equals(model.getType())) {
            int count = qtDefectiveItemService.count(new LambdaQueryWrapper<QtDefectiveItem>()
                    .eq(QtDefectiveItem::getCategoryId, id));
            if (count > 0) {
                throw new BusinessException("该类别包含至少1个缺陷项，不允许删除");
            }
        } else {
            throw new BusinessException("该类别类型不存在，无法删除");
        }
        Boolean delete = removeById(id);
        return delete;
    }
}




