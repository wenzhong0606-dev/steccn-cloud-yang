package com.steccn.flinkcdc.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 第一个数据源的数据
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
@Component
@Data
public class Datasource1Config {
    @Value("${cdc.datasource1.hostname}")
    private String hostname;

    @Value("${cdc.datasource1.port}")
    private String port;

    @Value("${cdc.datasource1.databaseList}")
    private String databaseList;

    @Value("${cdc.datasource1.tableList}")
    private String tableList;

    @Value("${cdc.datasource1.username}")
    private String username;

    @Value("${cdc.datasource1.password}")
    private String password;
}
