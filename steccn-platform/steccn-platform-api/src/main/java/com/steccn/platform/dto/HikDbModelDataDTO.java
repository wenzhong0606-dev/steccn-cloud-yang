package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbModelDataDTO {
    private HikDbWeighRegDTO weighReg;
    private List<HikDbWeighCommitDTO> weighCommits;
}
