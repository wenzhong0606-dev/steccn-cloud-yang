package com.steccn.oa.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.*;
import com.steccn.oa.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/08/31
 */
@FeignClient(contextId = "oaInfoService", value = FeignConstant.STECCN_OA_SERVICE_NAME)
public interface OaInfoService {
    /**
     * 校验用户名密码，获取userid
     *
     * @param oaLoginParam
     * @return R
     */
    @PostMapping("/oa/checkLogin")
    R checkLogin(@RequestBody OaLoginParam oaLoginParam);

    /**
     * 获取oa菜单
     *
     * @param userId
     * @return
     */

    @GetMapping("/oa/menuList")
    R getMenuList(@RequestParam("userId") String userId);

    /**
     * 获取应用token
     *
     * @param loginId
     * @return
     */
    @GetMapping("/oa/ssoToken")
    R getSsoToken(@RequestParam("loginId") String loginId);


    /**
     * 根据用户名查询用户
     *
     * @param username 用户id
     * @return
     */
    @GetMapping("/oa/username")
    R<OaUserDTO> loadByUsername(@RequestParam("username") String username);

    /**
     * 根据用户名查询用户信息
     *
     * @param loginId
     * @return
     */
    @GetMapping("/oa/userInfo")
    R<OaUserInfo> getOaUserInfo(@RequestParam("loginId") String loginId);

    /**
     * 根据工号查询id
     *
     * @param workCode
     * @return
     */
    @GetMapping("/oa/user/id/{workCode}")
    R<Long> getUserIdByWorkCode(@PathVariable("workCode") String workCode);


    /**
     * 根据主键id查询oa用户的中文名称
     * @param idList
     * @return
     */
    @PostMapping("/feign/oa/user/ch")
    @ApiOperation("根据主键id查询oa用户的中文名称")
    R<Map<Long, String>> listByIdCh(@RequestBody List<Long> idList);


    /**
     * 根据主键id查询oa用户的中文名称
     * @param idList
     * @return
     */
    @PostMapping("/feign/oa/user")
    @ApiOperation("根据主键id查询oa用户的中文名称")
    R<Map<Long, OaHrmresourceDTO>> listById(@RequestBody List<Long> idList);

    /**
     *
     *
     * @param userNames 用户名列表
     * @return
     */
    @PostMapping("/oa/user/name/id")
    @ApiOperation("根据用户名查询id")
    R<List<Integer>> getIdByWorkCode(@RequestBody List<String> userNames);

    /**
     * 获取基片当班信息
     * @param dto
     * @return
     */
    @ApiOperation("获取基片当班信息")
    @PostMapping("/feign/oa/todayClass/info")
    R<List<OaTodayClassVO>> getTodayClassInfo(@RequestBody TupleDTO<String,LocalDate> dto);


    /**
     *
     *
     * @param dto 用户名列表
     * @return
     */
    @PostMapping("/oa/user/jobTitle")
    @ApiOperation("根据用户名查询id")
    R<List<OaEmpVO>> getUserByIdByJobTitle(@RequestBody OaHrmDTO dto);

    /**
     * 根据岗位和部门查询
     * @param departmentId
     * @param jobTitle
     * @return
     */
    @GetMapping("/oa/user/jobTitle/department")
    @ApiOperation("根据岗位和部门查询")
    R<List<OaUserIdAndName>> getUserByIdByJobTitleAndDepartmentId(@RequestParam("departmentId") Integer departmentId,@RequestParam("jobTitle") Integer jobTitle );

    /**
     * 查询排班信息
     * @param dto
     * @return
     */
    @ApiOperation("查询排班信息")
    @PostMapping("/oa/roster/record")
    R<List<OaRosterRecordVO>> getRosterRecord(@RequestBody OaRosterRecordDTO dto);

    /**
     * 根据工号查询信息
     * @param workCodes
     * @return
     */
    @ApiOperation("查询排班信息")
    @PostMapping("/oa/user/workCode")
    R<List<OaUserIdAndName>> getInfoByWorkCode(@RequestBody List<String> workCodes);

    /**
     * 新建流程
     * @param dto
     * @return
     */
    @ApiOperation("新建流程")
    @PostMapping("/oa/workflow/doCreateRequest")
    R<OaWorkflowStatusVO> createWorkflow(@RequestBody OaWorkFlowParamDTO dto);

    /**
     * 提交流程
     * @param dto
     * @return
     */
    @ApiOperation("提交流程")
    @PostMapping("/oa/workflow/submitRequest")
    R<OaWorkflowStatusVO> submitWorkflow(@RequestBody OaWorkFlowSubmitParamDTO dto);

    /**
     * 根据用户条件查询
     * @param dto
     * @return
     */
    @ApiOperation("根据条件查询")
    @PostMapping("/oa/user/condition")
    R<List<OaEmpVO>> getUserByCondition(@RequestBody OaEmpStatusDTO dto);

    /**
     * 根据部门信息查询部门详情
     * @param dto
     * @return
     */
    @ApiOperation("根据部门信息查询部门详情")
    @PostMapping("/oa/org/detail")
    R<List<OaOrgDetailVO>> getOrgDetailByCondition(@RequestBody OaOrgDetailDTO dto);

    /**
     * 根据部门信息查询部门详情V2
     * @param dto
     * @return
     */
    @ApiOperation("根据部门信息查询部门详情V2")
    @PostMapping("/oa/org/detail/v2")
    R<List<OaOrgDetailVO>> getOrgDetailByConditionV2(@RequestBody OaOrgDetailParamDTO dto);

