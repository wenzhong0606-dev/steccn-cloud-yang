package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.domain.KqGroupmember;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.mapper.KqGroupMapper;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.mapper.HrmresourceMapper;
import com.steccn.oa.service.OaService;
import com.steccn.oa.vo.OaClassVO;
import com.steccn.oa.vo.OaMesUserVO;
import com.steccn.oa.vo.OaRosterRecordVO;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final OaService oaService;

    private final KqGroupMapper kqGroupMapper;

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
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().in(Hrmresource::getId, idList));
        if (CollUtil.isNotEmpty(list)){
            if (CollUtil.isNotEmpty(list)) {
                for (Hrmresource hrmresource : list) {
                    map.put(Long.valueOf(hrmresource.getId()), hrmresource.getLastname());
                }
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
        list.forEach(tmp->{
            MesUserDTO tmpDTO = new MesUserDTO();
            tmpDTO.setUserName(tmp.getLastname());
            tmpDTO.setLoginCode(tmp.getLoginid());
            tmpDTO.setOfficeCode("Unallocated");
            tmpDTO.setOfficeName("待分配部门");
            tmpDTO.setIsNewRecord(true);
            dto.add(tmpDTO);
        });
        return remoteMesFeignApi.synchronizeUser(dto);
    }

    @Override
    public OaClassVO getClassByUserId(TupleDTO<LocalDate, Integer> dto) {
        if(Objects.isNull(dto.getField1())||Objects.isNull(dto.getField2())){
            throw new BusinessException("必传值不能为空");
        }
        OaClassVO result = new OaClassVO();
        //先查询人相关id
        Hrmresource hrmresource = getOne(new LambdaQueryWrapper<Hrmresource>().select(Hrmresource::getLastname, Hrmresource::getJobtitle,Hrmresource::getWorkcode)
                .eq(Hrmresource::getId, dto.getField2()), false);
        if(Objects.isNull(hrmresource)){
            throw new BusinessException("该人员不存在");
        }
        result.setUserId(dto.getField2());
        result.setUserName(hrmresource.getLastname());
        //班组信息
        KqGroupmember groupId = kqGroupmemberMapper.selectOne(new LambdaQueryWrapper<KqGroupmember>()
                .select(KqGroupmember::getGroupid)
                .isNull(KqGroupmember::getIsdelete)
                .eq(KqGroupmember::getTypevalue, dto.getField2())
                .last("LIMIT 1"));
        if(Objects.nonNull(groupId)){
            KqGroup kqGroup = kqGroupMapper.selectOne(new LambdaQueryWrapper<KqGroup>()
                    .select(KqGroup::getGroupname)
                    .eq(KqGroup::getId, groupId.getGroupid())
                    .last("LIMIT 1"));
            if(Objects.nonNull(kqGroup)){
                result.setClassGroup(kqGroup.getGroupname());
            }
        }
        if(Objects.nonNull(hrmresource.getJobtitle())){
            OaRosterRecordDTO recordDTO = new OaRosterRecordDTO();
            recordDTO.setStartdate(dto.getField1().toString());
            recordDTO.setEnddate(dto.getField2().toString());
            recordDTO.setJobtitle(hrmresource.getJobtitle().toString());
            List<OaRosterRecordVO> rosterRecord = oaService.getRosterRecord(recordDTO);
            OaRosterRecordVO oaRosterRecordVO = rosterRecord.stream().filter(v -> v.getWorkcode().equals(hrmresource.getWorkcode())).findFirst().orElse(null);
            if(Objects.nonNull(oaRosterRecordVO)){
                result.setClassGroup(oaRosterRecordVO.getSerial());
            }
        }
        return result;
    }
}




