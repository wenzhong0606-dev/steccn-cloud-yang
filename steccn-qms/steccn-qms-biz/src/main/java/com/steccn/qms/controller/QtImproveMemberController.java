package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtImproveMemberDTO;
import com.steccn.qms.service.QtComplainTypeService;
import com.steccn.qms.dto.QtComplainTypeDTO;
import com.steccn.qms.service.QtImproveMemberService;
import com.steccn.qms.vo.QtComplainTypeVO;
import com.steccn.qms.vo.QtImproveMemberVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 整改成员
 * @date 2023/3/15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/improve/member")
@Slf4j
public class QtImproveMemberController {

    private final QtImproveMemberService qtImproveMemberService;

    @GetMapping
    @ApiOperation(value = "整改成员-列表")
    public R<List<QtImproveMemberVO>> memberList() {
        return R.success(qtImproveMemberService.memberList());
    }

    @PostMapping
    @ApiOperation(value = "整改成员-添加")
    public R<Boolean> add(@Valid @RequestBody QtImproveMemberDTO dto) {
        return R.success(qtImproveMemberService.addMember(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "整改成员-移除")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtImproveMemberService.delete(id));
    }
}

