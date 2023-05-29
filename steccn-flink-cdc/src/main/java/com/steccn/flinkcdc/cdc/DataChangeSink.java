package com.steccn.flinkcdc.cdc;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSONObject;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.steccn.flinkcdc.service.HrmresourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.stereotype.Component;

/**
 * 处理数据
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */
@Component
@Slf4j
public class DataChangeSink implements SinkFunction<DataChangeDTO> {
    @Override
    public void invoke(DataChangeDTO dataChangeInfo, Context context) {
        // 变更类型： 0 初始化 1新增 2修改 3删除 4导致源中的现有表被截断的操作
        Integer operatorType = dataChangeInfo.getOperatorType();
        // TODO 数据处理,不能在方法外注入需要的bean，会报错必须实例化才可以
        //  (Caused by: org.apache.flink.api.common.InvalidProgramException:  xxx... is not serializable.
        //  The object probably contains or references non serializable fields.)，
        // 所以使用SpringUtil 获取需要的 bean，比如获取 extends ElasticsearchRepository<T, ID>的接口如下所示，
        // 然后就可以使用封装的方法进行增删改操作了
        String data = dataChangeInfo.getData();
//        log.info("########data={}",data);
//        SystemDept systemDept = JSONObject.parseObject(data, SystemDept.class);
//        SystemDeptRepository bean = SpringUtil.getBean(SystemDeptRepository.class);
        HrmresourceService bean = SpringUtil.getBean(HrmresourceService.class);
        bean.handleData(dataChangeInfo);
        // 初始化/新增/修改（非逻辑删除）
//        bean.save(systemDept);
        // 删除/修改（逻辑删除）
        // bean.delete(systemDept);
    }
}
