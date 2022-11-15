package com.steccn.qms.dto;

import com.steccn.qms.vo.QtCheckTaskDestoryInfoVO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 巡检任务提交DTO
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskSubmitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;

    /**
     * 0待检验 1检验中 2已检验 3已取消 4暂存
     */
    private Integer status;

    private List<QtCheckTaskValueDTO> values;

    /**
     * 在线检
     */
    private List<QtOnlineCheckItemDTO> onlineItem;

    /**
     * 落板明细信息
     */
    private List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfo;

    /**
     * 备注
     */
    private String destoryInfoRemark;

}
