package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 下载类为List<String>时使用
 *
 * @author AndrewLuo
 * @date 2022/12/14
 */
@Data
public class EpDownloadStrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String str;

    public EpDownloadStrDTO(String str) {
        this.str = str;
    }
}
