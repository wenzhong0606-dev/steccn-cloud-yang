package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.JsSysEmployee;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.JsSysUser;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.mapper.JsSysEmployeeMapper;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.service.JsSysUserService;
import com.steccn.mes.vo.MesUserInfoVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaUserVO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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
    private final JsSysUserService jsSysUserService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final OaInfoService oaInfoService;
    private final JsSysOfficeService jsSysOfficeService;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

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
            Map<String, LocalDate> enterTimeMap = FeignOptional.of(() -> oaInfoService.getCompanyStartTime(mesUserInfo.stream()
                    .map(MesUserInfoVO::getLoginCode).collect(Collectors.toSet())), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            Set<String> postSet = mesUserInfo.stream().map(MesUserInfoVO::getPostCode).collect(Collectors.toSet());
            Map<String, SysOfficeVO> postDepMap = FeignOptional.of(() -> remoteOfficeFeign.postDepMap(postSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            mesUserInfo.forEach(tmp->{
                SysOfficeVO officeInfo = postDepMap.get(tmp.getPostCode());
                if(Objects.nonNull(enterTimeMap.get(tmp.getLoginCode()))){
                    tmp.setEnterTime(enterTimeMap.get(tmp.getLoginCode()));
                }
                if(Objects.nonNull(officeInfo)){
                    tmp.setDepCode(officeInfo.getOfficeCode());
                    tmp.setDepName(officeInfo.getOfficeName());
                }
            });

        }
        return mesUserInfo;
    }

    @Override
    public Map<String, List<String>> getEmpByDepTree(Set<String> depCode) {
        Map<String,List<String>> results = Maps.newHashMap();
        if(CollUtil.isNotEmpty(depCode)){
            for(String tmp:depCode){
                List<JsSysOffice> office = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                        .like(JsSysOffice::getParentCodes, tmp + ",")
                        .eq(JsSysOffice::getStatus, "0"));
                if(CollUtil.isNotEmpty(office)){
                    Set<String> officeCodes = office.stream().map(JsSysOffice::getOfficeCode).collect(Collectors.toSet());
                    officeCodes.add(tmp);
                    List<JsSysEmployee> employeeList = list(new LambdaQueryWrapper<JsSysEmployee>()
                            .in(JsSysEmployee::getOfficeCode, officeCodes));
                    List<String> empCode = employeeList.stream().map(v -> v.getEmpCode().split("_")[0]).collect(Collectors.toList());
                    results.put(tmp,empCode);
                }
            }
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String synOaUser() {
        List<OaUserVO> oaUserList = oaInfoService.getNormalUser().getData();
        List<JsSysUser> mesUser = jsSysUserService.list(new LambdaQueryWrapper<JsSysUser>()
                        .select(JsSysUser::getLoginCode,JsSysUser::getUserCode,JsSysUser::getUserName)
                        .eq(JsSysUser::getStatus, OaConstant.NO.toString()));
        Map<String, String> mesUserMap = mesUser.stream().collect(Collectors.toMap(JsSysUser::getLoginCode, JsSysUser::getUserCode));
        //mes中已经有的人
        List<String> usedMesUser = Lists.newArrayList();
        //mes中没有的人
        List<String> unUsedMesUser = Lists.newArrayList();
        List<JsSysEmployee> mesEmployeeList = jsSysEmployeeMapper.selectList(new LambdaQueryWrapper<JsSysEmployee>()
                .select(JsSysEmployee::getEmpCode,JsSysEmployee::getOfficeCode,JsSysEmployee::getOfficeName)
                .in(JsSysEmployee::getEmpCode, mesUser.stream().map(JsSysUser::getUserCode).collect(Collectors.toSet())));
        Map<String, String> mesEmployeeMap = mesEmployeeList.stream().collect(Collectors.toMap(JsSysEmployee::getEmpCode, JsSysEmployee::getOfficeCode));
        //获取部门数据
        List<JsSysOffice> officeList = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                .eq(JsSysOffice::getStatus, OaConstant.NO));
        Map<String, String> officeMap = officeList.stream().collect(Collectors.toMap(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName));
        //获取oa部门对应数据
        List<SysConfigurationDTO> oaMesDept = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByType("OA_MES_DEPT"), FeignConstant.STECCN_MES_SERVICE_NAME).get();
        Map<String, String> oaMesDeptMap = oaMesDept.stream().collect(Collectors.toMap(SysConfigurationDTO::getContent, SysConfigurationDTO::getRefContent));

        //开始操作
        for(OaUserVO oaUser:oaUserList){
            //先看mes中是否有人
            String userCode = mesUserMap.get(oaUser.getUserName());
            if(StrUtil.isBlank(userCode)){
                unUsedMesUser.add(oaUser.getUserName());
            }else {
                usedMesUser.add(userCode);
            }
        }
        return "";
    }
}




