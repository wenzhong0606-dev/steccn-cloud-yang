package com.steccn.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OaWorkFlowNameFileDTO implements Serializable {
    private String filePath;
    private String fileName;
}
