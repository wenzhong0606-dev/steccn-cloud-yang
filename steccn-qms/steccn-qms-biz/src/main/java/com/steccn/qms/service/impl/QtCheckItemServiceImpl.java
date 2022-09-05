package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.mapper.QtCheckItemMapper;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtItemCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class QtCheckItemServiceImpl extends ServiceImpl<QtCheckItemMapper, QtCheckItem>
        implements QtCheckItemService {

    private final QtItemCategoryService qtItemCategoryService;

    @Override
    public Page<QtCheckItemListVO> pageList(QtCheckItemPageDTO dto) {
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
        List<QtCheckItemListVO> resRecords = BeanUtil.copyToList(resPage.getRecords(), QtCheckItemListVO.class);
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    public Boolean add(QtCheckItemSaveDTO dto) {
        //1.校验category_id是否存在
        QtItemCategoryVO categoryInfo = qtItemCategoryService.info(dto.getCategoryId());
        if (Objects.isNull(categoryInfo) || !QtManagerConstant.QtItemCategoryEnum.CHECK.getType().equals(categoryInfo.getType())) {
            throw new BusinessException("检验项类别不存在");
        }
        //同一类别下检验项名称不可重复，否则提交时提示“同一类别下检验项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), null)) {
            throw new BusinessException("同一类别下检验项名称不能重复");
        }
        //2.插入数据
        QtCheckItem model = new QtCheckItem();
        BeanUtils.copyProperties(dto, model);
        //3.获取最大的自增编码
        model.setCode(getNextCode("JYX"));
        //状态：0停用  1启用
        model.setStatus(1);
        //把name转成拼音，用于排序
        model.setNameEn(PinyinUtil.getPinyin(dto.getName(), ""));
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = save(model);
        return save;
    }

    @Override
    public QtCheckItemListVO info(Integer id) {
        QtCheckItemListVO resVo = null;
        QtCheckItem byId = getById(id);
        if (Objects.nonNull(byId)) {
            resVo = new QtCheckItemListVO();
            BeanUtils.copyProperties(byId, resVo);
        }
        return resVo;
    }

    @Override
    public Boolean edit(QtCheckItemSaveDTO dto) {
        //校验是否存在
        QtCheckItem oldModel = getById(dto.getId());
        if (Objects.isNull(oldModel)) {
            throw new BusinessException("编辑的检验项不存在");
        }
        //同一类别下检验项名称不可重复，否则提交时提示“同一类别下检验项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), dto.getId())) {
            throw new BusinessException("同一类别下检验项名称不能重复");
        }
        //具体可以编辑的列
        oldModel.setName(dto.getName());
        oldModel.setDataType(dto.getDataType());
        oldModel.setMeasureUnit(dto.getMeasureUnit());
        oldModel.setPossibleValue(dto.getPossibleValue());
        oldModel.setDefaultValue(dto.getDefaultValue());
        oldModel.setOpDesc(dto.getOpDesc());
        oldModel.setNote(dto.getNote());
        oldModel.setUpdateBy(SecurityUtils.getUser().getUserId());
        oldModel.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = save(oldModel);
        return save;
    }

    /**
     * 格式：JYX#6位数字# 起始值：JYX000001
     *
     * @param prefix 前缀
     * @return
     */
    private String getNextCode(String prefix) {
        QtCheckItem one = getOne(new LambdaQueryWrapper<QtCheckItem>()
                .orderByDesc(QtCheckItem::getCode)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.isNull(one)) {
            return prefix + "000001";
        }
        String code = one.getCode();
        String substring = code.substring(3);
        Long l;
        try {
            l = Long.parseLong(substring);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            l = 1L;
        }
        l++;
        String finalValStr = String.valueOf(l);
        int length = finalValStr.length();
        while (length++ < 6) {
            finalValStr = 0 + finalValStr;
        }
        return prefix + finalValStr;
    }

    /**
     * 同类别下名称是否重复
     *
     * @param categoryId 类别id
     * @param name       名称
     * @param filterId   过滤的id，编辑时使用
     * @return 是否
     */
    private boolean existName(Integer categoryId, String name, Integer filterId) {
        int count = count(new LambdaQueryWrapper<QtCheckItem>()
                .eq(QtCheckItem::getCategoryId, categoryId)
                .eq(QtCheckItem::getName, name)
                .ne(Objects.nonNull(filterId), QtCheckItem::getId, filterId));
        return count > 0;
    }

}




