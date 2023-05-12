package com.steccn.oa.service;

import com.steccn.oa.domain.KqGroupmember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaGroupIdAndNameVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author AndrewLuo
* @description 针对表【kq_groupmember】的数据库操作Service
* @createDate 2023-04-04 11:20:18
*/
public interface KqGroupmemberService extends IService<KqGroupmember> {
    /**
     *
     * @param userId
     * @return
     */
    List<OaGroupIdAndNameVO> findGroupByUserId(Set<Long> userId);

    /**
     * 获取一个考勤组
     * @return
     */
    List<OaGroupIdAndNameVO> getOneGroupByUserId();
}
