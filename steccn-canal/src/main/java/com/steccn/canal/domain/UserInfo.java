package com.steccn.canal.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {

    private String id;

    private String name;

    private String sex;
}
