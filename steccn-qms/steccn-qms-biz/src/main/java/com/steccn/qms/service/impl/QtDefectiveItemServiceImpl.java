package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.dto.QtDefectiveItemSaveDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.service.QtCheckPlanService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtDefectiveItemTreeVo;
import com.steccn.qms.vo.QtDefectiveItemVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private QtCheckPlanGroupItemService qtCheckPlanGroupItemService;

    @Autowired
    private QtCheckPlanService qtCheckPlanService;

    @Override
    public Page<QtDefectiveItemVO> pageList(QtDefectiveItemPageDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<QtDefectiveItem> condition = new LambdaQueryWrapper<QtDefectiveItem>()
                .eq(QtDefectiveItem::getShowFlag, MpConstant.YES)
                .eq(QtDefectiveItem::getCategoryId, dto.getCategoryId())
                .like(StrUtil.isNotBlank(dto.getName()), QtDefectiveItem::getName, dto.getName());
        //2.解析排序规则
        if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(name using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(name using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_ITEM_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtDefectiveItem::getStatus);
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
    @Transactional(rollbackFor = Exception.class)
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
        boolean save = save(model);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(QtDefectiveItemSaveDTO dto) {
        //校验缺陷项是否存在
        QtDefectiveItem oldModel = getById(dto.getId());
        if (Objects.isNull(oldModel)) {
            throw new BusinessException("编辑的缺陷项不存在");
        }
        //快照不允许被编辑
        if (MpConstant.NO.equals(oldModel.getShowFlag())) {
            throw new BusinessException("检验项快照不允许被编辑");
        }
        //同一类别下检验项名称不可重复，否则提交时提示“同一类别下检验项名称不能重复”
        if (existName(dto.getCategoryId(), dto.getName(), dto.getId())) {
            throw new BusinessException("同一类别下缺陷项名称不能重复");
        }
        //具体可以编辑的列
        oldModel.setName(dto.getName());
        oldModel.setNameEn(PinyinUtil.getPinyin(dto.getName(), Strings.EMPTY));
        oldModel.setNote(dto.getNote());
        boolean save = updateById(oldModel);
        //判断是否关联检验方案，如果关联则需要升检验方案的版本
        Set<Integer> planIdList = qtCheckPlanService.linkedPlan(oldModel.getId(), 1);
        qtCheckPlanService.upVersion(planIdList);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean status(QtItemStatusDTO dto) {
        //1.先校验id是否存在
        QtDefectiveItem oldItem = getById(dto.getId());
        if (Objects.isNull(oldItem)) {
            throw new BusinessException("缺陷项不存在");
        }
        //2.如果是启用操作，则直接返回
        if (MpConstant.STATUS_START.equals(dto.getStatus())) {
            oldItem.setStatus(MpConstant.STATUS_START);
            return updateById(oldItem);
        }
        //2.快照不允许被删除
        if (MpConstant.NO.equals(oldItem.getShowFlag())) {
            throw new BusinessException("缺陷项快照不允许被停用");
        }
        //3.停用操作
        Set<Integer> inUseDefectiveItemSet = qtCheckPlanGroupItemService.inUseDefectiveItem();
        if (inUseDefectiveItemSet.contains(dto.getId())) {
            throw new BusinessException("已有检验方案调用了该缺陷项，不允许停用");
        }
        oldItem.setStatus(MpConstant.STATUS_STOP);
        return updateById(oldItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        //1.先校验id是否存在
        QtDefectiveItem oldItem = getById(id);
        if (Objects.isNull(oldItem)) {
            throw new BusinessException("缺陷项不存在");
        }
        //2.快照不允许被删除
        if (MpConstant.NO.equals(oldItem.getShowFlag())) {
            throw new BusinessException("缺陷项快照不允许被删除");
        }
        //3.判断是否已有检验方案在使用
        Set<Integer> inUseDefectiveItemSet = qtCheckPlanGroupItemService.inUseDefectiveItem();
        if (inUseDefectiveItemSet.contains(id)) {
            throw new BusinessException("已有检验方案调用了该缺陷项，不允许删除");
        }
        return removeById(id);
    }

    @Override
    public List<QtDefectiveItemTreeVo> selectItemList() {
        List<QtDefectiveItem> dataList = list(new LambdaQueryWrapper<QtDefectiveItem>()
                .eq(QtDefectiveItem::getShowFlag, MpConstant.YES)
                .eq(QtDefectiveItem::getStatus, MpConstant.STATUS_START)
                .orderByAsc(QtDefectiveItem::getCreateTime));
        if (CollUtil.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        //组装成树状数据
        Map<String, List<QtDefectiveItemVO>> mapData = new HashMap<>();
        dataList.forEach(tmp -> {
            QtItemCategory qtItemCategory = qtItemCategoryService.getById(tmp.getCategoryId());
            QtDefectiveItemVO vo = new QtDefectiveItemVO();
            BeanUtils.copyProperties(tmp, vo);
            if (mapData.get(qtItemCategory.getName()) == null) {
                List<QtDefectiveItemVO> vos = Lists.newArrayList();
                vos.add(vo);
                mapData.put(qtItemCategory.getName(), vos);
            } else {
                mapData.get(qtItemCategory.getName()).add(vo);
            }
        });
        List<QtDefectiveItemTreeVo> vos = Lists.newArrayList();
        for (Map.Entry<String, List<QtDefectiveItemVO>> entry : mapData.entrySet()) {
            QtDefectiveItemTreeVo qtDefectiveItemTreeVo = new QtDefectiveItemTreeVo();
            qtDefectiveItemTreeVo.setCategoryName(entry.getKey());
            qtDefectiveItemTreeVo.setChildren(entry.getValue());
            vos.add(qtDefectiveItemTreeVo);
        }
        return vos;
    }

    @Override
    public Map<Integer, Integer> defectiveItemSnapshot(List<Integer> defectiveItemIdList) {
        //如果缺陷项id集合为空，则直接返回空的map
        if (CollUtil.isEmpty(defectiveItemIdList)) {
            return Maps.newHashMap();
        }
        //根据检验项主键查询所有的集合数据
        List<QtDefectiveItem> qtDefectiveItemList = listByIds(defectiveItemIdList);
        //定义返回map
        Map<Integer, Integer> resMap = Maps.newHashMap();
        //保存新的快照，快照对用户不可见
        qtDefectiveItemList.forEach(v -> {
            QtDefectiveItem qtDefectiveItem = new QtDefectiveItem();
            BeanUtils.copyProperties(v, qtDefectiveItem);
            qtDefectiveItem.setId(null);
            qtDefectiveItem.setShowFlag(MpConstant.NO);
            save(qtDefectiveItem);
            resMap.put(v.getId(), qtDefectiveItem.getId());
        });
        return resMap;
    }

    /**
     * 根据缺陷项类别id查询缺陷项列表
     * @param id
     * @return
     */
    @Override
    public List<QtBaseIdNameDTO> selectItemIds(Integer id) {
        //如果类别不传，返回所有的缺陷项
        List<QtBaseIdNameDTO> qtBaseIdNameDTOS = Lists.newArrayList();
        List<QtDefectiveItem> qtDefectiveItems = Lists.newArrayList();
        QtBaseIdNameDTO qtBaseIdNameDTO = new QtBaseIdNameDTO();
        if (id == null){
            qtDefectiveItems = list();
            if (!qtDefectiveItems.isEmpty()) {
                for (QtDefectiveItem tem : qtDefectiveItems) {
                    qtBaseIdNameDTO.setId(tem.getId());
                    qtBaseIdNameDTO.setName(tem.getName());
                    qtBaseIdNameDTOS.add(qtBaseIdNameDTO);
                }
            }
        }else {
            qtDefectiveItems = list(new LambdaQueryWrapper<QtDefectiveItem>().eq(QtDefectiveItem::getCategoryId,id));
            if (!qtDefectiveItems.isEmpty()){
                for (int i = 0; i < qtDefectiveItems.size() ; i++) {
                    QtBaseIdNameDTO tmp = new QtBaseIdNameDTO();
                    tmp.setId(qtDefectiveItems.get(i).getId());
                    tmp.setName(qtDefectiveItems.get(i).getName());
                    qtBaseIdNameDTOS.add(tmp);
                }
                }
            }
        return qtBaseIdNameDTOS;
    }

    @Override
    public Map<Integer, String> listByIdCh(Set<Integer> defectIdSet) {
        Map<Integer, String> resMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(defectIdSet)){
            List<QtDefectiveItem> qtDefectiveItems = listByIds(defectIdSet);
            if (CollUtil.isNotEmpty(qtDefectiveItems)) {
                qtDefectiveItems.forEach(ele -> {
                    resMap.put(ele.getId(), ele.getName());
                });
            }
        }
        return resMap;

    }

    @Override
    public Map<Integer, QtDefectiveItem> listById(Set<Integer> defectIdSet) {
        Map<Integer, QtDefectiveItem> resMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(defectIdSet)){
            List<QtDefectiveItem> qtDefectiveItems = listByIds(defectIdSet);
            if (CollUtil.isNotEmpty(qtDefectiveItems)) {
                qtDefectiveItems.forEach(ele -> {
                    resMap.put(ele.getId(), ele);
                });
            }
        }
        return resMap;
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
        long count = count(new LambdaQueryWrapper<QtDefectiveItem>()
                .eq(QtDefectiveItem::getShowFlag, MpConstant.YES)
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




