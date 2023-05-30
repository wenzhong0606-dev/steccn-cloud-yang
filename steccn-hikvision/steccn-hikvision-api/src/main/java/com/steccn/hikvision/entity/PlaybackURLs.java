package com.steccn.hikvision.entity;

import lombok.Data;

@Data
public class PlaybackURLs {
    private String cameraIndexCode;
    private String recordLocation;
    private String protocol;
    private Integer transmode;
    private String beginTime;
    private String endTime;
    private String uuid;
    private String expand;
    private String streamform;
    private Integer lockType;
}
