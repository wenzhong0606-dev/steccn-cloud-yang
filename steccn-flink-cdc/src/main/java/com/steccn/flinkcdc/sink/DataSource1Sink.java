package com.steccn.flinkcdc.sink;

import cn.hutool.json.JSONUtil;
import com.steccn.flinkcdc.dto.MysqlDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 数据源1的监听结果
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
@Component
@Slf4j
public class DataSource1Sink implements SinkFunction<MysqlDataDTO>, Serializable {
    @Override
    public void invoke(MysqlDataDTO value, Context context) throws Exception {
        log.info("监听数据: {}", JSONUtil.toJsonStr(value));
        // 拿到变更数据做业务逻辑处理...
    }
}
