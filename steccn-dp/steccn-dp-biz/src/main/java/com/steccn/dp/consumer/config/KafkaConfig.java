package com.steccn.dp.consumer.config;

import com.google.common.collect.Sets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

import java.util.Set;

/**
 * Kafka原料配置
 *
 * @author AndrewLuo
 * @date 2025/03/10
 */
@Configuration
public class KafkaConfig {
    /**
     * 原料数据过滤
     * @param factory
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerScreenContainerFactory(
            ConcurrentKafkaListenerContainerFactory<String, String> factory) {

        factory.setRecordFilterStrategy(new RecordFilterStrategy<String, String>() {
            @Override
            public boolean filter(ConsumerRecord<String, String> record) {
                // 返回 true 表示消息会被过滤掉
                Set<String> valueSet = Sets.newHashSet();
                //配料状态
                valueSet.add("Raw_YL.ZHM.plyx_BOOL");
                //1#硅砂排料快排速度
                valueSet.add("Raw_YL.CS.kw1_UINT");
                //2#硅砂排料快排速度
                valueSet.add("Raw_YL.CS.kw2_UINT");
                //复合澄清剂喂料快排速度
                valueSet.add("Raw_YL.CS.kw3_UINT");
                //铝粉喂料快排速度
                valueSet.add("Raw_YL.CS.kw4_UINT");
                //小料喂料快排速度
                valueSet.add("Raw_YL.CS.kw6_UINT");
//                	1#硅砂排料慢排速度
                valueSet.add("Raw_YL.CS.mw1_UINT");
//                Raw_YL.CS.mw2_UINT	2#硅砂排料慢排速度
                valueSet.add("Raw_YL.CS.mw2_UINT");
//                Raw_YL.CS.mw3_UINT	复合澄清剂喂料慢排速度
                valueSet.add("Raw_YL.CS.mw3_UINT");
//                Raw_YL.CS.mw4_UINT	铝粉喂料慢排速度
                valueSet.add("Raw_YL.CS.mw4_UINT");
//                Raw_YL.CS.mw6_UINT	小料喂料慢排速度
                valueSet.add("Raw_YL.CS.mw6_UINT");
//                RY.KS.MODE_L		左火
                valueSet.add("RY.KS.MODE_L");
//                RY.KS.MODE_R		右火
                valueSet.add("RY.KS.MODE_R");
                //RY.KS.PIC81_PV_OUT	窑压测量实际值
                valueSet.add("RY.KS.PIC81_PV_OUT");
                //RY.KS.LIC92_PV_OUT	液位测量实际值
                valueSet.add("RY.KS.LIC92_PV_OUT");
                return !valueSet.contains(record.key());
            }
        });

        return factory;
    }
}
