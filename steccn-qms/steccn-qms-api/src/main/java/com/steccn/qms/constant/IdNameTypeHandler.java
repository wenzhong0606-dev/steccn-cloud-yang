package com.steccn.qms.constant;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.steccn.qms.dto.QtBaseIdNameDTO;

/**
 * @author yequan
 * @desc idName转换器
 * @date 2023/4/23
 */
public class IdNameTypeHandler extends AbstractJsonTypeHandler<QtBaseIdNameDTO> {

    @Override
    protected QtBaseIdNameDTO parse(String json) {
        return JSON.parseObject(json, QtBaseIdNameDTO.class);
    }

    @Override
    protected String toJson(QtBaseIdNameDTO obj) {
        return JSON.toJSONString(obj);
    }
}
