package com.steccn.ems.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仪表台账表分页明细
 */
@Data
public class EpInstrumentAccountVO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 所属分区节点id
     */
    private Long partitionNode;

    /**
     * 所属分区节点名称
     */
    private String partitionNodeName;

    /**
     * 仪表名称
     */
    @NotNull(message = "仪表名称不能为空")
    private String meterName;

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
}
