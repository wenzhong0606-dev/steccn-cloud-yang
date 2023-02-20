package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.qms.domain.QtReport;
import com.steccn.qms.domain.QtReportWarnUser;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.service.QtReportWarnUserService;
import com.steccn.qms.mapper.QtReportWarnUserMapper;
import com.steccn.qms.vo.QtReportWarnUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【qt_report_warn_user(spc报表卡片报警人)】的数据库操作Service实现
 * @createDate 2023-02-16 15:50:52
 */
@Service
@RequiredArgsConstructor
public class QtReportWarnUserServiceImpl extends ServiceImpl<QtReportWarnUserMapper, QtReportWarnUser>
        implements QtReportWarnUserService {
    private final OaInfoService oaInfoService;

    @Override
    public void saveWarnUser(List<Long> warnUserIds, Integer qtReportId) {
        List<QtReportWarnUser> warnUserList = Lists.newArrayList();
        warnUserIds.forEach(userId -> {
            QtReportWarnUser qtReportWarnUser = new QtReportWarnUser();
            qtReportWarnUser.setUserId(userId);
            qtReportWarnUser.setReportId(qtReportId);
            warnUserList.add(qtReportWarnUser);
        });
        saveBatch(warnUserList);
    }

    @Override
    public void updateWarnUser(List<Long> warnUserIds, Integer id) {
        remove(new LambdaQueryWrapper<QtReportWarnUser>().in(QtReportWarnUser::getReportId, id));
        saveWarnUser(warnUserIds, id);
    }

    @Override
    public List<QtReportWarnUserVO> getWarnUser(Integer id) {
        List<QtReportWarnUserVO> res = Lists.newArrayList();
        List<QtReportWarnUser> list = list(new LambdaQueryWrapper<QtReportWarnUser>().in(QtReportWarnUser::getReportId, id));
        if (CollUtil.isNotEmpty(list)) {
            List<Long> userIds= Lists.newArrayList();
            list.forEach( qtReportWarnUser -> userIds.add(qtReportWarnUser.getUserId()));
            Map<Long, String> map = FeignOptional.of(() -> oaInfoService.listByIdCh(userIds), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            list.forEach( qtReportWarnUser -> {
                QtReportWarnUserVO user = new QtReportWarnUserVO();
                user.setId(qtReportWarnUser.getUserId());
                user.setName(map.get(qtReportWarnUser.getUserId()));
                res.add(user);
            });
        }
        return res;
    }

}




