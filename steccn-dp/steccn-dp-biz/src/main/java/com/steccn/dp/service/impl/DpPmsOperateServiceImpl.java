package com.steccn.dp.service.impl;

import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.service.DpPmsOperateService;
import com.steccn.dp.vo.DpPmsOperateInStorageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @desc 生产管理-运营驾驶舱服务类
 * @date 2023/11/29
 */
@Service
@RequiredArgsConstructor
public class DpPmsOperateServiceImpl implements DpPmsOperateService {


    @Override
    public DpPmsOperateInStorageVO inStorage(DpPmsOperateDTO dto) {
        return null;
    }
}
