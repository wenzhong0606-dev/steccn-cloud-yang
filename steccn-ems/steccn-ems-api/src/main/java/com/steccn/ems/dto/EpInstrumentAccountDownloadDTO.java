package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EpInstrumentAccountDownloadDTO implements Serializable {
    /**
     * 图片base64码
     */
    private String base64;
}
