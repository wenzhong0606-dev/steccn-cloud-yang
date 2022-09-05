package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.mapper.QtItemCategoryMapper;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtItemCategorySaveDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.steccn.qms.vo.QtItemCategoryVO;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qt_item_category(类型表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
public class QtItemCategoryServiceImpl extends ServiceImpl<QtItemCategoryMapper, QtItemCategory>
        implements QtItemCategoryService {

    @Override
    public List<QtItemCategoryVO> getCategoryList(Integer type) {
        //1.定义返回集合
        List<QtItemCategoryVO> resList = Lists.newArrayList();
        //2.从数据库获取数据
        List<QtItemCategory> dbList = list(new LambdaQueryWrapper<QtItemCategory>().eq(QtItemCategory::getType, type));
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
}




