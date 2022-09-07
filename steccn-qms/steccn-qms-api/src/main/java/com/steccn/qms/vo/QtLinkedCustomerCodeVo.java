package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 回显关联客户Vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtLinkedCustomerCodeVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
}
