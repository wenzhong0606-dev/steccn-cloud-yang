package com.steccn.flinkcdc.service;

import com.alibaba.fastjson2.JSONObject;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.steccn.flinkcdc.dto.HrmresourceDTO;
import org.springframework.stereotype.Service;

/**
 * 处理泛微hrmresoruce
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */
@Service
public class HrmresourceService {
    /**
     * 处理人员信息
     * @param dataChangeInfo
     * @return
     */
    public Boolean handleData(DataChangeDTO dataChangeInfo){
        String data = dataChangeInfo.getData();
        HrmresourceDTO hrmresourceDTO = JSONObject.parseObject(data, HrmresourceDTO.class);
        return true;
    }
}
