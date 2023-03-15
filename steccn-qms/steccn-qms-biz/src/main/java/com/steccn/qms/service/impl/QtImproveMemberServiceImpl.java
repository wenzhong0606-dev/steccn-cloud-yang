package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.qms.domain.QtComplainType;
import com.steccn.qms.domain.QtImproveMember;
import com.steccn.qms.dto.QtImproveMemberDTO;
import com.steccn.qms.service.QtImproveMemberService;
import com.steccn.qms.mapper.QtImproveMemberMapper;
import com.steccn.qms.vo.QtComplainTypeVO;
import com.steccn.qms.vo.QtImproveMemberVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qms_improve_member(整改成员)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
public class QtImproveMemberServiceImpl extends ServiceImpl<QtImproveMemberMapper, QtImproveMember>
        implements QtImproveMemberService {

    @Override
    public List<QtImproveMemberVO> memberList() {
        List<QtImproveMemberVO> resList = null;
        List<QtImproveMember> dbList = list(new LambdaQueryWrapper<QtImproveMember>()
                .orderByAsc(QtImproveMember::getCreateTime));
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, QtImproveMemberVO.class);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    public Boolean addMember(QtImproveMemberDTO dto) {
        long count = count(new LambdaQueryWrapper<QtImproveMember>()
                .eq(QtImproveMember::getUserId, dto.getUserId()));
        Assert.isTrue(count == 0, "整改成员不允许重复");
        QtImproveMember model = new QtImproveMember();
        BeanUtils.copyProperties(dto, model);
        return save(model);
    }

    @Override
    public Boolean delete(Integer id) {
        return removeById(id);
    }
}




