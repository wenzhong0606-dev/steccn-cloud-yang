package com.steccn.qms.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Excel更新DTO
 */
@Data
public class QtExcelDTO implements Serializable {
    /**
     * 选择是否更新已存在数据，若选中则根据编码更新对应数据
     */
    private Integer updateFlag;

    /**
     * excel文件
     */
    private MultipartFile file;
}
