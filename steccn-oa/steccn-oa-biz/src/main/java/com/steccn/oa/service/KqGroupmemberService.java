package com.steccn.oa.service;

import com.steccn.oa.domain.KqGroupmember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaGroupIdAndNameVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【kq_groupmember】的数据库操作Service
* @createDate 2023-04-04 11:20:18
*/
public interface KqGroupmemberService extends IService<KqGroupmember> {
    Map<Long, List<OaGroupIdAndNameVO>> findGroupByUserId(Set<Long> userId);
}
