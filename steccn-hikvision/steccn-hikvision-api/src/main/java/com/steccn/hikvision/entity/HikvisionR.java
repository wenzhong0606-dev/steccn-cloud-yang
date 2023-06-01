package com.steccn.hikvision.entity;


import lombok.Data;

@Data
public class HikvisionR {
    private int code;
    private String msg;
    private CameraData data;

}
