package com.steccn.common.convert;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc
 * @date 2023/4/19
 */
public class LocalDateTimeTypeHandler extends AbstractJsonTypeHandler<List<LocalDateTime>> {

    @Override
    protected List<LocalDateTime> parse(String json) {
        return JSON.parseArray(json, LocalDateTime.class);
    }

    @Override
    protected String toJson(List<LocalDateTime> obj) {
        return JSON.toJSONString(obj);
    }
}
