package com.steccn.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.*;

import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.service.OaService;
import com.steccn.oa.vo.OaCompanyVO;
import com.steccn.oa.vo.OaEmpVO;
import com.steccn.oa.vo.OaToDoWorkflowVO;
import com.steccn.oa.vo.OaWorkflowCountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * oa拦截器
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Api(tags = "泛微oa接口")
@RestController
@RequestMapping("/oa")
@Slf4j
@RequiredArgsConstructor
public class OaController {

    private final OaService oaService;

    private final HrmresourceService hrmresourceService;

    @ApiOperation(value = "获取oa用户的菜单权限")
    @GetMapping("/menuList")
    public R getOaMenuList(@RequestParam("userId") String userId) {
        return R.success(oaService.getMenuList(userId));
    }

    @ApiOperation(value = "校验oa中的用户名密码")
    @PostMapping("/checkLogin")
    public R checkLogin(@RequestBody OaLoginParam oaLoginParam) {
        return R.success(oaService.checkLogin(oaLoginParam));
    }

    @ApiOperation(value = "获取oa跳过登录页token")
    @GetMapping("/ssoToken")
    public R getSsoToken(@RequestParam("loginId") String loginId) {
        OaSsoTokenParam param = new OaSsoTokenParam();
        param.setLoginid(loginId);
        OaSsoTokenResult oaSsoTokenResult = oaService.getSsoToken(param);
        if (oaSsoTokenResult.isStatus()) {
            return R.success(oaSsoTokenResult);
        } else {
            return R.failure(oaSsoTokenResult.getToken());
        }
    }

    @ApiOperation("根据用户名查询用户")
    @GetMapping("/username")
    R loadByUsername(@RequestParam("username") String username) {
        Hrmresource one = hrmresourceService.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getLoginid, username)
                .select(Hrmresource::getId, Hrmresource::getLoginid, Hrmresource::getPassword, Hrmresource::getLastname));
        OaUserDTO oaUserDTO = null;
        if (Objects.nonNull(one)) {
            oaUserDTO = new OaUserDTO();
            oaUserDTO.setUsername(one.getLoginid());
            oaUserDTO.setPassword(one.getPassword());
            oaUserDTO.setUserId(one.getId().longValue());
            oaUserDTO.setName(one.getLastname());
        }
        return R.success(oaUserDTO);
    }

    @ApiOperation("根据id获取用户信息")
    @GetMapping("/userInfo")
    R getUserInfo(@RequestParam("loginId") String loginId) {
        OaUserInfo oaUserInfo = oaService.getOaUserInfo(loginId);
        if (oaUserInfo.getStatus()) {
            return R.success(oaUserInfo);
        } else {
            return R.failure("userInfo为空");
        }
    }

    @ApiOperation("获取所有人员")
    @GetMapping("/allUser")
    R getAllUser() {
        List<OaUserIdAndName> userIdAndNames = oaService.getAll();
        if (!userIdAndNames.isEmpty()) {
            return R.success(userIdAndNames);
        } else {
            return R.failure("userInfo为空");
        }
    }

    @ApiOperation("根据名称模糊获取人员列表")
    @GetMapping("/user/name/{userName}")
    public R<List<OaUserIdAndName>> getUserByName(@PathVariable("userName") String userName) {
        return R.success(hrmresourceService.getUserByName(userName));
    }

    @ApiOperation(value = "OA获取公司数据")
    @GetMapping("/company/{id}")
    public R<List<OaCompanyVO>> getOaCompany(@PathVariable Integer id) {
        return R.success(oaService.getCompany(id));
    }

    @ApiOperation(value = "根据部门和名字获取Emp数据")
    @PostMapping("/company/emp")
    public R<List<OaEmpVO>> getOaEmp(@RequestBody OaEmpDTO dto) {
        return R.success(oaService.getEmpByCondition(dto));
    }

    @ApiOperation(value = "获取待办列表")
    @GetMapping("/workflow/todoList")
    public R<List<OaToDoWorkflowVO>> getOaToDoList() {
        return R.success(oaService.getToDoList(0));
    }

    @ApiOperation(value = "获取待阅列表(消息)")
    @GetMapping("/workflow/toReadList")
    public R<List<OaToDoWorkflowVO>> getOaToReadList() {
        return R.success(oaService.getToDoList(1));
    }

    @ApiOperation(value = "获取流程状态数量")
    @GetMapping("/workflow/count")
    public R<OaWorkflowCountVO> getOaWorkflowCount() {
        return R.success(oaService.getWorkflowCount());
    }

    @ApiOperation("新建流程")
    @PostMapping("/workflow/doCreateRequest")
    public R test(@RequestBody OaWorkFlowParamDTO dto) {
        oaService.getWorkFlow(dto);
        return null;
    }

    @GetMapping("/user/id/{workCode}")
    public R<Long> getIdByWorkCode(@PathVariable("workCode") String workCode) {
        return R.success(hrmresourceService.getIdByWorkCode(workCode));
    }

    @ApiOperation("根据用户名查询id")
    @PostMapping("/user/name/id")
    public R<List<Integer>> getIdByWorkCode(@RequestBody List<String> userNames) {
        return R.success(hrmresourceService.getIdByUserName(userNames));
    }

    @ApiOperation("根据岗位查询人")
    @PostMapping("/user/jobTitle")
    public R<List<OaEmpVO>> getUserByIdByJobTitle(@RequestBody OaHrmDTO dto){
        return R.success(oaService.getHrmSearchResult(dto));
    }
}
