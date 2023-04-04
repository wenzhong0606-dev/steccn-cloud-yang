package com.steccn.data.config;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.core.env.Environment;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 分片算法，按月分片
 */
public class TimeShardingAlgorithm implements PreciseShardingAlgorithm<LocalDateTime>, RangeShardingAlgorithm<LocalDateTime> {

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
        return buildShardingTable(preciseShardingValue.getLogicTableName(), preciseShardingValue.getValue());
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
        Range<LocalDateTime> valueRange = rangeShardingValue.getValueRange();
        LocalDateTime start = valueRange.lowerEndpoint();
        LocalDateTime end = valueRange.upperEndpoint();

        Collection<String> tables = new ArrayList<>();
        while (start.compareTo(end) <= 0) {
            tables.add(buildShardingTable(rangeShardingValue.getLogicTableName(), start));
            start = start.plusMonths(1L);
        }
        // collection配置的数据节点表，这里是排除不存在配置中的表
        collection.retainAll(tables);
        return collection;
    }

    private static void executeSql(List<String> sqlList) {
        Environment env = SpringUtil.getApplicationContext().getEnvironment();
        try (Connection conn = DriverManager.getConnection(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.jdbc-url")), env.getProperty("spring.datasource.hikari.username"), env.getProperty("spring.datasource.hikari.password"))) {
            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(false);
                for (String sql : sqlList) {
                    st.execute(sql);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



}