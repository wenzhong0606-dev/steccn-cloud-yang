package com.steccn.canal.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class JsSysDictData implements Serializable {
    private String dictCode;
    private String parentCode;
    private String parentCodes;
}
