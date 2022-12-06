package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EpInstrumentAccountPageVO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 仪表名称
     */
    private String meterName;

    /**
     * 所属分区节点id
     */
    private Long partitionNode;

    /**
     * 所属分区节点名称
     */
    private EpPartitionVO partitionNodeName;

    /**
     * 远传表映射编码
     */
    private String distantMapCode;

    /**
     * 安装位置数据字典id
     */
    private Long site;

    /**
     * 安装位置名称
     */
    private String siteName;

    /**
     * 最后抄表时间
     */
    private LocalDateTime lastReadingTime;

    /**
     * 0停用 1启用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
