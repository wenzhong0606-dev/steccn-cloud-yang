package com.steccn.hikvision.entity;

import lombok.Data;

@Data
public class PreviewURLs {
    private String cameraIndexCode;
    private int streamType;
    private String protocol;
    private int transmode;
    private String expand;
    private String streamform;
}
