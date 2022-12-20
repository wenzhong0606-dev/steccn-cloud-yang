package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EpInstrumentAccountPageVO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 能源类型
     */
    private Integer meterType;

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
    private String partitionNodeName;

    /**
     * 所属分区节点包括父节点
     */
    private String allPartitionNodeName;

    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;

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
     * 资产编码
     */
    private String assetsCode;

    /**
     * 最后抄表时间
     */
    private LocalDateTime lastReadingTime;

    /**
     * 现在时间
     */
    private LocalDateTime localDateTimeNow=LocalDateTime.now();

    /**
     * 0停用 1启用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
