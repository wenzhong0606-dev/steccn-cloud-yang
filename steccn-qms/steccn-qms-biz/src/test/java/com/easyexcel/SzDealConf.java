package com.easyexcel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class SzDealConf {
    public static void main(String[] args) {

        //指定路径和编码
        CsvWriter writer = CsvUtil.getWriter("e:/testWrite.csv", CharsetUtil.CHARSET_UTF_8);
//按行写出
        List<String> list = Lists.newArrayList();

        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("D:\\ksproject\\gitlab\\steccn-cloud\\data\\aa.csv"));
        List<CsvRow> rows = data.getRows();
        System.out.println("[");
        for (CsvRow csvRow : rows) {

            List<String> rawList = csvRow.getRawList();
            String s0 = rawList.get(0);
            String s2 = rawList.get(2);
            String type = "脱硫脱硝";
            Map<Object, String> map = Maps.newLinkedHashMap();
            map.put("descript", s2);
            map.put("name", s2);
            map.put("code", s0);
            map.put("max", "");
            map.put("min", "");
            map.put("type", type);
            map.put("unit", "");
            String ss = JSONUtil.toJsonStr(map);
            System.out.println(ss+",");
        }
        System.out.println("]");
    }
}
