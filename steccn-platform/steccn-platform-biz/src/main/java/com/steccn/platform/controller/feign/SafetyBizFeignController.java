package com.steccn.platform.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SafetyTodoListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/01/23
 */
@Api("安环系统业务Feign")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/safety")
public class SafetyBizFeignController {
    private final SafetyTodoListService safetyTodoListService;

    @ApiOperation(value = "发送所有待办消息")
    @GetMapping("/sendAllMsg")
    public R<Boolean> sendAllMsg() {
        return R.success(safetyTodoListService.sendMsg());
    }
}
