package com.steccn.hikvision.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.HikCompanyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/05/17
 */
@RestController
@RequestMapping("/feign/hikvision")
@RequiredArgsConstructor
public class HikFeignController {
    private final HikvisionService hikvisionService;

    @PostMapping("/company")
    public R<List<HikCompanyVO>> hikCompanyInfo(@RequestBody TupleDTO<String,String> dto) {
        return R.success(hikvisionService.getCompanyInfo(dto));
    }
}
