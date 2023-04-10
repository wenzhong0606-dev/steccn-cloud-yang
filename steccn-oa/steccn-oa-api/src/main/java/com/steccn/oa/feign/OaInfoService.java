package com.steccn.oa.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.*;
import com.steccn.oa.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
     *
     *
     * @param userNames 用户名列表
     * @return
     */
    @PostMapping("/oa/user/name/id")
    @ApiOperation("根据用户名查询id")
    R<List<Integer>> getIdByWorkCode(@RequestBody List<String> userNames);

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
    R<Map<Long, List<OaGroupIdAndNameVO>>> getGroupByUser(@RequestBody Set<Long> userId);

    /**
     * 根据id查询用户名
     * @param id
     * @return
     */
    @ApiOperation("根据Id查询用户名")
    @GetMapping("/feign/oa/user/loginid")
    R<String> getLoginIdById(@RequestParam("id") Integer id);
}
