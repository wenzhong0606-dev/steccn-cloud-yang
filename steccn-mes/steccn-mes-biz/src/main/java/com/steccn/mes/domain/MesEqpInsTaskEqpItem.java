package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 点检路线设备点检项关联表
 * @TableName mes_eqp_ins_task_eqp_item
 */
@TableName(value ="mes_eqp_ins_task_eqp_item")
@Data
public class MesEqpInsTaskEqpItem implements Serializable {
    /**
     * 数据id
     */
    @TableId
    private Long id;

    /**
     * 点检项id
     */
    private Long insItemId;

    /**
     * 点检项排序
     */
    private Long sort;

    /**
     * 设备id
     */
    private Long eqpId;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 点检路线id
     */
    private Long insTaskId;

    /**
     * 是否选中
     */
    private String isSelected;

    /**
     * 是否完成点检(0完成，1合格，2不合格)
     */
    private String isFinished;

    /**
     * 图片url
     */
    private String imgUrl;

    /**
     * 图片业务主键
     */
    private Long imgBizKey;

    /**
     * 点检项的值
     */
    private String itemValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}