package com.steccn.qms.util;

import com.steccn.qms.constant.QtManagerConstant;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载excel模板工具类
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
public class DownloadModelUtil {
    public static InputStream getModelInputStream(String modelName) throws IOException {
        URL url = new URL(QtManagerConstant.QT_CHART_EXCEL_MODEL_URL + modelName);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }
}
