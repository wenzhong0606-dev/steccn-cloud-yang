package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.steccn.oa.mapper.KqGroupMapper;
import com.steccn.oa.service.KqGroupService;
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

    private final KqGroupService kqGroupService;

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
            vo.setClassGroupId(tmp.getGroupId());
            vo.setClassGroupName(tmp.getGroupName());
            vo.setUserId(tmp.getUserId());
            result.add(vo);
        });
        return result;
    }

    @Override
    public OaGroupIdAndNameVO getOneGroupByUserId() {
        OaGroupIdAndNameVO result = new OaGroupIdAndNameVO();
        Set<Long> userIdSet = Sets.newHashSet();
        userIdSet.add(SecurityUtils.getUser().getUserId());
        List<OaGroupIdAndNameVO> resultList = this.findGroupByUserId(userIdSet);
        if(CollUtil.isEmpty(resultList)){
            return result;
        }
        //修改映射关系
        resultList = changeGroupRelation(resultList);
        result = resultList.get(0);
        return result;
    }

    @Override
    public List<OaGroupIdAndNameVO> getABCGroup() {
        List<OaGroupIdAndNameVO> result = Lists.newArrayList();
        //放入具体值
        Set<String> oaClassName = Sets.newHashSet();
        for (OaManagerConstant.OaTeamGroupMapping value : OaManagerConstant.OaTeamGroupMapping.values()) {
            oaClassName.add(value.getOaName());
        }
        List<KqGroup> list = kqGroupService.list(new LambdaQueryWrapper<KqGroup>().in(KqGroup::getGroupname, oaClassName));
        for(KqGroup tmp:list){
            OaGroupIdAndNameVO vo = new OaGroupIdAndNameVO();
            vo.setClassGroupId(tmp.getId());
            vo.setClassGroupName(tmp.getGroupname());
            result.add(vo);
        }
        //修改有映射关系
        result = changeGroupRelation(result);
        return result;
    }


    /**
     * 修改映射关系
     * @param resultList
     * @return
     */
    private List<OaGroupIdAndNameVO> changeGroupRelation(List<OaGroupIdAndNameVO> resultList){
        if(CollUtil.isNotEmpty(resultList)){
            resultList.forEach(tmp->{
                String nameByOaName = OaManagerConstant.OaTeamGroupMapping.getNameByOaName(tmp.getClassGroupName());
                if(StrUtil.isNotEmpty(nameByOaName)){
                    tmp.setClassGroupName(nameByOaName);
                }
            });
        }
        return resultList;
    }
}




