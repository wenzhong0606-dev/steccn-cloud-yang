package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.domain.KqGroupmember;
import com.steccn.oa.dto.OaGroupDTO;
import com.steccn.oa.service.KqGroupmemberService;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.vo.OaGroupIdAndNameVO;
import lombok.RequiredArgsConstructor;
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
    public Map<Long, List<OaGroupIdAndNameVO>> findGroupByUserId(Set<Long> userId) {
        Map<Long, List<OaGroupIdAndNameVO>> result = MapUtil.newHashMap();
        MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<OaGroupDTO>()
                .selectAs(KqGroupmember::getTypevalue,OaGroupDTO::getUserId)
                .selectAs(KqGroupmember::getGroupid,OaGroupDTO::getGroupId)
                .selectAs(KqGroup::getGroupname,OaGroupDTO::getGroupName)
                .leftJoin(KqGroup.class,KqGroup::getId,KqGroupmember::getGroupid)
                .ne(KqGroupmember::getIsdelete,1);
        List<OaGroupDTO> oaGroupDTOS = kqGroupmemberMapper.selectJoinList(OaGroupDTO.class, mpjLambdaWrapper);
        oaGroupDTOS.forEach(tmp -> {
            if(result.get(tmp.getUserId())==null){
                result.put(tmp.getUserId(), Lists.newArrayList());
            }
            result.get(tmp.getUserId()).add(new OaGroupIdAndNameVO(tmp.getGroupId(),tmp.getGroupName()));
        });
        return result;
    }
}




