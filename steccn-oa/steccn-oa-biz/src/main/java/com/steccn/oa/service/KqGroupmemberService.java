package com.steccn.oa.service;

import com.steccn.oa.domain.KqGroupmember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaGroupLeaderDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.vo.OaGroupIdAndNameVO;
import com.steccn.oa.vo.OaTodayClassVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
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
     * 获取用户所属考勤组
     * @return
     */
    OaGroupIdAndNameVO getOneGroupByUserId();

    /**
     * 获取甲乙丙班的班组id
     *
     * @return
     */
    List<OaGroupIdAndNameVO> getABCGroup();

    /**
     * 根据班组id获取班组长
     * @param groupIds
     * @return
     */
    Map<Integer, OaGroupLeaderDTO> getLeaderByGroupId(Set<Integer> groupIds);

    /**
     * oa获取基片当班信息
     * @param workshopName 车间id
     * @param date 日期
     * @return
     */
    List<OaTodayClassVO> getTodayClassInfo(String workshopName, LocalDate date);

    /**
     * oa获取sjg当班信息
     * @return
     */
    OaTodayClassVO getTodayClassInfoSJG();
}
