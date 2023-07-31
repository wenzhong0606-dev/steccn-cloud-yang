package com.steccn.platform.serializer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 避免返回转义字符串
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
public class StringToJsonSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        serializer.write(JSONObject.parseObject(object.toString()));
    }
}
