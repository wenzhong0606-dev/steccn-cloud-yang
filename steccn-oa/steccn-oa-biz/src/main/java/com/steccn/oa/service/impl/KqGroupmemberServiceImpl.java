package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.domain.KqGroupmember;
import com.steccn.oa.dto.OaGroupDTO;
import com.steccn.oa.service.KqGroupmemberService;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.vo.OaGroupIdAndNameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【kq_groupmember】的数据库操作Service实现
* @createDate 2023-04-04 11:20:18
*/
@Service
@RequiredArgsConstructor
public class KqGroupmemberServiceImpl extends ServiceImpl<KqGroupmemberMapper, KqGroupmember>
    implements KqGroupmemberService{
    private final KqGroupmemberMapper kqGroupmemberMapper;

    @Override
    public List<OaGroupIdAndNameVO> findGroupByUserId(Set<Long> userId) {
        if(CollUtil.isEmpty(userId)){
            throw new BusinessException("userId不能为空");
        }
        List<OaGroupIdAndNameVO> result = Lists.newArrayList();
        MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<OaGroupDTO>()
                .selectAs(KqGroupmember::getTypevalue,OaGroupDTO::getUserId)
                .selectAs(KqGroupmember::getGroupid,OaGroupDTO::getGroupId)
                .selectAs(KqGroup::getGroupname,OaGroupDTO::getGroupName)
                .leftJoin(KqGroup.class,KqGroup::getId,KqGroupmember::getGroupid)
                .isNull(KqGroupmember::getIsdelete)
                .in(KqGroupmember::getTypevalue,userId);
        List<OaGroupDTO> oaGroupDTOS = kqGroupmemberMapper.selectJoinList(OaGroupDTO.class, mpjLambdaWrapper);
        oaGroupDTOS.forEach(tmp -> {
            OaGroupIdAndNameVO vo = new OaGroupIdAndNameVO();
            BeanUtils.copyProperties(tmp,vo);
            result.add(vo);
        });
        return result;
    }

    @Override
    public OaGroupIdAndNameVO getOneGroupByUserId() {
        OaGroupIdAndNameVO vo = new OaGroupIdAndNameVO();
        Set<Long> userIdSet = Sets.newHashSet();
        userIdSet.add(SecurityUtils.getUser().getUserId());
        List<OaGroupIdAndNameVO> resultList = this.findGroupByUserId(userIdSet);
        if(CollUtil.isNotEmpty(resultList)){
            vo = resultList.get(0);
            String nameByOaName = OaManagerConstant.OaTeamGroupMapping.getNameByOaName(vo.getGroupName());
            if(StrUtil.isNotEmpty(nameByOaName)){
                vo.setGroupName(nameByOaName);
            }
        }
        return vo;
    }
}




