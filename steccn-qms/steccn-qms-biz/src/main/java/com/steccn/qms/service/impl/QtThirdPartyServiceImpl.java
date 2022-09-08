package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtDefectiveDataPush;
import com.steccn.qms.domain.QtDefectiveDataPushItem;
import com.steccn.qms.dto.QtThirdPartyDefectContentDTO;
import com.steccn.qms.dto.QtThirdPartyDefectiveDTO;
import com.steccn.qms.dto.QtThirdPartyDefectiveDataDTO;
import com.steccn.qms.service.QtDefectiveDataPushItemService;
import com.steccn.qms.service.QtDefectiveDataPushService;
import com.steccn.qms.service.QtThirdPartyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * @desc 第三方同步缺陷数据实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtThirdPartyServiceImpl implements QtThirdPartyService {

    private final QtDefectiveDataPushService qtDefectiveDataPushService;
    private final QtDefectiveDataPushItemService qtDefectiveDataPushItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> saveDefective(QtThirdPartyDefectiveDTO dto) {
        //1.获取当前时间,定义标识
        LocalDateTime now = LocalDateTime.now();
        boolean flag = Boolean.FALSE;

        try {
            //2.保存主表
            QtDefectiveDataPush model = new QtDefectiveDataPush();
            long mainId = IdUtil.getSnowflake().nextId();
            model.setId(mainId);
            model.setMsgType(dto.getMsgEchoType());
            if (Objects.nonNull(dto.getData())) {
                model.setLine(dto.getData().getLine());
                model.setErrorCode(dto.getData().getErrcode());
                model.setProductTime(dto.getData().getProduct_time());
            }
            model.setCreateTime(now);
            qtDefectiveDataPushService.save(model);
            //3.保存子表
            if (Objects.nonNull(dto.getData())) {
                QtThirdPartyDefectiveDataDTO data = dto.getData();
                List<QtThirdPartyDefectContentDTO> defect_content = data.getDefect_Content();
                if (CollUtil.isNotEmpty(defect_content)) {
                    List<QtDefectiveDataPushItem> itemList = Lists.newArrayList();
                    for (QtThirdPartyDefectContentDTO child : defect_content) {
                        QtDefectiveDataPushItem item = new QtDefectiveDataPushItem();
                        item.setId(IdUtil.getSnowflake().nextId());
                        item.setDefectType(child.getDefect_type());
                        item.setLevel(child.getLevel());
                        item.setDefectCount(child.getDefect_count());
                        item.setCreateTime(now);
                        item.setPushId(mainId);
                        itemList.add(item);
                    }
                    qtDefectiveDataPushItemService.saveBatch(itemList);
                }
            }
            flag = Boolean.TRUE;
        } catch (Exception e) {
            log.error("请求的dto数据接收 -> {}", dto);
            log.error(e.getLocalizedMessage());
        }
        if (flag) {
            return R.success(200);
        } else {
            return R.failure("消息不合法");
        }
    }
}
