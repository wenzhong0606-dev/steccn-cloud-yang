package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.qms.domain.QtComplainType;
import com.steccn.qms.domain.QtCustomerComplain;
import com.steccn.qms.service.QtComplainTypeService;
import com.steccn.qms.mapper.QtComplainTypeMapper;
import com.steccn.qms.dto.QtComplainTypeDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.vo.QtComplainTypeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qms_complain_type(投诉类型)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:53
 */
@Service
public class QtComplainTypeServiceImpl extends ServiceImpl<QtComplainTypeMapper, QtComplainType>
        implements QtComplainTypeService {

    @Autowired
    private QtCustomerComplainService customerComplainService;

    @Override
    public List<QtComplainTypeVO> typeList() {
        List<QtComplainTypeVO> resList = null;
        List<QtComplainType> dbList = list(new LambdaQueryWrapper<QtComplainType>()
                .orderByAsc(QtComplainType::getCreateTime));
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, QtComplainTypeVO.class);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    public Boolean saveType(QtComplainTypeDTO dto) {
        //编辑是否存在id
        if (Objects.nonNull(dto.getId())) {
            QtComplainType oldModel = getById(dto.getId());
            Assert.notNull(oldModel, "编辑的投诉类型不存在");
        }

        //判断是否存在
        long count = count(new LambdaQueryWrapper<QtComplainType>()
                .eq(QtComplainType::getType, dto.getType())
                .ne(Objects.nonNull(dto.getId()), QtComplainType::getId, dto.getId()));
        Assert.isTrue(count == 0, "投诉类型名称不可重复");
        //定义新对象
        QtComplainType model = new QtComplainType();
        BeanUtils.copyProperties(dto, model);
        return saveOrUpdate(model);
    }

    @Override
    public Boolean delete(Integer id) {
        long count = customerComplainService.count(new LambdaQueryWrapper<QtCustomerComplain>().eq(QtCustomerComplain::getComplainType, id));
        Assert.isTrue(count == 0, "存在该类型的客诉，不允许删除");
        return removeById(id);
    }

    @Override
    public Map<Integer, String> listByIdCh(Set<Integer> ids) {
        Map<Integer, String> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<QtComplainType> qtComplainTypes = listByIds(ids);
            if (CollUtil.isEmpty(qtComplainTypes)) {
                return map;
            }
            qtComplainTypes.forEach(qtComplainType -> {
                map.put(qtComplainType.getId(), qtComplainType.getType());
            });
        }
        return map;
    }
}




