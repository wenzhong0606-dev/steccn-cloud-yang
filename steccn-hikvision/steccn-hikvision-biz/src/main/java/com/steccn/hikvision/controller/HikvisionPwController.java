package com.steccn.hikvision.controller;


import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveDTO;
import com.steccn.pms.dto.MesRoleDTO;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.vo.CameraVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;


/**
 * @author yequan
 * @desc 海康威视权限控制器
 */
@RestController
@RequestMapping("/hikvision/pw")
@RequiredArgsConstructor
public class HikvisionPwController {

    private final SysHikPwService sysHikPwService;

    @ApiOperation("权限保存")
    @PostMapping
    public R<Boolean> save(@Valid @RequestBody ValidList<SysHikPwSaveDTO> dtoList) {
        return R.success(sysHikPwService.pwSave(dtoList.getList()));
    }

    @ApiOperation("权限查询")
    @GetMapping
    public R<List<SysHikPwDTO>> info(@RequestParam("roleCode") String roleCode) {
        return R.success(sysHikPwService.info(roleCode));
    }

    @ApiOperation("用户权限-树型结构输出")
    @GetMapping("/tree")
    public R<List<CameraVO>> tree() {
        String username = SecurityUtils.getUser().getUsername();
        return R.success(sysHikPwService.tree(username));
    }

    @ApiOperation("获取所有角色")
    @GetMapping("/getAllRole")
    public R<List<MesRoleDTO>> getAllRole(){
        return R.success(sysHikPwService.getAllRole());
    }
}
