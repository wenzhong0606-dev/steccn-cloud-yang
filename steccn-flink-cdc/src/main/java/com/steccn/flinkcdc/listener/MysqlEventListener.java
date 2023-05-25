package com.steccn.flinkcdc.listener;

import cn.hutool.json.JSONObject;
import com.steccn.flinkcdc.constant.CdcConstant;
import com.steccn.flinkcdc.dto.MysqlDataDTO;
import com.steccn.flinkcdc.sink.DataSource1Sink;
import com.ververica.cdc.connectors.mysql.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.ververica.cdc.debezium.DebeziumSourceFunction;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.data.Envelope;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.dispatcher.DispatcherGateway;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * mysql变化监听器
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
@Component
@RequiredArgsConstructor
public class MysqlEventListener implements ApplicationRunner {
    private final DataSource1Sink dataSource1Sink;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //配置数据源
        DebeziumSourceFunction<MysqlDataDTO> mySqlSource = MySqlSource.<MysqlDataDTO>builder()
                .hostname(CdcConstant.DATASOURCE1_HOSTNAME)
                .port(Integer.parseInt(CdcConstant.DATASOURCE1_PORT))
                .databaseList(CdcConstant.DATASOURCE1_DATABASE_LIST)
                .tableList(CdcConstant.DATASOURCE1_TABLE_LIST)
                .username(CdcConstant.DATASOURCE1_USERNAME)
                .password(CdcConstant.DATASOURCE1_PASSWORD)
                // 增量数据
                .startupOptions(StartupOptions.latest())
                .deserializer(new DebeziumDeserializationSchema() {
                    @Override
                    public TypeInformation getProducedType() {
                        return TypeInformation.of(MysqlDataDTO.class);
                    }

                    @Override
                    public void deserialize(SourceRecord sourceRecord, Collector collector) throws Exception {
                        String topic = sourceRecord.topic();
                        String[] fields = topic.split("\\.");
                        String database = fields[1];
                        String table = fields[2];
                        Envelope.Operation operationType = Envelope.operationFor(sourceRecord);
                        Struct struct = (Struct) sourceRecord.value();
                        String before = this.getJsonStr(struct, "before");
                        String after = this.getJsonStr(struct, "after");
                        collector.collect(MysqlDataDTO.builder()
                                .database(database)
                                .table(table)
                                .operationType(operationType.toString())
                                .before(before)
                                .after(after)
                                .build());
                    }

                    private String getJsonStr(Struct struct, String fieldName) {
                        Struct data = struct.getStruct(fieldName);
                        JSONObject result = new JSONObject();
                        if (data != null) {
                            Schema schema = data.schema();
                            List<Field> fieldList = schema.fields();
                            fieldList.forEach(e -> result.put(e.name(), data.get(e)));
                        }
                        return result.toString();
                    }
                })
                .build();
        //设置环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(3000);
        DataStream<MysqlDataDTO> streamSource = env
                .addSource(mySqlSource, "MySQL Source")
                .setParallelism(1);
        streamSource.addSink(this.dataSource1Sink);

        //执行
        env.execute("mysql-cdc");
    }

}
