package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.steccn.platform.dto.MesOfficeDTO;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Sets;
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
    private final RemoteMesFeignApi remoteMesFeignApi;

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
    public List<List<String>> getEmpTree(Set<String> userName) {
        List<MesUserInfoVO> mesUserInfo = jsSysEmployeeMapper.selectEmpInfo(userName);
        List<List<String>> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(mesUserInfo)){
            String depCode = mesUserInfo.get(0).getPostCode();
            JsSysOffice node = jsSysOfficeService.getOne(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getOfficeCode, depCode)
                    .eq(JsSysOffice::getStatus, OaConstant.NO.toString())
                    .last("LIMIT 1"));
            List<JsSysOffice> tree = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                    .eq(JsSysOffice::getParentCode, node.getParentCode())
                    .eq(JsSysOffice::getStatus,OaConstant.NO.toString())
                    .orderByDesc(JsSysOffice::getTreeSort));
            for(JsSysOffice tmp:tree){
                List<String> list = Lists.newArrayList(Arrays.asList(tmp.getParentCodes().split(",")));
                list = list.subList(2,list.size());
                list.add(tmp.getOfficeCode());
                results.add(list);
            }
        }
        return results;
    }

    @Override
    public Map<String, List<String>> getEmpByDepTree(Set<String> depCode) {
        Map<String,List<String>> results = Maps.newHashMap();
        if(CollUtil.isNotEmpty(depCode)){
            for(String tmp:depCode){
                List<JsSysOffice> office = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                        .like(JsSysOffice::getParentCodes, tmp + ",")
                        .eq(JsSysOffice::getStatus, "0"));
                Set<String> officeCodes = Sets.newHashSet();
                if(CollUtil.isNotEmpty(office)){
                    officeCodes  = office.stream().map(JsSysOffice::getOfficeCode).collect(Collectors.toSet());
                }
                officeCodes.add(tmp);
                List<JsSysEmployee> employeeList = list(new LambdaQueryWrapper<JsSysEmployee>()
                        .in(JsSysEmployee::getOfficeCode, officeCodes));
                if(CollUtil.isNotEmpty(employeeList)){
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
        StringBuilder result = new StringBuilder();
        List<OaUserVO> oaUserList = oaInfoService.getNormalUser().getData();
        List<OaUserVO> noNameList = oaUserList.stream().filter(v->Objects.isNull(v.getUserName())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(noNameList)){
            for(OaUserVO tmp:noNameList){
                result.append(tmp.getName()+"用户名为空;");
            }
        }

        oaUserList = oaUserList.stream().filter(v->Objects.nonNull(v.getUserName())).collect(Collectors.toList());
        List<JsSysUser> mesUser = jsSysUserService.list(new LambdaQueryWrapper<JsSysUser>()
                        .select(JsSysUser::getLoginCode,JsSysUser::getUserCode,JsSysUser::getUserName)
                        .eq(JsSysUser::getStatus, OaConstant.NO.toString()));
        Map<String, String> mesUserMap = mesUser.stream().collect(Collectors.toMap(JsSysUser::getLoginCode, JsSysUser::getUserCode));
        //mes中已经有的人
        List<String> usedMesUser = Lists.newArrayList();
        List<JsSysEmployee> mesEmployeeList = jsSysEmployeeMapper.selectList(new LambdaQueryWrapper<JsSysEmployee>()
                .select(JsSysEmployee::getEmpCode,JsSysEmployee::getOfficeCode,JsSysEmployee::getOfficeName)
                .in(JsSysEmployee::getEmpCode, mesUser.stream().map(JsSysUser::getUserCode).collect(Collectors.toSet())));
        Map<String, String> mesEmployeeMap = mesEmployeeList.stream().collect(Collectors.toMap(JsSysEmployee::getEmpCode, JsSysEmployee::getOfficeCode));
        //获取部门数据
        List<JsSysOffice> officeList = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                .eq(JsSysOffice::getStatus, OaConstant.NO));
        //获取oa部门对应数据
        List<SysConfigurationDTO> oaMesDept = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByType("OA_MES_DEPT"), FeignConstant.STECCN_MES_SERVICE_NAME).get();
        Map<String, String> oaMesDeptMap = oaMesDept.stream().collect(Collectors.toMap(SysConfigurationDTO::getContent, SysConfigurationDTO::getRefContent));

        //开始操作
        for(OaUserVO oaUser:oaUserList){
            //先看mes中是否有人
            String userCode = mesUserMap.get(oaUser.getUserName());
            if(StrUtil.isBlank(userCode)){
                //mes中没有，新增
                MesUserDTO mesUserDTO = new MesUserDTO();
                mesUserDTO.setLoginCode(oaUser.getUserName());
                mesUserDTO.setUserName(oaUser.getName());
                mesUserDTO.setIsNewRecord(true);
                String oaOfficeId = oaMesDeptMap.get(oaUser.getDepartmentId().toString());
                JsSysOffice officeTmp = officeList.stream().filter(v -> v.getParentCode().equals(oaOfficeId)&&v.getOfficeName().equals("岗位")).findFirst().orElse(null);
                if(Objects.isNull(officeTmp)){
                    result.append(oaOfficeId+"不存在;");
                    continue;
                }
                JsSysOffice officeTmp2 = officeList.stream().filter(v -> v.getParentCode().equals(officeTmp.getOfficeCode())
                        && v.getOfficeName().equals(oaUser.getJobTitleName())).findFirst().orElse(null);
                if(Objects.isNull(officeTmp2)){
                    result.append(oaOfficeId+"部门下"+oaUser.getJobTitleName()+"不存在;");
                    continue;
                }
                mesUserDTO.setOfficeCode(officeTmp2.getOfficeCode());
                mesUserDTO.setOfficeName(officeTmp2.getOfficeName());
                log.debug(oaUser.getUserName());
//                String post = HttpUtil.post("http://192.168.16.252:8080/web/user/save", JSONObject.toJSONString(mesUserDTO));
//                String s = remoteMesFeignApi.saveUser(mesUserDTO);
                result.append(oaUser.getUserName()+"mes中不存在该用户");
            }else {
                usedMesUser.add(userCode);
                //对比officeCode
                String officeCode = mesEmployeeMap.get(userCode);
                String oaOfficeId = oaMesDeptMap.get(oaUser.getDepartmentId().toString());
                JsSysOffice officeTmp = officeList.stream().filter(v -> v.getParentCode().equals(oaOfficeId)&&v.getOfficeName().equals("岗位")).findFirst().orElse(null);
                if(Objects.isNull(officeTmp)){
                    result.append("部门"+oaUser.getDepartmentId().toString()+"不存在");
                    result.append("部门"+ oaOfficeId+"不存在;");
                    continue;
                }
                JsSysOffice officeTmp2 = officeList.stream().filter(v -> v.getParentCode().equals(officeTmp.getOfficeCode())
                        && v.getOfficeName().equals(oaUser.getJobTitleName())).findFirst().orElse(null);
                if(Objects.isNull(officeTmp2)){
                    result.append(oaOfficeId+"部门下"+oaUser.getJobTitleName()+"不存在;");
                    continue;
                }
                log.debug(userCode);
                if(!officeCode.equals(officeTmp2.getOfficeCode())){
                    JsSysEmployee employeeTmp = new JsSysEmployee();
                    employeeTmp.setOfficeCode(officeTmp2.getOfficeCode());
                    employeeTmp.setOfficeName(officeTmp2.getOfficeName());
                    jsSysEmployeeMapper.update(employeeTmp,new LambdaQueryWrapper<JsSysEmployee>().eq(JsSysEmployee::getEmpCode,userCode));
                }

            }

        }
        mesUser = mesUser.stream().filter(v->!usedMesUser.contains(v.getUserCode())).collect(Collectors.toList());
        for(JsSysUser user:mesUser){
            if(user.getUserCode().equals("system")||user.getUserCode().equals("admin")){
                continue;
            }
            jsSysEmployeeMapper.delete(new LambdaQueryWrapper<JsSysEmployee>().eq(JsSysEmployee::getEmpCode,user.getUserCode()));
            JsSysUser userTmp = new JsSysUser();
            userTmp.setStatus(OaConstant.NUM_1.toString());
            jsSysUserService.update(userTmp,new LambdaQueryWrapper<JsSysUser>()
                    .eq(JsSysUser::getUserCode,user.getUserCode()));
        }
        return result.toString();
    }
}




