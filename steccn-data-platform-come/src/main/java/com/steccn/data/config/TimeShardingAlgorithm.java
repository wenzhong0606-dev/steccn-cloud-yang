package com.steccn.data.config;

import com.google.common.collect.Range;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.SpringContextHolder;
import com.steccn.data.mapper.EpMeterCollectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @desc 分片算法，按月分片
 */
@Slf4j
public class TimeShardingAlgorithm implements PreciseShardingAlgorithm<LocalDateTime>, RangeShardingAlgorithm<LocalDateTime> {

    /**
     * 存在的表-需要验证下此方法在应用层面是否线程安全
     */
    private Set<String> tableSet;
    private Boolean isLoad = false;

    /**
     * 需要空构造方法
     */
    public TimeShardingAlgorithm() {

    }

    /**
     * 时间格式
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 精确分片
     *
     * @param collection
     * @param preciseShardingValue
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<LocalDateTime> preciseShardingValue) {
        if (!isLoad) {
            tableSet = getTableSet(preciseShardingValue.getLogicTableName());
            isLoad = true;
        }
        String format = preciseShardingValue.getValue().format(DATE_TIME_FORMATTER);
        String tableName = preciseShardingValue.getLogicTableName() + "_" + format;
        if (!tableSet.contains(tableName)) {
            synchronized (preciseShardingValue.getLogicTableName().intern()) {
                if (!tableSet.contains(tableName)) {
                    createTable(preciseShardingValue.getLogicTableName(), "_" + format);
                    tableSet = getTableSet(preciseShardingValue.getLogicTableName());
                }
            }
        }
        return tableName;
    }

    /**
     * 构建分片后的表名
     *
     * @param logicTableName
     * @param date
     * @return
     */
    private String buildShardingTable(String logicTableName, LocalDateTime date) {
        String format = date.format(DATE_TIME_FORMATTER);
        return logicTableName + "_" + format;
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<LocalDateTime> rangeShardingValue) {
        if (!isLoad) {
            tableSet = getTableSet(rangeShardingValue.getLogicTableName());
            isLoad = true;
        }
        //获取范围属性
        Range<LocalDateTime> valueRange = rangeShardingValue.getValueRange();
        LocalDateTime start = valueRange.lowerEndpoint();
        LocalDateTime end = valueRange.upperEndpoint();

        Collection<String> resTableList = new ArrayList<>();
        while (start.compareTo(end) <= 0) {
            String tableName = buildShardingTable(rangeShardingValue.getLogicTableName(), start);
            if (tableSet.contains(tableName)) {
                resTableList.add(tableName);
            }
            start = start.plusMonths(1L);
        }
        // collection配置的数据节点表，这里是排除不存在配置中的表
        collection.retainAll(resTableList);
        return collection;
    }

    /**
     * 获取所有的tables
     *
     * @param logicTableName
     * @return
     */
    private Set<String> getTableSet(String logicTableName) {
        EpMeterCollectMapper epMeterCollectMapper = SpringContextHolder.getBean(EpMeterCollectMapper.class);
        return epMeterCollectMapper.getTableNames(logicTableName + "%");
    }

    /**
     * 创建表
     *
     * @param suffix 后缀
     * @return
     */
    private String createTable(String logicalTableName, String suffix) {
        try {
            EpMeterCollectMapper epMeterCollectMapper = SpringContextHolder.getBean(EpMeterCollectMapper.class);
            String newTableName = logicalTableName + suffix;
            epMeterCollectMapper.createTable(newTableName,
                    newTableName + "_key",
                    logicalTableName + "_code_time" + suffix,
                    logicalTableName + "_time_code" + suffix);
            return newTableName;
        } catch (Exception e) {
            log.error("表创建失败，请检查表名是否重复！");
            throw new BusinessException("表创建失败，请检查表名是否重复！");
        }
    }
}