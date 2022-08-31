package com.steccn.portal.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.steccn.common.vo.R;
import com.steccn.portal.util.MesToken;
import lombok.RequiredArgsConstructor;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * mes菜单数据
 *
 * @author Mongknife
 * @date 2022/8/31
 */
@Service
@RequiredArgsConstructor
public class MesMenuService {

    /**
     * 获取mes菜单数据
     *
     * @return
     */
    public R menuRoute(String userName) throws IOException {
        String url = "http://61.177.139.229:380/web/sso/" + userName + "/" + getMesToken(userName) + "?url=http://61.177.139.229:380/web/a/menuRoute?p1=v1%26p2=v2&relogin=false";
        HttpGet httpGet = new HttpGet(url);
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        JSONObject jsonObject;
        R<Object> objectR = new R<>();
        try {
            // 执行请求
            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                //请求体内容
                String content = EntityUtils.toString(entity, "utf-8");
                JSONArray jsonArray = JSON.parseArray(content);
                objectR.setData(jsonArray);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
            //相当于关闭浏览器
            httpClient.close();
        }
        return objectR;
    }


    /**
     * 获取mes系统token
     *
     * @return
     */
    public String getMesToken(String userName) {
        return MesToken.getMesToken(userName);
    }
}
