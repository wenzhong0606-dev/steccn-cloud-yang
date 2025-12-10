package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.IotAlert;
import com.steccn.platform.dto.IotAlertDTO;
import com.steccn.platform.service.IotAlertService;
import com.steccn.platform.mapper.IotAlertMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Service实现
* @createDate 2025-12-10 14:27:19
*/
@Service
public class IotAlertServiceImpl extends ServiceImpl<IotAlertMapper, IotAlert>
    implements IotAlertService{


    @Override
    public Boolean addAlert(IotAlertDTO dto) {
        IotAlert model = new IotAlert();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }
}




