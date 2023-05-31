package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * mes员工DTO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class MesEmployeeDTO implements Serializable {
    private String employeePosts;
    private List<String> employeeOfficeList;
    private MesCompanyDTO company;
    private MesOfficeDTO office;
}
