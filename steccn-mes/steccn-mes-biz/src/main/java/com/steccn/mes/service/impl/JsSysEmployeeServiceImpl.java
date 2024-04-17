package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.JsSysEmployee;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.mapper.JsSysEmployeeMapper;
import com.steccn.mes.vo.MesUserInfoVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【js_sys_employee(员工表)】的数据库操作Service实现
* @createDate 2024-01-03 16:26:50
*/
@Service
@RequiredArgsConstructor
public class JsSysEmployeeServiceImpl extends ServiceImpl<JsSysEmployeeMapper, JsSysEmployee>
    implements JsSysEmployeeService{
    private final JsSysEmployeeMapper jsSysEmployeeMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Map<String, List<String>> getPostUser(Set<String> postCode) {
        Map<String, List<String>> result = Maps.newHashMap();
        List<JsSysEmployee> list = list(new LambdaQueryWrapper<JsSysEmployee>()
                .select(JsSysEmployee::getEmpCode, JsSysEmployee::getOfficeCode)
                .eq(JsSysEmployee::getStatus, OaConstant.NO.toString())
                .in(JsSysEmployee::getOfficeCode, postCode));
        if(CollUtil.isNotEmpty(list)){
            result = list.stream()
                    .collect(Collectors.groupingBy(
                            JsSysEmployee::getOfficeCode,
                            Collectors.mapping(JsSysEmployee::getEmpCode,Collectors.toList())
                    ));
        }
        return result;
    }

    @Override
    public List<MesUserInfoVO> getEmpDepPost(Set<String> userName) {
        List<MesUserInfoVO> mesUserInfo = jsSysEmployeeMapper.selectEmpInfo(userName);
        if(CollUtil.isNotEmpty(mesUserInfo)){
            Set<String> postSet = mesUserInfo.stream().map(MesUserInfoVO::getPostCode).collect(Collectors.toSet());
            Map<String, SysOfficeVO> postDepMap = FeignOptional.of(() -> remoteOfficeFeign.postDepMap(postSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            mesUserInfo.forEach(tmp->{
                SysOfficeVO officeInfo = postDepMap.get(tmp.getPostCode());
                if(Objects.nonNull(officeInfo)){
                    tmp.setDepCode(officeInfo.getOfficeCode());
                    tmp.setDepName(officeInfo.getOfficeName());
                }
            });

        }
        return mesUserInfo;
    }
}




