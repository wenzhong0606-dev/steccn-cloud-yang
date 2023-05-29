package com.steccn.flinkcdc.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 第一个数据源的数据
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
@Component
@Data
@ConfigurationProperties(prefix = "cdc")
public class DatasourceConfig {
    private List<DatasourceConfigStatic> infos;

    @Data
    public static class DatasourceConfigStatic{
        private String hostname;

        private String port;

        private String databaseList;

        private String tableList;

        private String username;

        private String password;
    }
}
