package com.steccn.oa.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.*;
import com.steccn.oa.service.*;
import com.steccn.oa.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * oaFeign
 *
 * @author Mongknife
 * @date 2023/2/17
 */
@RestController
@Api("oaFeign")
@RequestMapping("/feign/oa")
@RequiredArgsConstructor
@Slf4j
public class OaFeignController {

    private final HrmresourceService hrmresourceService;

    private final WorkflowCurrentoperatorService workflowCurrentoperatorService;

    private final KqGroupmemberService kqGroupmemberService;

    private final OaService oaService;

    private final HrmdepartmentService hrmdepartmentService;

    private final HrmjobtitlesService hrmjobtitlesService;

    private final HrmClassInfoService hrmClassInfoService;

    private final WorkflowRequestlogService workflowRequestlogService;
    @PostMapping("/user/ch")
    @ApiOperation("根据主键id查询oa用户的中文名称")
    public R<Map<Long, String>> listByIdCh(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(hrmresourceService.listByIdCh(idList));
    }

    @PostMapping("/user")
    @ApiOperation("根据主键id查询oa用户的信息")
    public R<Map<Long, OaHrmresourceDTO>> listById(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(hrmresourceService.listById(idList));
    }

    @ApiOperation("获取流程当前节点的操作人")
    @PostMapping("/workflow/currentOperator")
    public R<OaWorkFlowOperatorDTO> getCurrentOperator(@RequestBody OaOperatorParamDTO dto){
        return R.success(workflowCurrentoperatorService.getUserIdByRequestId(dto));
    }

    @ApiOperation("根据userId查询考勤组")
    @PostMapping("/user/group")
    public R<List<OaGroupIdAndNameVO>> getGroupByUser(@RequestBody Set<Long> userId){
        return R.success(kqGroupmemberService.findGroupByUserId(userId));
    }

    @ApiOperation("根据Id查询用户名")
    @GetMapping("/user/loginid")
    public R<String> getLoginIdById(@RequestParam("id") Integer id){
        return R.success(hrmresourceService.getLoginIdById(id));
    }

    @ApiOperation("获取所有用户名称")
    @GetMapping("/all/user")
    public R<List<String>> getAllUserName(){
        return R.success(hrmresourceService.getAllUserName());
    }

    @ApiOperation("根据Id查询用户信息")
    @GetMapping("/user/info/id")
    public R<OaEmpVO> getUserInfoById(@RequestParam("id") Integer id){
        return R.success(hrmresourceService.getUserInfoById(id));
    }

    @ApiOperation("保存文档")
    @GetMapping("/workflow/saveDoc")
    public R<OaSaveDocVO> saveDoc(@RequestParam("url") String url, @RequestParam("docName") String docName){
        return R.success(oaService.saveDoc(url,docName));
    }

    @ApiOperation("根据部门id获取信息")
    @GetMapping("/department/info")
    public R<OaDepartmentVO> getDepartmentInfoById(@RequestParam("id") Integer id){
        return R.success(hrmdepartmentService.getOaDepartmentById(id));
    }

    @ApiOperation("根据岗位id获取岗位信息")
    @GetMapping("/jobTitle/info")
    public R<OaJobTitleVO> getJobInfoById(@RequestParam("id") Integer id){
        return R.success(hrmjobtitlesService.getJobInfoById(id));
    }

    @ApiOperation("获取班组长信息")
    @PostMapping("/group/leader")
    public R<Map<Integer,OaGroupLeaderDTO>> getLeaderInfoByGroup(@RequestBody Set<Integer> groupIds){
        return R.success(kqGroupmemberService.getLeaderByGroupId(groupIds));
    }

    @ApiOperation("获取班次对应的班组")
    @PostMapping("/group/count")
    public R<Map<String, OaGroupCountDTO>> getGroupInfo(@RequestBody TupleDTO<LocalDate,LocalDate> dateDTO){
        return R.success(oaService.getClassGroupMap(dateDTO));
    }

    @ApiOperation("获取班次对应的班组(基片)")
    @PostMapping("/group/count/jp")
    public R<Map<String, OaGroupCountDTO>> getGroupInfoJp(@RequestBody TripleDTO<LocalDate,LocalDate,Integer> dateDTO){
        return R.success(oaService.getClassGroupMapJp(dateDTO));
    }

    @ApiOperation("获取班组长信息")
    @PostMapping("/group/workshop/leader")
    public R<Map<String, List<OaClassUserNameDTO>>> getLeaderName(@RequestBody Set<String> productWorkShop ){
        return R.success(hrmdepartmentService.getClassUserName(productWorkShop));
    }

    @ApiOperation("获取用户班组班次信息")
    @PostMapping("/user/class")
    public R<OaClassVO> getUserClass(@RequestBody TupleDTO<LocalDate,Integer> dto){
        return R.success(hrmClassInfoService.getClassByUserId(dto));
    }

    @PostMapping("/thirdParty/allDepartment")
    @ApiOperation(value = "获取所有部门")
    public R<List<OaDepartmentVO>> getAllDepartment(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(hrmdepartmentService.getAllDepartment(dto));
    }

    @PostMapping("/thirdParty/allJobTitle")
    @ApiOperation(value = "获取所有岗位")
    public R<List<OaJobTitleVO>> getAllJobTitle(HttpServletRequest request, @RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(hrmjobtitlesService.getAllJobTitle(dto));
    }

    @ApiOperation("获取当前节点操作人")
    @PostMapping("/workflow/operator")
    public R<OaUserIdAndName> getOperator(@RequestBody TupleDTO<Integer,Integer> dto){
        return R.success(workflowRequestlogService.getOperator(dto));
    }

    @ApiOperation("获取当前节点的所有操作人")
    @PostMapping("/workflow/operator/users")
    public R<Set<Integer>> getWorkflowUsers(@RequestBody TupleDTO<String,String> dto){
        return R.success(workflowCurrentoperatorService.getUserByRequestIdAndNodeId(dto));
    }

    @ApiOperation("获取当班信息")
    @PostMapping("/todayClass/info")
    public R<List<OaTodayClassVO>> getTodayClassInfo(@RequestBody TupleDTO<String,LocalDate> dto){
        return R.success(kqGroupmemberService.getTodayClassInfo(dto.getField1(),dto.getField2()));
    }

    @ApiOperation("获取当班信息")
    @PostMapping("/jobTitle/id")
    public R<List<Integer>> getJobTitleById(@RequestBody Set<Integer> jobTitles){
        return R.success(hrmresourceService.getUserIdByJobTitleId(jobTitles));
    }

    @ApiOperation("获取用户入职时间")
    @PostMapping("/user/workStartTime")
    public R<Map<String, LocalDate>> getCompanyStartTime(@RequestBody Set<String> loginCode){
        return R.success(hrmresourceService.getCompanyStartTime(loginCode));
    }

    @GetMapping("/user/getNormalUser")
    @ApiOperation(value = "获取oa普通用户")
    public R<List<OaUserVO>> getNormalUser() {
        return R.success(hrmresourceService.getAllNormalOaUser());
    }
}
