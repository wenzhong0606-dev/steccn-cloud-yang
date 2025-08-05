package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.domain.PlatformOffice;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.service.PlatformOfficeService;
import com.steccn.platform.mapper.PlatformOfficeMapper;
import com.steccn.platform.vo.PlatformOfficeVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【platform_office(菜单表)】的数据库操作Service实现
* @createDate 2025-07-30 16:21:41
*/
@Service
public class PlatformOfficeServiceImpl extends ServiceImpl<PlatformOfficeMapper, PlatformOffice>
    implements PlatformOfficeService{

    @Override
    public Boolean add(PlatformOfficeDTO dto) {
        if(Objects.isNull(dto.getParentId())){
            dto.setParentId(OaConstant.NO);
        }else {
            PlatformOffice byId = getById(dto.getParentId());
            if(Objects.isNull(byId)){
                throw new BusinessException("父级节点不存在");
            }
        }
        PlatformOffice one = getOne(new LambdaQueryWrapper<PlatformOffice>().eq(PlatformOffice::getOfficeCode, dto.getOfficeCode()),false);
        if(Objects.nonNull(one)){
            throw new BusinessException("编码重复");
        }
        PlatformOffice model = new PlatformOffice();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }

    @Override
    public Boolean update(PlatformOfficeDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("主键id不能为空");
        }
        if(Objects.isNull(dto.getParentId())){
            dto.setParentId(OaConstant.NO);
        }else {
            PlatformOffice byId = getById(dto.getParentId());
            if(Objects.isNull(byId)){
                throw new BusinessException("父级节点不存在");
            }
        }
        PlatformOffice model = new PlatformOffice();
        BeanUtil.copyProperties(dto,model);
        //编码无法修改
        model.setOfficeCode(null);
        updateById(model);
        return true;
    }

    @Override
    public Boolean deleteById(Set<Integer> ids) {
        removeByIds(ids);
        return true;
    }

    @Override
    public PlatformOfficeVO getTreeById(Integer id) {
        List<PlatformOffice> list = list();
        List<PlatformOfficeVO> results = BeanUtil.copyToList(list, PlatformOfficeVO.class);
        PlatformOfficeVO platformOfficeVO = buildSubTree(results, id);
        return platformOfficeVO;
    }

    @Override
    public List<PlatformOfficeVO> getTreeList() {
        List<PlatformOffice> list = list();
        List<PlatformOfficeVO> data = BeanUtil.copyToList(list, PlatformOfficeVO.class);
        List<PlatformOfficeVO> parent = data.stream().filter(v -> v.getParentId().equals(OaConstant.NO))
                .sorted(Comparator.comparing(PlatformOfficeVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        List<PlatformOfficeVO> results = Lists.newArrayList();
        parent.forEach(tmp->{
            PlatformOfficeVO platformOfficeVO = buildSubTree(data, tmp.getId());
            results.add(platformOfficeVO);
        });
        return results;
    }

    public static PlatformOfficeVO buildSubTree(List<PlatformOfficeVO> allNodes, Integer parentId) {
        PlatformOfficeVO root = allNodes.stream()
                .filter(node -> node.getId().equals(parentId))
                .findFirst()
                .orElse(null);

        if (root == null) return null;

        buildChildren(root, allNodes);
        return root;
    }

    private static void buildChildren(PlatformOfficeVO parent, List<PlatformOfficeVO> allNodes) {
        List<PlatformOfficeVO> children = allNodes.stream()
                .filter(node -> Objects.equals(node.getParentId(), parent.getId()))
                .sorted(Comparator.comparing(PlatformOfficeVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        for (PlatformOfficeVO child : children) {
            buildChildren(child, allNodes); // 递归构建子节点
        }
        parent.setChildren(children);
    }
}




