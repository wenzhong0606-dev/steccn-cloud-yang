package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.domain.*;
import com.steccn.oa.dto.OaGroupDTO;
import com.steccn.oa.dto.OaGroupLeaderDTO;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.mapper.HrmresourceMapper;
import com.steccn.oa.mapper.KqFormatDetailMapper;
import com.steccn.oa.mapper.KqGroupMapper;
import com.steccn.oa.service.KqGroupService;
import com.steccn.oa.service.KqGroupmemberService;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.service.KqMesGroupLeaderService;
import com.steccn.oa.vo.OaGroupIdAndNameVO;
import com.steccn.oa.vo.OaRosterRecordVO;
import com.steccn.oa.vo.OaTodayClassVO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final KqGroupService kqGroupService;

    private final OaInfoService oaInfoService;

    private final KqMesGroupLeaderService kqMesGroupLeaderService;

    private final KqFormatDetailMapper kqFormatDetailMapper;

    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    private final HrmresourceMapper hrmresourceMapper;

    @Override
    public List<OaGroupIdAndNameVO> findGroupByUserId(Set<Long> userId) {
        if(CollUtil.isEmpty(userId)){
            throw new BusinessException("userId不能为空");
        }
        List<OaGroupIdAndNameVO> result = Lists.newArrayList();
        MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<OaGroupDTO>()
                .selectAs(KqGroupmember::getTypevalue,OaGroupDTO::getUserId)
                .selectAs(KqGroupmember::getGroupid,OaGroupDTO::getGroupId)
                .selectAs(KqGroup::getGroupname,OaGroupDTO::getGroupName)
                .leftJoin(KqGroup.class,KqGroup::getId,KqGroupmember::getGroupid)
                .isNull(KqGroupmember::getIsdelete)
                .in(KqGroupmember::getTypevalue,userId)
                .orderByDesc(KqGroupmember::getId);
        List<OaGroupDTO> oaGroupDTOS = kqGroupmemberMapper.selectJoinList(OaGroupDTO.class, mpjLambdaWrapper);
        oaGroupDTOS.forEach(tmp -> {
            OaGroupIdAndNameVO vo = new OaGroupIdAndNameVO();
            vo.setClassGroupId(tmp.getGroupId());
            vo.setClassGroupName(tmp.getGroupName());
            vo.setUserId(tmp.getUserId());
            result.add(vo);
        });
        return result;
    }

    @Override
    public OaGroupIdAndNameVO getOneGroupByUserId() {
        OaGroupIdAndNameVO result = new OaGroupIdAndNameVO();
        Set<Long> userIdSet = Sets.newHashSet();
        userIdSet.add(SecurityUtils.getUser().getUserId());
        List<OaGroupIdAndNameVO> resultList = this.findGroupByUserId(userIdSet);
        if(CollUtil.isEmpty(resultList)){
            return result;
        }
        //修改映射关系
//        resultList = changeGroupRelation(resultList);
        result = resultList.get(0);
        //映射关系修改
        if(result.getClassGroupName().contains(OaConstant.CLASS_A)){
            result.setClassGroupName(OaConstant.CLASS_A);
        }else if(result.getClassGroupName().contains(OaConstant.CLASS_B)){
            result.setClassGroupName(OaConstant.CLASS_B);
        }else if(result.getClassGroupName().contains(OaConstant.CLASS_C)){
            result.setClassGroupName(OaConstant.CLASS_C);
        }
        return result;
    }

    @Override
    public List<OaGroupIdAndNameVO> getABCGroup() {
        List<OaGroupIdAndNameVO> result = Lists.newArrayList();
        //放入具体值
        Set<String> oaClassName = Sets.newHashSet();
        for (OaManagerConstant.OaTeamGroupMapping value : OaManagerConstant.OaTeamGroupMapping.values()) {
            oaClassName.add(value.getOaName());
        }
        List<KqGroup> list = kqGroupService.list(new LambdaQueryWrapper<KqGroup>().in(KqGroup::getGroupname, oaClassName));
        for(KqGroup tmp:list){
            OaGroupIdAndNameVO vo = new OaGroupIdAndNameVO();
            vo.setClassGroupId(tmp.getId());
            vo.setClassGroupName(tmp.getGroupname());
            result.add(vo);
        }
        //修改有映射关系
        result = changeGroupRelation(result);
        return result;
    }

    @Override
    public Map<Integer, OaGroupLeaderDTO> getLeaderByGroupId(Set<Integer> groupIds) {
        Map<Integer, OaGroupLeaderDTO> resultMap = MapUtil.newHashMap();
        if(CollUtil.isEmpty(groupIds)){
            return resultMap;
        }
        MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<OaGroupLeaderDTO>()
                .selectAs(Hrmresource::getId,OaGroupLeaderDTO::getUserId)
                .selectAs(Hrmresource::getLastname,OaGroupLeaderDTO::getName)
                .selectAs(KqGroupmember::getGroupid,OaGroupLeaderDTO::getGroupId)
                .leftJoin(Hrmresource.class,Hrmresource::getId,KqGroupmember::getTypevalue)
                .leftJoin(Hrmjobtitles.class,Hrmjobtitles::getId,Hrmresource::getJobtitle)
                .isNull(KqGroupmember::getIsdelete)
                .in(KqGroupmember::getGroupid,groupIds)
                .like(Hrmjobtitles::getJobtitlename, OaConstant.GROUP_LEADER);
        List<OaGroupLeaderDTO> result = kqGroupmemberMapper.selectJoinList(OaGroupLeaderDTO.class,mpjLambdaWrapper);
        if(CollUtil.isNotEmpty(result)){
            //根据groupId转换成map
            resultMap = result.stream().collect(Collectors.toMap(OaGroupLeaderDTO::getGroupId, Function.identity(), (key1, key2) -> key2));
        }
        return resultMap;
    }

