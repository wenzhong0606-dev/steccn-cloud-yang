package com.steccn.flinkcdc.constant;

import com.steccn.flinkcdc.config.Datasource1Config;
import com.steccn.oa.config.OaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
public class CdcConstant {
    /**
     * dataBase1常量
     */
    public static final String DATASOURCE1_HOSTNAME =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;
    public static final String DATASOURCE1_PORT =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;
    public static final String DATASOURCE1_DATABASE_LIST =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;
    public static final String DATASOURCE1_TABLE_LIST =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;
    public static final String DATASOURCE1_USERNAME =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;
    public static final String DATASOURCE1_PASSWORD =Datasource1ConfigHelper.DATASOURCE1_HOSTNAME;

}
@Component
class Datasource1ConfigHelper{
    static String DATASOURCE1_HOSTNAME;
    static String DATASOURCE1_PORT;
    static String DATASOURCE1_DATABASE_LIST;
    static String DATASOURCE1_TABLE_LIST;
    static String DATASOURCE1_USERNAME;
    static String DATASOURCE1_PASSWORD;

    @Autowired
    public void setOaConfig(Datasource1Config config){
        DATASOURCE1_HOSTNAME = config.getHostname();
        DATASOURCE1_PORT = config.getPort();
        DATASOURCE1_DATABASE_LIST = config.getDatabaseList();
        DATASOURCE1_TABLE_LIST = config.getTableList();
        DATASOURCE1_USERNAME = config.getUsername();
        DATASOURCE1_PASSWORD = config.getPassword();
    }
}
