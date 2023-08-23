package com.steccn.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SalesUserNameDTO;
import com.steccn.platform.feign.RemoteSalesFeignApi;
import com.steccn.platform.service.SalesTokenService;
import com.steccn.platform.vo.SalesTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 获取销售系统token
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
@Service
@RequiredArgsConstructor
public class SalesTokenServiceImpl implements SalesTokenService {
    private final RemoteSalesFeignApi remoteSalesFeignApi;
    @Override
    public SalesTokenVO getSalesToken() {
        SalesUserNameDTO dto = new SalesUserNameDTO();
        dto.setLogin(SecurityUtils.getUser().getUsername());
        String data = remoteSalesFeignApi.getToken(dto);
        JSONObject jsonObject = JSONObject.parseObject(data);
        SalesTokenVO token = jsonObject.getJSONObject("data").toJavaObject(SalesTokenVO.class);
        return token;
    }
}
