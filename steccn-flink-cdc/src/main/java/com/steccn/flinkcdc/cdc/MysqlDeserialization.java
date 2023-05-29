package com.steccn.flinkcdc.cdc;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 类型转换
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */

@Slf4j
public class MysqlDeserialization implements DebeziumDeserializationSchema<DataChangeDTO> {

    public static final String TS_MS = "ts_ms";
    public static final String BIN_FILE = "file";
    public static final String POS = "pos";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String SOURCE = "source";

    /**
     * 获取操作类型 READ CREATE UPDATE DELETE TRUNCATE;
     * 变更类型： 0 初始化 1新增 2修改 3删除 4导致源中的现有表被截断的操作
     */
    private static final Map<String, Integer> OPERATION_MAP = ImmutableMap.of(
            "READ", 0,
            "CREATE", 1,
            "UPDATE", 2,
            "DELETE", 3,
            "TRUNCATE", 4);

    /**
     * 反序列化数据,转为变更JSON对象
     *
     * @param sourceRecord sourceRecord
     * @param collector    collector
     */
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<DataChangeDTO> collector) {
        String topic = sourceRecord.topic();
        String[] fields = topic.split("\\.");
        String database = fields[1];
        String tableName = fields[2];
        Struct struct = (Struct) sourceRecord.value();
        final Struct source = struct.getStruct(SOURCE);
        DataChangeDTO dataChangeDTO = new DataChangeDTO();
        // 获取操作类型 READ CREATE UPDATE DELETE TRUNCATE;
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        String type = operation.toString().toUpperCase();
        int eventType = OPERATION_MAP.get(type);
        // fixme 一般情况是无需关心其之前之后数据的,直接获取最新的数据即可,但这里为了演示,都进行输出
        dataChangeDTO.setBeforeData(getJsonObject(struct, BEFORE).toJSONString());
        dataChangeDTO.setAfterData(getJsonObject(struct, AFTER).toJSONString());
        if (eventType == 3) {
            dataChangeDTO.setData(getJsonObject(struct, BEFORE).toJSONString());
        } else {
            dataChangeDTO.setData(getJsonObject(struct, AFTER).toJSONString());
        }
        dataChangeDTO.setOperatorType(eventType);
        dataChangeDTO.setFileName(Optional.ofNullable(source.get(BIN_FILE)).map(Object::toString).orElse(""));
        dataChangeDTO.setFilePos(
                Optional.ofNullable(source.get(POS))
                        .map(x -> Integer.parseInt(x.toString()))
                        .orElse(0)
        );
        dataChangeDTO.setDatabase(database);
        dataChangeDTO.setTableName(tableName);
        dataChangeDTO.setOperatorTime(Optional.ofNullable(struct.get(TS_MS))
                .map(x -> Long.parseLong(x.toString())).orElseGet(System::currentTimeMillis));
        // 输出数据
        collector.collect(dataChangeDTO);
    }

    /**
     * 从元素数据获取出变更之前或之后的数据
     *
     * @param value        value
     * @param fieldElement fieldElement
     * @return JSONObject
     */
    private JSONObject getJsonObject(Struct value, String fieldElement) {
        Struct element = value.getStruct(fieldElement);
        JSONObject jsonObject = new JSONObject();
        if (element != null) {
            Schema afterSchema = element.schema();
            List<Field> fieldList = afterSchema.fields();
            for (Field field : fieldList) {
                Object afterValue = element.get(field);
                jsonObject.put(field.name(), afterValue);
            }
        }
        return jsonObject;
    }

    @Override
    public TypeInformation<DataChangeDTO> getProducedType() {
        return TypeInformation.of(DataChangeDTO.class);
    }
}