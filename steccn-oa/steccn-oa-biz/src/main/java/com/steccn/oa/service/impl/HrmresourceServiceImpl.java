package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.*;
import com.steccn.oa.dto.OaHrmresourceDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.mapper.HrmdepartmentMapper;
import com.steccn.oa.mapper.HrmjobtitlesMapper;
import com.steccn.oa.mapper.HrmresourceMapper;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.service.HrmjobtitlesService;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.vo.OaEmpVO;
import com.steccn.oa.vo.OaMesUserVO;
import com.steccn.oa.vo.OaUserVO;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【hrmresource】的数据库操作Service实现
 * @createDate 2022-09-01 18:37:02
 */
@Service
@RequiredArgsConstructor
public class HrmresourceServiceImpl extends ServiceImpl<HrmresourceMapper, Hrmresource>
        implements HrmresourceService {

    private final KqGroupmemberMapper kqGroupmemberMapper;

    private final RemoteMesFeignApi remoteMesFeignApi;

    private final HrmdepartmentMapper hrmdepartmentMapper;

    private final HrmjobtitlesMapper hrmjobtitlesMapper;
    private final HrmresourceMapper hrmresourceMapper;
    private final HrmjobtitlesService hrmjobtitlesService;

    @Override
    public Long getIdByWorkCode(String workCode) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getWorkcode, workCode), false);
        if (Objects.isNull(hrmresource)) {
            return null;
        } else {
            return Long.valueOf(hrmresource.getId());
        }
    }

    @Override
    public OaUserIdAndName getNameByLoginId(String loginId) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getLoginid, loginId), false);
        OaUserIdAndName oaUserIdAndName = new OaUserIdAndName();
        if (!Objects.isNull(hrmresource)) {
            oaUserIdAndName.setName(hrmresource.getLastname());
            oaUserIdAndName.setId(hrmresource.getId());
            return oaUserIdAndName;
        }else {
            return null;
        }

    }

    @Override
    public List<OaUserIdAndName> getUserByName(String userName) {
        List<OaUserIdAndName> userList = Lists.newArrayList();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().like(Hrmresource::getLastname, userName));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(hrmresource -> {
                OaUserIdAndName oaUserIdAndName = new OaUserIdAndName();
                oaUserIdAndName.setName(hrmresource.getLastname());
                oaUserIdAndName.setId(hrmresource.getId());
                userList.add(oaUserIdAndName);
            });
        }
        return userList;
    }

    @Override
    public List<OaUserIdAndName> getUserByDepartmentIdAndJobTitle(Integer departmentId, Integer jobTitle) {
        List<OaUserIdAndName> userList = Lists.newArrayList();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getDepartmentid,departmentId).eq(Hrmresource::getJobtitle,jobTitle));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(hrmresource -> {
                OaUserIdAndName oaUserIdAndName = new OaUserIdAndName();
                oaUserIdAndName.setName(hrmresource.getLastname());
                oaUserIdAndName.setId(hrmresource.getId());
                userList.add(oaUserIdAndName);
            });
        }
        return userList;
    }

    @Override
    public Map<Long, String> listByIdCh(List<Long> idList) {
        Map<Long, String> map = Maps.newHashMap();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>()
                .in(Hrmresource::getId, idList)
                .select(Hrmresource::getId, Hrmresource::getLastname));
        if (CollUtil.isNotEmpty(list)) {
            for (Hrmresource hrmresource : list) {
                map.put(Long.valueOf(hrmresource.getId()), hrmresource.getLastname());
            }
        }
        return map;
    }

    @Override
    public Map<Long, OaHrmresourceDTO> listById(List<Long> idList) {
        Map<Long, OaHrmresourceDTO> map = Maps.newHashMap();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>()
                .in(Hrmresource::getId, idList)
                .select(Hrmresource::getId, Hrmresource::getLastname, Hrmresource::getMessagerurl, Hrmresource::getJobtitle));
        if (CollUtil.isNotEmpty(list)) {
            Set<Integer> jobTitleIdSet = list.stream().map(Hrmresource::getJobtitle).collect(Collectors.toSet());
            Map<Integer, String> jobMap = hrmjobtitlesService.listByIdCh(jobTitleIdSet);
            for (Hrmresource hrmresource : list) {
                OaHrmresourceDTO oaHrmresourceDTO = new OaHrmresourceDTO();
                oaHrmresourceDTO.setLastname(hrmresource.getLastname());
                if (StrUtil.isNotBlank(hrmresource.getMessagerurl())) {
                    oaHrmresourceDTO.setMessagerurl(OaConstant.OA_WEAVER_SITE + hrmresource.getMessagerurl());
                }
                oaHrmresourceDTO.setJobtitleName(jobMap.get(hrmresource.getJobtitle()));
                map.put(Long.valueOf(hrmresource.getId()), oaHrmresourceDTO);
            }
        }
        return map;
    }

    @Override
    public List<Integer> getIdByUserName(List<String> userNames) {
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().in(Hrmresource::getLoginid,userNames));
        List<Integer> ids = Lists.newArrayList();
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                ids.add(tmp.getId());
            });
        }
        return ids;
    }

    @Override
    public List<OaUserIdAndName> getIdByWorkCode(List<String> workCodes) {
        List<OaUserIdAndName> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(workCodes)){
            List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().in(Hrmresource::getWorkcode,workCodes));
            if(CollUtil.isNotEmpty(list)){
                list.forEach(tmp -> {
                    OaUserIdAndName result = new OaUserIdAndName();
                    result.setName(tmp.getLastname());
                    result.setId(tmp.getId());
                    result.setWorkcode(tmp.getWorkcode());
                    results.add(result);
                });
            }
        }
        return results;
    }

    @Override
    public String getLoginIdById(Integer id) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getId, id), false);
        if(Objects.isNull(hrmresource)){
            return null;
        }
        return hrmresource.getLoginid();
    }

    @Override
    public OaEmpVO getUserInfoById(Integer id) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getId, id), false);
        OaEmpVO result = new OaEmpVO();
        if(Objects.nonNull(hrmresource)){
            BeanUtil.copyProperties(hrmresource,result);
        }
        return result;
    }

    @Override
    public List<OaMesUserVO> getMesUser() {
        String data = remoteMesFeignApi.getAllUser();
        List<OaMesUserVO> oaMesUserVOS = JSONObject.parseArray(data, OaMesUserVO.class);
        List<String> loginName = oaMesUserVOS.stream().map(OaMesUserVO::getLoginCode).collect(Collectors.toList());
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>()
                .select(Hrmresource::getId, Hrmresource::getLoginid)
                .in(Hrmresource::getLoginid,loginName));
        //拿到所有考勤组
        List<KqGroupmember> kqGroupmembers = kqGroupmemberMapper.selectList(new LambdaQueryWrapper<KqGroupmember>()
                .select(KqGroupmember::getId,KqGroupmember::getGroupid,KqGroupmember::getTypevalue).isNull(KqGroupmember::getIsdelete));
        oaMesUserVOS.forEach(tmp->{
            Hrmresource one = list.stream().filter(v -> v.getLoginid().equals(tmp.getLoginCode())).findFirst().orElse(null);
            if(StrUtil.isNotBlank(tmp.getOfficeType())&&tmp.getOfficeType().equals(OaConstant.NUM_6.toString())){
                String code = tmp.getOfficeCode();
                tmp.setOfficeCode(null);
                tmp.setPostCode(code);
            }
            if(Objects.nonNull(one)){
                tmp.setOaId(one.getId());
                KqGroupmember memberTmp = kqGroupmembers.stream().filter(v -> v.getTypevalue().equals(one.getId()))
                        .sorted(Comparator.comparing(KqGroupmember::getId).reversed())
                        .findFirst().orElse(null);
                if(Objects.nonNull(memberTmp)){
                    tmp.setGroupId(memberTmp.getGroupid());
                }
            }
        });
        return oaMesUserVOS;
    }

    @Override
    public String synchronizeUser() {
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().ne(Hrmresource::getStatus, OaConstant.NUM_5));
        List<MesUserDTO> dto = Lists.newArrayList();
        List<Hrmjobtitles> hrmjobtitles = hrmjobtitlesMapper.selectList(new LambdaQueryWrapper<Hrmjobtitles>()
                .isNull(Hrmjobtitles::getCanceled));
        List<Hrmdepartment> hrmdepartments = hrmdepartmentMapper.selectList(new LambdaQueryWrapper<Hrmdepartment>()
                .isNull(Hrmdepartment::getCanceled));
        list.forEach(tmp->{
            MesUserDTO tmpDTO = new MesUserDTO();
            tmpDTO.setUserName(tmp.getLastname());
            tmpDTO.setLoginCode(tmp.getLoginid());
            //传值为岗位名称
            tmpDTO.setOfficeCode(hrmjobtitles.stream().filter(v->v.getId().equals(tmp.getJobtitle())).findFirst().get().getJobtitlename());
            //传值为部门名称
            Hrmdepartment hrmdepartment = hrmdepartments.stream().filter(v -> v.getId().equals(tmp.getDepartmentid())).findFirst().get();
            if(hrmdepartment.getDepartmentname().equals("深加工二车间")){
                hrmdepartment.setDepartmentname("深加工二车间（东线）");
            }
            if(hrmdepartment.getDepartmentname().equals("深加工三车间")){
                hrmdepartment.setDepartmentname("深加工三车间（西线）");
            }
            String parentName = "";
            if(!hrmdepartment.getSupdepid().equals(0)){
                Hrmdepartment hrmParentDepartment = hrmdepartments.stream().filter(v -> v.getId().equals(hrmdepartment.getSupdepid())).findFirst().get();
                if(hrmParentDepartment.getDepartmentname().equals("深加工二车间")){
                    hrmParentDepartment.setDepartmentname("深加工二车间（东线）");
                }
                if(hrmParentDepartment.getDepartmentname().equals("深加工三车间")){
                    hrmParentDepartment.setDepartmentname("深加工三车间（西线）");
                }
                parentName = hrmParentDepartment.getDepartmentname();
            }
            tmpDTO.setOfficeName(parentName+"/"+hrmdepartment.getDepartmentname());
            dto.add(tmpDTO);
        });
        return remoteMesFeignApi.synchronizeUser(dto);
    }


    @Override
    public List<OaUserVO> getAllOaUser(TripleDTO<LocalDateTime,LocalDateTime, Set<String>> dto) {
        List<OaUserVO> oaUserVOS = hrmresourceMapper.selectJoinList(OaUserVO.class,
                new MPJLambdaWrapper<OaUserVO>()
                        .selectAs(Hrmresource::getId, OaUserVO::getId)
                        .selectAs(Hrmresource::getLastname, OaUserVO::getName)
                        .selectAs(Hrmresource::getJobtitle, OaUserVO::getJobTitleId)
                        .selectAs(Hrmresource::getLoginid,OaUserVO::getUserName)
                        .selectAs(Hrmjobtitles::getJobtitlename, OaUserVO::getJobTitleName)
                        .selectAs(Hrmresource::getDepartmentid, OaUserVO::getDepartmentId)
                        .selectAs(Hrmresource::getWorkcode,OaUserVO::getWorkCode)
                        .selectAs(Hrmresource::getStatus,OaUserVO::getStatus)
                        .selectAs(Hrmdepartment::getDepartmentname, OaUserVO::getDepartmentName)
                        .leftJoin(Hrmjobtitles.class, Hrmjobtitles::getId, Hrmresource::getJobtitle)
                        .leftJoin(Hrmdepartment.class, Hrmdepartment::getId, Hrmresource::getDepartmentid)
//                        .ne(Hrmresource::getStatus, OaConstant.NUM_5)
                        .isNull(Hrmjobtitles::getCanceled)
                        .notIn(CollUtil.isNotEmpty(dto.getField3()),Hrmresource::getCertificatenum,dto.getField3())
                        .le(Hrmresource::getModified, dto.getField2().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .ge(Hrmresource::getModified, dto.getField1().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .orderByDesc(Hrmresource::getModified));
        oaUserVOS = oaUserVOS.stream().filter(v->StrUtil.isNotBlank(v.getUserName())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(oaUserVOS)){
            List<KqGroupmember> kqGroupmembers = kqGroupmemberMapper.selectList(new LambdaQueryWrapper<KqGroupmember>()
                    .isNull(KqGroupmember::getIsdelete)
                    .in(KqGroupmember::getTypevalue, oaUserVOS.stream().map(OaUserVO::getId).collect(Collectors.toSet())));
            oaUserVOS.forEach(tmp -> {
                KqGroupmember kqGroupmember = kqGroupmembers.stream()
                        .filter(v -> v.getTypevalue().equals(tmp.getId()))
                        .max(Comparator.comparing(KqGroupmember::getId))
                        .orElse(new KqGroupmember());
                tmp.setGroupId(kqGroupmember.getGroupid());
            });

        }
        return oaUserVOS;
    }

    @Override
    public List<OaUserVO> getAllNormalOaUser() {
        List<OaUserVO> oaUserVOS = hrmresourceMapper.selectJoinList(OaUserVO.class,
                new MPJLambdaWrapper<OaUserVO>()
                        .selectAs(Hrmresource::getId, OaUserVO::getId)
                        .selectAs(Hrmresource::getLastname, OaUserVO::getName)
                        .selectAs(Hrmresource::getJobtitle, OaUserVO::getJobTitleId)
                        .selectAs(Hrmresource::getLoginid,OaUserVO::getUserName)
                        .selectAs(Hrmjobtitles::getJobtitlename, OaUserVO::getJobTitleName)
                        .selectAs(Hrmresource::getDepartmentid, OaUserVO::getDepartmentId)
                        .selectAs(Hrmresource::getWorkcode,OaUserVO::getWorkCode)
                        .selectAs(Hrmresource::getStatus,OaUserVO::getStatus)
                        .selectAs(Hrmdepartment::getDepartmentname, OaUserVO::getDepartmentName)
                        .leftJoin(Hrmjobtitles.class, Hrmjobtitles::getId, Hrmresource::getJobtitle)
                        .leftJoin(Hrmdepartment.class, Hrmdepartment::getId, Hrmresource::getDepartmentid)
                        .notIn(Hrmresource::getStatus, Sets.newHashSet(4,5,6))
//                        .ne(Hrmresource::getStatus, OaConstant.NUM_5)
                        .isNull(Hrmjobtitles::getCanceled));
        return oaUserVOS;
    }

    @Override
    public OaUserVO getOaUserByLoginCode(String loginCode) {
        Hrmresource one = getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getLoginid, loginCode).last("LIMIT 1"));
        OaUserVO result = new OaUserVO();
        if(Objects.nonNull(one)){
            result.setId(one.getId());
            result.setDepartmentId(one.getDepartmentid());
            result.setName(one.getLastname());
            result.setJobTitleId(one.getJobtitle());
        }
        return result;
    }

    @Override
    public List<Integer> getUserIdByJobTitleId(Set<Integer> jobTitles) {
        List<Integer> results = Lists.newArrayList();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>()
                .in(Hrmresource::getJobtitle,jobTitles));
        if(CollUtil.isNotEmpty(list)){
            results = list.stream().map(Hrmresource::getId).collect(Collectors.toList());
        }
        return results;
    }

    @Override
    public Map<String, LocalDate> getCompanyStartTime(Set<String> loginCode) {
        Map<String,LocalDate> results = Maps.newHashMap();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().select(Hrmresource::getCreatedate, Hrmresource::getLoginid)
                .in(Hrmresource::getLoginid, loginCode).isNotNull(Hrmresource::getCreatedate));
        if(CollUtil.isNotEmpty(list)){
            results = list.stream().collect(Collectors.toMap(
                    Hrmresource::getLoginid,
                    v->LocalDate.parse(v.getCreatedate())
            ));
        }
        return results;
    }

    @Override
    public List<String> getAllUserName() {
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>());
        return list.stream().map(Hrmresource::getLoginid).collect(Collectors.toList());
    }
}




