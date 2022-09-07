package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.mapper.QtCheckItemMapper;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtCheckItemListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private QtItemCategoryService qtItemCategoryService;

    @Autowired
    private RemoteDictFeignApi remoteDictFeign;

    @Override
    public Page<QtCheckItemListVO> pageList(QtCheckItemPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtCheckItem> condition = new LambdaQueryWrapper<QtCheckItem>()
                .eq(QtCheckItem::getCategoryId, dto.getCategoryId())
                .like(Objects.nonNull(dto.getName()), QtCheckItem::getName, dto.getName());
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
        IPage<QtCheckItem> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<QtCheckItemListVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (QtCheckItem qtCheckItem : resPage.getRecords()) {
                QtCheckItemListVO vo = new QtCheckItemListVO();
                BeanUtils.copyProperties(qtCheckItem, vo);
                dictVal(vo);
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtCheckItemSaveDTO dto) {
        //1.判断缺陷项类别是否存在
        if (!qtItemCategoryService.existType(dto.getCategoryId(), QtManagerConstant.QtItemCategoryEnum.CHECK.getType())) {
            throw new BusinessException("检验项类别不存在");
        }
        //2.同一类别下检验项名称不可重复，否则提交时提示“同一类别下检验项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), null)) {
            throw new BusinessException("同一类别下检验项名称不能重复");
        }
        //3.根据数据字典id去查询数据字典信息
        checkExistDict(SysDictConstant.TYPE_MEASURE_UNIT, dto.getMeasureUnit(), "计量单位不存在");
        validDataTypeAndDefaultVal(SysDictConstant.TYPE_CHECK_DATA_TYPE, dto.getDataType(), "数据类型不存在", dto.getDefaultValue());
        //4.插入数据
        QtCheckItem model = new QtCheckItem();
        BeanUtils.copyProperties(dto, model);
        //5.获取最大的自增编码
        model.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYX, CodeGenerateUtil.NUM_BIT_6, currentMaxCode()));
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

    private void validDataTypeAndDefaultVal(String type, Long dataTypeDictId, String errorMsg, List<String> defaultValList) {
        R<Map<Long, SysDictVO>> dataTypeFeignRes = remoteDictFeign.listById(Arrays.asList(dataTypeDictId));
        if (ResultCode.SUCCESS.getCode() != dataTypeFeignRes.getCode()) {
            throw new BusinessException("公共服务不可用");
        }
        if (Objects.isNull(dataTypeFeignRes.getData()) || MapUtil.isEmpty(dataTypeFeignRes.getData())) {
            throw new BusinessException(errorMsg);
        }
        //获取数据类型的数据字典
        SysDictVO sysDictVO = dataTypeFeignRes.getData().get(dataTypeDictId);
        if (!type.equals(sysDictVO.getType())) {
            throw new BusinessException(errorMsg);
        }
//      数值1 百分比2 文本3 单选4 多选5 人工判断良或次6 自动计算极差7 自动计算平均值8
//      仅当数据类型为“数值”、“百分比”、“文本”、“单选”、“多选”、“人工判断良或次”时显示
//      1）数据类型为“数值”、“百分比”：提示“请输入默认值”，数值输入，范围同double型
//      2）数据类型为“文本”：提示“请输入默认值”，文本输入，1<=长度<=100
//      3）数据类型为“单选”、“多选”：提示“请选择默认选项”，下拉单选/多选可能值
//      4）数据类型为“人工判断良或次”：提示“请选择默认选项”，下拉单选“良”或“次”
        List<String> codeList = Arrays.asList("1", "2", "3", "4", "5", "6");
        List<String> qualityList = Arrays.asList("良", "次");
        if (codeList.contains(sysDictVO.getCode())) {
            //需要校验defaultValue
            if ("1".equals(sysDictVO.getCode()) || "2".equals(sysDictVO.getCode())) {
                if (CollUtil.isNotEmpty(defaultValList)) {
                    for (String s : defaultValList) {
                        try {
                            Double.parseDouble(s);
                        } catch (Exception e) {
                            throw new BusinessException("数据类型为数值，百分比时，数据输入类型应为数值类型");
                        }
                    }
                }
            } else if ("6".equals(sysDictVO.getCode())) {
                if (CollUtil.isNotEmpty(defaultValList)) {
                    for (String s : defaultValList) {
                        if (!qualityList.contains(s)) {
                            throw new BusinessException("人工判断良或次只能选择良或次");
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查数据字典类型是否存在
     *
     * @param type     数据字典类型
     * @param dictId   数据字典id
     * @param errorMsg 错误消息
     */
    public void checkExistDict(String type, Long dictId, String errorMsg) {
        //根据数据字典id去查询数据字典信息
        if (Objects.nonNull(dictId)) {
            R<Boolean> measureUnitFeignRes = remoteDictFeign.existTypeId(type, dictId);
            if (ResultCode.SUCCESS.getCode() != measureUnitFeignRes.getCode()) {
                throw new BusinessException("公共服务不可用");
            }
            if (Objects.isNull(measureUnitFeignRes.getData()) || !measureUnitFeignRes.getData()) {
                throw new BusinessException(errorMsg);
            }
        }
    }

    @Override
    public QtCheckItemListVO info(Integer id) {
        QtCheckItemListVO resVo = null;
        QtCheckItem byId = getById(id);
        if (Objects.nonNull(byId)) {
            resVo = new QtCheckItemListVO();
            BeanUtils.copyProperties(byId, resVo);
            //数据字典字段赋值name
            dictVal(resVo);
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
        //根据数据字典id去查询数据字典信息
        checkExistDict(SysDictConstant.TYPE_MEASURE_UNIT, dto.getMeasureUnit(), "计量单位不存在");
        validDataTypeAndDefaultVal(SysDictConstant.TYPE_CHECK_DATA_TYPE, dto.getDataType(), "数据类型不存在", dto.getDefaultValue());

        //具体可以编辑的列
        oldModel.setName(dto.getName());
        oldModel.setNameEn(PinyinUtil.getPinyin(dto.getName(), Strings.EMPTY));
        oldModel.setDataType(dto.getDataType());
        oldModel.setMeasureUnit(dto.getMeasureUnit());
        oldModel.setOpDesc(dto.getOpDesc());
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

    @Override
    public List<QtCheckItemListVO> selectItemList() {
        List<QtCheckItem> dataList = list(new LambdaQueryWrapper<QtCheckItem>()
                .eq(QtCheckItem::getStatus, MpConstant.STATUS_START)
                .orderByDesc(QtCheckItem::getCreateTime));
        if (CollUtil.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        List<QtCheckItemListVO> resList = Lists.newArrayList();
        for (QtCheckItem model : dataList) {
            QtCheckItemListVO resVo = new QtCheckItemListVO();
            BeanUtils.copyProperties(model, resVo);
            dictVal(resVo);
            resList.add(resVo);
        }
        return resList;
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
        QtCheckItem maxCodeModel = getOne(new LambdaQueryWrapper<QtCheckItem>().orderByDesc(QtCheckItem::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_JYX.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
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
        long count = count(new LambdaQueryWrapper<QtCheckItem>()
                .eq(QtCheckItem::getCategoryId, categoryId)
                .eq(QtCheckItem::getName, name)
                .ne(Objects.nonNull(filterId), QtCheckItem::getId, filterId));
        return count > 0;
    }


    /**
     * 数据字典的值转换
     *
     * @param vo vo
     */
    private void dictVal(QtCheckItemListVO vo) {
        R<Map<Long, SysDictVO>> dictFeignRes = remoteDictFeign.listById(Arrays.asList(vo.getDataType(), vo.getMeasureUnit()));
        if (ResultCode.SUCCESS.getCode() != dictFeignRes.getCode()) {
            throw new BusinessException("公共服务不可用");
        }
        Map<Long, SysDictVO> dictMap = dictFeignRes.getData();
        SysDictVO dataTypeDictVO = dictMap.get(vo.getDataType());
        SysDictVO measureUnitDictVO = dictMap.get(vo.getMeasureUnit());
        vo.setDataTypeName(Objects.nonNull(dataTypeDictVO) ? dataTypeDictVO.getName() : null);
        vo.setMeasureUnitName(Objects.nonNull(measureUnitDictVO) ? measureUnitDictVO.getName() : null);
    }
}