    /**
     * 根据部门信息查询部门详情V3
     * @param dto
     * @return
     */
    @ApiOperation("根据部门信息查询部门详情V2")
    @PostMapping("/oa/org/detail/v3")
    R<List<OaDepartmentVO>> getOrgDetailByConditionV3(@RequestBody OaOrgDetailParamDTO dto);

    /**
     * 获取流程节点当前操作人
     * @param dto
     * @return
     */
    @ApiOperation("获取流程当前节点的操作人")
    @PostMapping("/feign/oa/workflow/currentOperator")
    R<OaWorkFlowOperatorDTO> getCurrentOperator(@RequestBody OaOperatorParamDTO dto);

    /**
     * 根据userId查询考勤组
     * @param userId
     * @return
     */
    @ApiOperation("根据userId查询考勤组")
    @PostMapping("/feign/oa/user/group")
    R<List<OaGroupIdAndNameVO>> getGroupByUser(@RequestBody Set<Long> userId);

    /**
     * 根据id查询用户名
     * @param id
     * @return
     */
    @ApiOperation("根据Id查询用户名")
    @GetMapping("/feign/oa/user/loginid")
    R<String> getLoginIdById(@RequestParam("id") Integer id);

    @ApiOperation("根据Id查询用户信息")
    @GetMapping("/feign/oa/user/info/id")
    R<OaEmpVO> getUserInfoById(@RequestParam("id") Integer id);

    /**
     * 传入url生产文档
     * @param url 跳转url  docName 文档名字
     * @return
     */
    @ApiOperation("保存文档并获取文档id")
    @GetMapping("/feign/oa/workflow/saveDoc")
    R<OaSaveDocVO> saveDoc(@RequestParam("url") String url,@RequestParam("docName") String docName);

    @ApiOperation("根据部门id获取信息")
    @GetMapping("/feign/oa/department/info")
    R<OaDepartmentVO> getDepartmentInfoById(@RequestParam("id") Integer id);

    @ApiOperation("根据岗位id获取岗位信息")
    @GetMapping("/feign/oa/jobTitle/info")
    R<OaJobTitleVO> getJobInfoById(@RequestParam("id") Integer id);

    @ApiOperation("获取班组长信息")
    @PostMapping("/feign/oa/group/leader")
    R<Map<Integer,OaGroupLeaderDTO>> getLeaderInfoByGroup(@RequestBody Set<Integer> groupIds);

    /**
     * 键为2023-06-06:早班 值为甲班
     * @param tupleDTO filed1开始时间 filed2结束时间
     * @return
     */
    @ApiOperation("获取班次对应的班组")
    @PostMapping("/feign/oa/group/count")
    R<Map<String,OaGroupCountDTO>> getGroupInfo(@RequestBody TupleDTO<LocalDate,LocalDate> tupleDTO);

    @ApiOperation("精准获取基片班组")
    @PostMapping("/feign/oa/group/count/jp")
    R<Map<String,OaGroupCountDTO>> getGroupInfoJp(@RequestBody TripleDTO<LocalDate,LocalDate,Integer> tupleDTO);

    /**
     * 获取班组长信息
     * @param productWorkShop 车间名称set
     * @return
     */
    @ApiOperation("获取班组长信息")
    @PostMapping("/feign/oa/group/workshop/leader")
    R<Map<String, List<OaClassUserNameDTO>>> getLeaderName(@RequestBody Set<String> productWorkShop);

    /**
     * 根据id和日期获取用户班组班次信息
     * @param dto
     * @return
     */
    @ApiOperation("获取用户班组班次信息")
    @PostMapping("/feign/oa/user/class")
    R<OaClassVO> getUserClass(@RequestBody TupleDTO<LocalDate,Integer> dto);

    /**
     * 获取所有部门
     * @param dto
     * @return
     */
    @PostMapping("/feign/oa/thirdParty/allDepartment")
    @ApiOperation(value = "获取所有部门")
    R<List<OaDepartmentVO>> getAllDepartment(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 获取所有岗位
     * @param dto
     * @return
     */
    @PostMapping("/feign/oa/thirdParty/allJobTitle")
    @ApiOperation(value = "获取所有岗位")
    R<List<OaJobTitleVO>> getAllJobTitle(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 获取当前节点操作人
     * @param dto E1 requestId E2 nodeId
     * @return
     */
    @ApiOperation("获取当前节点操作人")
    @PostMapping("/feign/oa/workflow/operator")
    R<OaUserIdAndName> getOperator(@RequestBody TupleDTO<Integer,Integer> dto);

    /**
     * 获取当前节点所有操作人
     * @param dto
     * @return 若结果中有-1则表示为归档
     */
    @ApiOperation("获取当前节点的所有操作人")
    @PostMapping("/feign/oa/workflow/operator/users")
     R<Set<Integer>> getWorkflowUsers(@RequestBody TupleDTO<String,String> dto);

    /**
     * 根据岗位id获取用户id
     * @param jobTitles
     * @return
     */
    @PostMapping("/feign/oa/jobTitle/id")
    R<List<Integer>> getJobTitleById(@RequestBody Set<Integer> jobTitles);

    /**
     * 获取oa用户入职时间
     * @param loginCode
     * @return
     */
    @ApiOperation("获取用户入职时间")
    @PostMapping("/feign/oa/user/workStartTime")
    R<Map<String, LocalDate>> getCompanyStartTime(@RequestBody Set<String> loginCode);
}