//    @Override
//    public List<OaTodayClassVO> getTodayClassInfo(String workshopName, LocalDate date) {
//        List<OaTodayClassVO> results = Lists.newArrayList();
//        //算当前班次
////        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(date);
//        OaRosterRecordDTO rosterDto = new OaRosterRecordDTO();
////        rosterDto.setStartdate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
////        rosterDto.setEnddate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
//        rosterDto.setStartdate(date.toString());
//        rosterDto.setEnddate(date.toString());
//        //获取熔化工
//        rosterDto.setJobtitle(OaConstant.JOB_RONGHUA_ID_1);
//        List<OaRosterRecordVO> oaRosterRecordVOS = Lists.newArrayList();
//        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//        rosterDto.setJobtitle(OaConstant.JOB_RONGHUA_ID_2);
//        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//        rosterDto.setJobtitle(OaConstant.JOB_RONGHUA_ID_3);
//        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//        Set<Long> userId = Sets.newHashSet();
//        //过滤车间
//        oaRosterRecordVOS = oaRosterRecordVOS.stream().filter(v->v.getDepartmentname().contains(workshopName)).collect(Collectors.toList());
//        //分为早班和晚班
//        for(int i = 0; i < 2;i++){
//            //过滤当前班次
//            OaTodayClassVO result = new OaTodayClassVO();
//            String classStr = "";
//            if(i==0){
//                result.setClassOrder("早班");
//                classStr = "白班";
//            } else {
//                result.setClassOrder("晚班");
//                classStr = "夜班";
//            }
//
////            if (qtTaskClassCalculate.getClassType().equals(0)) {
////                result.setClassOrder("早班");
////                classStr = "白班";
////            } else {
////                result.setClassOrder("晚班");
////                classStr = "夜班";
////            }
//            String finalClassStr = classStr;
//            List<OaRosterRecordVO> oaRosterRecordVosTmp = Lists.newArrayList();
//            oaRosterRecordVosTmp = oaRosterRecordVOS.stream().filter(v -> v.getSerial().equals(finalClassStr)).collect(Collectors.toList());
//            //避免班组重复
//            if(i==1&&CollUtil.isNotEmpty(results)){
//                oaRosterRecordVosTmp = oaRosterRecordVOS.stream().filter(v -> v.getSerial().equals(finalClassStr)
//                        &&!v.getDepartmentname().contains(results.get(0).getClassGroup())).collect(Collectors.toList());
//            }
//            if (CollUtil.isNotEmpty(oaRosterRecordVosTmp)) {
////                List<String> workCodes = Lists.newArrayList();
////                oaRosterRecordVosTmp.forEach(tmp -> {
////                    workCodes.add(tmp.getWorkcode());
////                });
////                //找到熔化工id
////                if (CollUtil.isNotEmpty(workCodes)) {
////                    List<OaUserIdAndName> oaUserIdAndNames = FeignOptional.of(() -> oaInfoService.getInfoByWorkCode(workCodes), FeignConstant.STECCN_OA_SERVICE_NAME).get();
////                    if (CollUtil.isNotEmpty(oaUserIdAndNames)) {
////                        oaUserIdAndNames.forEach(tmp -> {
////                            userId.add(tmp.getId().longValue());
////                        });
////                    }
////                }
//                String[] split = oaRosterRecordVosTmp.get(0).getDepartmentname().split(">");
//                String classGroupName = split[split.length - 1];
//                result.setClassGroup(classGroupName);
//                KqMesGroupLeader one = kqMesGroupLeaderService.getOne(new LambdaQueryWrapper<KqMesGroupLeader>().select()
//                        .eq(KqMesGroupLeader::getWorkshopName, workshopName)
//                        .eq(KqMesGroupLeader::getGroupName, classGroupName), false);
//                result.setClassLeader(one.getLeaderName());
//                results.add(result);
//            }
////            List<OaGroupIdAndNameVO> groupByUserId = this.findGroupByUserId(userId);
////            //拿到出现次数最多的班组
////            if(CollUtil.isNotEmpty(groupByUserId)){
////                String groupName = groupByUserId.stream()
////                        .collect(Collectors.groupingBy(OaGroupIdAndNameVO::getClassGroupName, Collectors.counting()))
////                        .entrySet().stream()
////                        .max(Map.Entry.comparingByValue())
////                        .map(Map.Entry::getKey)
////                        .orElse(null);
////                result.setClassGroup(groupName);
////            }
////            //获取班组长
////            String leader = "";
////            //拿到当班的熔化班长
////            rosterDto.setJobtitle(OaConstant.JOB_RONGHUABANZHANG_ID_1);
////            String leader1Name = getLeaderName(rosterDto);
////            if(StrUtil.isBlank(leader1Name)){
////                rosterDto.setJobtitle(OaConstant.JOB_RONGHUABANZHANG_ID_2);
////                leader1Name = getLeaderName(rosterDto);
////                if(StrUtil.isBlank(leader1Name)){
////                    rosterDto.setJobtitle(OaConstant.JOB_RONGHUABANZHANG_ID_3);
////                    leader1Name = getLeaderName(rosterDto);
////                }
////            }
////            leader = leader1Name;
////            //拿到当班的成型班长
////            rosterDto.setJobtitle(OaConstant.JOB_CHENGXINGBANZHANG_ID_1);
////            String leader2Name = getLeaderName(rosterDto);
////            if(StrUtil.isBlank(leader2Name)){
////                rosterDto.setJobtitle(OaConstant.JOB_CHENGXINGBANZHANG_ID_2);
////                leader2Name = getLeaderName(rosterDto);
////                if(StrUtil.isBlank(leader2Name)){
////                    rosterDto.setJobtitle(OaConstant.JOB_CHENGXINGBANZHANG_ID_3);
////                    leader2Name = getLeaderName(rosterDto);
////                }
////            }
////            if(StrUtil.isNotBlank(leader2Name)){
////                if(StrUtil.isBlank(leader)){
////                    leader = leader2Name;
////                }else {
////                    leader = leader + "/" +leader2Name;
////                }
////            }
////            //拿到当班的成品班长
////            rosterDto.setJobtitle(OaConstant.JOB_CHENGPINBANZHANG_ID);
////            String leader3Name = getLeaderName(rosterDto);
////            if(StrUtil.isNotBlank(leader3Name)){
////                if(StrUtil.isBlank(leader)){
////                    leader = leader3Name;
////                }else {
////                    leader = leader + "/" +leader3Name;
////                }
////            }
////            //拿到当班的成品班长
////            result.setClassLeader(leader);
////            results.add(result);
//        }
//        return results;
//    }
@Override
public List<OaTodayClassVO> getTodayClassInfo(String workshopName, LocalDate date) {
    List<OaTodayClassVO> results = Lists.newArrayList();
    //分为早班和晚班
    for(int i = 0; i < 2;i++){
        //过滤当前班次
        OaTodayClassVO result = new OaTodayClassVO();
        String classStr = "";
        if(i==0){
            result.setClassOrder("早班");
            classStr = "白班";
        } else {
            result.setClassOrder("晚班");
            classStr = "夜班";
        }
        String finalClassStr = classStr;
        LocalTime time = LocalTime.now();
        if(i==0){
            time = LocalTime.of(8,0);
        }else {
            time = LocalTime.of(20,0);
        }
        //基片班组的id为53,54,55
        KqFormatDetail kqFormatDetail = kqFormatDetailMapper.selectOne(new LambdaQueryWrapper<KqFormatDetail>()
                .eq(KqFormatDetail::getWorkbegindate, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .eq(KqFormatDetail::getWorkbegintime, time.toString())
                .in(KqFormatDetail::getGroupid, Sets.newHashSet(53, 54, 55))
                .last("LIMIT 1"));
        KqGroup kqGroup = kqGroupService.getOne(new LambdaQueryWrapper<KqGroup>()
                .eq(KqGroup::getId, kqFormatDetail.getGroupid()), false);
        String[] split = kqGroup.getGroupname().split("-");
        result.setClassGroup(split[1]);
        KqMesGroupLeader one = kqMesGroupLeaderService.getOne(new LambdaQueryWrapper<KqMesGroupLeader>()
                        .eq(KqMesGroupLeader::getWorkshopName, workshopName)
                        .eq(KqMesGroupLeader::getGroupName, split[1]), false);
        result.setClassLeader(one.getLeaderName());
        results.add(result);
    }
    return results;
}

    @Override
    public List<OaTodayClassVO> getTodayClassInfoSJG(String workshopName, LocalDate date) {
        List<OaTodayClassVO> results = Lists.newArrayList();
        workshopName = workshopName.substring(0,6);
        List<KqGroup> kqGroups = kqGroupService.list(new LambdaQueryWrapper<KqGroup>()
                .likeRight(KqGroup::getGroupname, workshopName)
                .orderByDesc(KqGroup::getId).last("LIMIT 3"));
        if(kqGroups.size()!=3){
            return results;
        }
        //分为早班和晚班
        for(int i = 0; i < 2;i++){
            //过滤当前班次
            OaTodayClassVO result = new OaTodayClassVO();
            String classStr = "";
            if(i==0){
                result.setClassOrder("早班");
                classStr = "白班";
            } else {
                result.setClassOrder("晚班");
                classStr = "夜班";
            }
            String finalClassStr = classStr;
            LocalTime time = LocalTime.now();
            if(i==0){
                time = LocalTime.of(8,0);
            }else {
                time = LocalTime.of(20,0);
            }
            //获取车间对应的考勤组
            KqFormatDetail kqFormatDetail = kqFormatDetailMapper.selectOne(new LambdaQueryWrapper<KqFormatDetail>()
                    .eq(KqFormatDetail::getWorkbegindate, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .eq(KqFormatDetail::getWorkbegintime, time.toString())
                    .in(KqFormatDetail::getGroupid, kqGroups.stream().map(KqGroup::getId).collect(Collectors.toSet()))
                    .last("LIMIT 1"));
            KqGroup kqGroup = kqGroups.stream().filter(v->v.getId().equals(kqFormatDetail.getGroupid())).findFirst().orElse(null);
            if(kqGroup.getGroupname().contains(OaConstant.CLASS_A)){
                result.setClassGroup(OaConstant.CLASS_A);
            }else if(kqGroup.getGroupname().contains(OaConstant.CLASS_B)){
                result.setClassGroup(OaConstant.CLASS_B);
            }else if(kqGroup.getGroupname().contains(OaConstant.CLASS_C)){
                result.setClassGroup(OaConstant.CLASS_C);
            }
            //获取班组长
            MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<OaGroupLeaderDTO>()
                    .selectAs(Hrmresource::getId,OaGroupLeaderDTO::getUserId)
                    .selectAs(Hrmresource::getLastname,OaGroupLeaderDTO::getName)
                    .selectAs(KqGroupmember::getGroupid,OaGroupLeaderDTO::getGroupId)
                    .leftJoin(Hrmresource.class,Hrmresource::getId,KqGroupmember::getTypevalue)
                    .leftJoin(Hrmjobtitles.class,Hrmjobtitles::getId,Hrmresource::getJobtitle)
                    .isNull(KqGroupmember::getIsdelete)
                    .eq(KqGroupmember::getGroupid,kqGroup.getId())
                    .eq(Hrmjobtitles::getJobtitlename, OaConstant.GROUP_LEADER);
            List<OaGroupLeaderDTO> groupLeader = kqGroupmemberMapper.selectJoinList(OaGroupLeaderDTO.class,mpjLambdaWrapper);
            if(CollUtil.isNotEmpty(groupLeader)){
                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < groupLeader.size(); j++){
                    if (j == 0){
                        sb.append(groupLeader.get(j).getName());
                    }else {
                        sb.append("/");
                        sb.append(groupLeader.get(j).getName());
                    }
                }
                result.setClassLeader(sb.toString());
            }
            results.add(result);
        }
        return results;
    }

    @Override
    public List<OaTodayClassVO> getTodayClassInfoYL(LocalDate date) {
        List<OaTodayClassVO> results = Lists.newArrayList();
        //分为早班和晚班
        for(int i = 0; i < 2;i++){
            //过滤当前班次
            OaTodayClassVO result = new OaTodayClassVO();
            String classStr = "";
            if(i==0){
                result.setClassOrder("早班");
                classStr = "白班";
            } else {
                result.setClassOrder("晚班");
                classStr = "夜班";
            }
            String finalClassStr = classStr;
            LocalTime time = LocalTime.now();
            if(i==0){
                time = LocalTime.of(8,0);
            }else {
                time = LocalTime.of(20,0);
            }
            //原料班组的id为56, 57, 58
            KqFormatDetail kqFormatDetail = kqFormatDetailMapper.selectOne(new LambdaQueryWrapper<KqFormatDetail>()
                    .eq(KqFormatDetail::getWorkbegindate, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .eq(KqFormatDetail::getWorkbegintime, time.toString())
                    .in(KqFormatDetail::getGroupid, Sets.newHashSet(56, 57, 58))
                    .last("LIMIT 1"));
            KqGroup kqGroup = kqGroupService.getOne(new LambdaQueryWrapper<KqGroup>()
                    .eq(KqGroup::getId, kqFormatDetail.getGroupid()), false);
            String[] split = kqGroup.getGroupname().split("-");
            result.setClassGroup(split[1]);
            KqMesGroupLeader one = kqMesGroupLeaderService.getOne(new LambdaQueryWrapper<KqMesGroupLeader>()
                    .eq(KqMesGroupLeader::getWorkshopName, "原料车间")
                    .eq(KqMesGroupLeader::getGroupName, split[1]), false);
            String leaderName = one.getLeaderName();
            if(date.getDayOfWeek().getValue()==7){
                leaderName = leaderName+"/从斌";
            }else {
                leaderName = leaderName+"/黄哲";
            }
            result.setClassLeader(leaderName);
            results.add(result);
        }
        return results;
    }

    @Override
    public List<OaTodayClassVO> getTodayClassInfoWarehouse(TupleDTO<Set<Integer>, LocalDate> dto) {
        List<OaTodayClassVO> results = Lists.newArrayList();
        Assert.notEmpty(dto.getField1(),"仓库不能为空");
        Assert.notNull(dto.getField2(),"日期不能为空");
        //分为早班和晚班
        for(int i = 0; i < 2;i++){
            //过滤当前班次
            OaTodayClassVO result = new OaTodayClassVO();
            String classStr = "";
            if(i==0){
                result.setClassOrder("早班");
                classStr = "白班";
            } else {
                result.setClassOrder("晚班");
                classStr = "夜班";
            }
            String finalClassStr = classStr;
            LocalTime time = LocalTime.now();
            if(i==0){
                time = LocalTime.of(8,0);
            }else {
                time = LocalTime.of(20,0);
            }
            //仓库班组的id为33,34,35
            KqFormatDetail kqFormatDetail = kqFormatDetailMapper.selectOne(new LambdaQueryWrapper<KqFormatDetail>()
                    .eq(KqFormatDetail::getWorkbegindate, dto.getField2().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .eq(KqFormatDetail::getWorkbegintime, time.toString())
                    .in(KqFormatDetail::getGroupid, Sets.newHashSet(33, 34, 35))
                    .last("LIMIT 1"));
            KqGroup kqGroup = kqGroupService.getOne(new LambdaQueryWrapper<KqGroup>()
                    .eq(KqGroup::getId, kqFormatDetail.getGroupid()), false);
            String[] split = kqGroup.getGroupname().split("-");
            result.setClassGroup(split[1]);
            //拿到班组中对应的人
            Map<String, String> oaMesMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.sysConfRef(new TupleDTO<>("OA_MES_WAREHOUSE", dto.getField1())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if(CollUtil.isNotEmpty(oaMesMap)){
                Set<Integer> departmentIds = oaMesMap.keySet().stream().map(Integer::parseInt).collect(Collectors.toSet());
                //部门下的所有人
                List<Hrmresource> hrmresources = hrmresourceMapper.selectList(new LambdaQueryWrapper<Hrmresource>()
                        .in(Hrmresource::getDepartmentid, departmentIds));
                //考情组下的人
                List<KqGroupmember> kqGroupmembers = kqGroupmemberMapper.selectList(new LambdaQueryWrapper<KqGroupmember>()
                        .eq(KqGroupmember::getGroupid, kqGroup.getId())
                        .isNull(KqGroupmember::getIsdelete));
                //考勤组的人要包括部门的人
                if(CollUtil.isNotEmpty(hrmresources)
                        &&CollUtil.isNotEmpty(kqGroupmembers)){
                    List<Integer> hrmIdList = hrmresources.stream().map(Hrmresource::getId).collect(Collectors.toList());
                    kqGroupmembers = kqGroupmembers.stream().filter(v->hrmIdList.contains(v.getTypevalue())).collect(Collectors.toList());
                    //查到姓名
                    if(CollUtil.isNotEmpty(kqGroupmembers)){
                        List<Hrmresource> hrmNames = hrmresourceMapper.selectList(new LambdaQueryWrapper<Hrmresource>()
                                .in(Hrmresource::getId, kqGroupmembers.stream().map(KqGroupmember::getTypevalue).collect(Collectors.toSet())));
                        result.setClassMemberName(hrmNames.stream().map(Hrmresource::getLastname).collect(Collectors.toList()));
                    }
                }
            }
            results.add(result);
        }
        return results;
    }


    /**
     * 修改映射关系
     * @param resultList
     * @return
     */
    private List<OaGroupIdAndNameVO> changeGroupRelation(List<OaGroupIdAndNameVO> resultList){
        if(CollUtil.isNotEmpty(resultList)){
            resultList.forEach(tmp->{
                String nameByOaName = OaManagerConstant.OaTeamGroupMapping.getNameByOaName(tmp.getClassGroupName());
                if(StrUtil.isNotEmpty(nameByOaName)){
                    tmp.setClassGroupName(nameByOaName);
                }
            });
        }
        return resultList;
    }

    /**
     * 获取对应的班组长名称
     *
     * @param rosterDto
     * @return
     */
    private String getLeaderName(OaRosterRecordDTO rosterDto){
        String result = "";
        List<OaRosterRecordVO> leader = Lists.newArrayList();
        leader.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
        if (CollUtil.isNotEmpty(leader)) {
            List<String> workCodes = Lists.newArrayList();
            leader.forEach(tmp -> {
                workCodes.add(tmp.getWorkcode());
            });
            //找到熔化班长名称
            if (CollUtil.isNotEmpty(workCodes)) {
                List<OaUserIdAndName> oaUserIdAndNames = FeignOptional.of(() -> oaInfoService.getInfoByWorkCode(workCodes), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                if (CollUtil.isNotEmpty(oaUserIdAndNames)) {
                    result = oaUserIdAndNames.get(0).getName();
                }
            }
        }
        return result;
    }
}




