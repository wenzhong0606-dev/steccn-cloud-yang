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
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.dto.QtDefectiveItemSaveDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtDefectiveItemVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qt_defective_item(缺陷项表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@Slf4j
public class QtDefectiveItemServiceImpl extends ServiceImpl<QtDefectiveItemMapper, QtDefectiveItem>
        implements QtDefectiveItemService {

    @Autowired
    private QtItemCategoryService qtItemCategoryService;

    @Override
    public Page<QtDefectiveItemVO> pageList(QtDefectiveItemPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtDefectiveItem> condition = new LambdaQueryWrapper<QtDefectiveItem>()
                .like(Objects.nonNull(dto.getName()), QtDefectiveItem::getName, dto.getName());
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

    @Override
    public Boolean add(QtDefectiveItemSaveDTO dto) {
        //1.判断缺陷项类别是否存在
        if (!qtItemCategoryService.existType(dto.getCategoryId(), QtManagerConstant.QtItemCategoryEnum.DEFECTIVE.getType())) {
            throw new BusinessException("缺陷项类别不存在");
        }
        //2.同一类别下缺陷项名称不可重复，否则提交时提示“同一类别下缺陷项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), null)) {
            throw new BusinessException("同一类别下缺陷项名称不能重复");
        }
        //3.插入数据
        QtDefectiveItem model = new QtDefectiveItem();
        BeanUtils.copyProperties(dto, model);
        //4.获取最大的自增编码
        model.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_QXX, CodeGenerateUtil.NUM_BIT_6, currentMaxCode()));
        //状态：0停用  1启用
        model.setStatus(MpConstant.STATUS_START);
        //把name转成拼音，用于排序
        model.setNameEn(PinyinUtil.getPinyin(dto.getName(), Strings.EMPTY));
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = save(model);
        return save;
    }

    @Override
    public Boolean edit(QtDefectiveItemSaveDTO dto) {
        //校验缺陷项是否存在
        QtDefectiveItem oldModel = getById(dto.getId());
        if (Objects.isNull(oldModel)) {
            throw new BusinessException("编辑的缺陷项不存在");
        }
        //同一类别下检验项名称不可重复，否则提交时提示“同一类别下检验项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), dto.getId())) {
            throw new BusinessException("同一类别下缺陷项名称不能重复");
        }
        //具体可以编辑的列
        oldModel.setName(dto.getName());
        oldModel.setNote(dto.getNote());
        oldModel.setUpdateBy(SecurityUtils.getUser().getUserId());
        oldModel.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = updateById(oldModel);
        return save;
    }

    @Override
    public Boolean status(QtItemStatusDTO dto) {
        throw new BusinessException("停用启用功能暂未实现");
    }

    @Override
    public Boolean delete(Integer id) {
        throw new BusinessException("删除功能暂未实现");
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
        int count = count(new LambdaQueryWrapper<QtDefectiveItem>()
                .eq(QtDefectiveItem::getCategoryId, categoryId)
                .eq(QtDefectiveItem::getName, name)
                .ne(Objects.nonNull(filterId), QtDefectiveItem::getId, filterId));
        return count > 0;
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        QtDefectiveItem maxCodeModel = getOne(new LambdaQueryWrapper<QtDefectiveItem>().orderByDesc(QtDefectiveItem::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_QXX.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




