package com.steccn.data.config;
 
import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
 
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
 
/**
 * 分片算法，按月分片
 */
public class TimeShardingAlgorithm implements PreciseShardingAlgorithm<String>, RangeShardingAlgorithm<String> {
 
    /**
     * 需要空构造方法
     */
    public TimeShardingAlgorithm() {}
 
    /**
     * 时间格式
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
 
    /**
     * 精确分片
     * @param collection
     * @param preciseShardingValue
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        return buildShardingTable(preciseShardingValue.getLogicTableName(), preciseShardingValue.getValue());
    }
 
    /**
     * 构建分片后的表名
     * @param logicTableName
     * @param date
     * @return
     */
    private String buildShardingTable(String logicTableName, String date) {
        StringBuffer stringBuffer = new StringBuffer(logicTableName).append("_").append(date, 0, 6);
        return stringBuffer.toString();
    }
 
    /**
     * 范围分片
     * @param collection
     * @param rangeShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<String> rangeShardingValue) {
        Range<String> valueRange = rangeShardingValue.getValueRange();
        String lower = valueRange.lowerEndpoint();
        String upper = valueRange.upperEndpoint();
 
        LocalDate start = LocalDate.parse(lower, DATE_TIME_FORMATTER);
        LocalDate end = LocalDate.parse(upper, DATE_TIME_FORMATTER);
 
        Collection<String> tables = new ArrayList<>();
        while (start.compareTo(end) <= 0) {
            tables.add(buildShardingTable(rangeShardingValue.getLogicTableName(), start.format(DATE_TIME_FORMATTER)));
            start = start.plusMonths(1L);
        }
        
        // collection配置的数据节点表，这里是排除不存在配置中的表
        collection.retainAll(tables);
        return collection;
    }
 
}