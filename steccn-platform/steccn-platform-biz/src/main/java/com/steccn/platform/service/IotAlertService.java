package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.IotAlert;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.IotAlertDTO;
import com.steccn.platform.vo.IotAlertVO;
import com.steccn.platform.vo.PlatformUserVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Service
* @createDate 2025-12-10 14:27:19
*/
public interface IotAlertService extends IService<IotAlert> {
    /**
     * 新增告警
     * @param dto
     * @return
     */
    Boolean addAlert(IotAlertDTO dto);

    /**
     * 修改告警
     * @param dto
     * @return
     */
    Boolean updateAlert(IotAlertDTO dto);

    /**
     * 获取配置详情
     * @param id
     * @return
     */
    IotAlertVO getOneInfo(Integer id);

    /**
     * 报警规则列表
     * @param dto
     * @return
     */
    Page<IotAlertVO> listInfo(BasePageDTO dto);

    /**
     *
     * @return
     */
    Boolean deleteInfo(Integer id);

    Boolean alertInfo();
}
