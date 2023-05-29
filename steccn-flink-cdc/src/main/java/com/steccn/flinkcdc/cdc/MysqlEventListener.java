package com.steccn.flinkcdc.cdc;

import com.steccn.flinkcdc.config.DatasourceConfig;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlEventListener implements ApplicationRunner {

    private final DataChangeSink dataChangeSink;
    private final DatasourceConfig datasourceConfig;

    @Override
    public void run(ApplicationArguments args) {
        for(DatasourceConfig.DatasourceConfigStatic datasourceConfigStatic:datasourceConfig.getInfos()){
            CompletableFuture.runAsync(() -> {
                try {
                    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                    MySqlSource<DataChangeDTO> mySqlSource = buildDataChangeSource(datasourceConfigStatic);
                    DataStream<DataChangeDTO> streamSource = env
                            .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "mysql-source")
                            //对接收器使用并行1来保持消息的顺序
                            .setParallelism(1);
                    streamSource.addSink(dataChangeSink);
                    env.executeAsync("mysql-cdc");
                } catch (Exception e) {
                    log.error("mysql --> es, Exception=", e);
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        }
    }

    /**
     * 构造变更数据源
     *
     * @return DebeziumSourceFunction<DataChangeInfo>
     */
    private MySqlSource<DataChangeDTO> buildDataChangeSource(DatasourceConfig.DatasourceConfigStatic datasourceConfigStatic) {
        return MySqlSource.<DataChangeDTO>builder()
                .hostname(datasourceConfigStatic.getHostname())
                .port(Integer.parseInt(datasourceConfigStatic.getPort()))
                .databaseList(datasourceConfigStatic.getDatabaseList())
                // 支持正则匹配
                .tableList(datasourceConfigStatic.getTableList())
                .username(datasourceConfigStatic.getUsername())
                .password(datasourceConfigStatic.getPassword())
                // initial:初始化快照,即全量导入后增量导入(检测更新数据写入)
                .startupOptions(StartupOptions.latest())
                .deserializer(new MysqlDeserialization())
                .serverTimeZone("GMT+8")
                .build();
    }
}


