package com.steccn.qms.controller;

import com.steccn.qms.service.QtCheckTaskDestoryInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 类说明
 * @author
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/destoryInfo")
@Slf4j

public class QtCheckTaskDestoryInfo {

    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;


}
