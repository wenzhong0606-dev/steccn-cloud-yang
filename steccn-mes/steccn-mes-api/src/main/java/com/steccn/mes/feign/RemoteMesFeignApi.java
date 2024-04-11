package com.steccn.mes.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.mes.vo.MesUserInfoVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * mes 远程调用
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@FeignClient(contextId = "RemoteMesServiceFeignApi", value = FeignConstant.STECCN_MES_SERVICE_NAME)
public interface RemoteMesFeignApi {

    /**
     * 根据code获取设备信息
     * @param code
     * @return
     */
    @GetMapping("/feign/mes/eqp/getByCode")
    R<MesEqpVO> getByCode(@RequestParam("code") String code);

    /**
     * 修改设备状态 f1 设备code f2 0正常 1停用
     * @param dto
     * @return
     */
    @PostMapping("/feign/mes/eqp/state")
    R<Boolean> updateEqpState(@RequestBody TupleDTO<String,String> dto);

    /**
     * 获取岗位下的人员
     * @param dto 岗位code
     * @return
     */
    @ApiOperation("获取岗位下的人员")
    @PostMapping("/feign/mes/emp/post")
    R<Map<String, List<String>>> getPostUser(@RequestBody Set<String> dto);

    /**
     * 获取用户所属部门和岗位
     * @param dto
     * @return
     */
    @PostMapping("/user/getEmpDepPost")
    @ApiOperation(value = "获取用户所属部门和岗位")
    R<List<MesUserInfoVO>> getEmpDepPost(@RequestBody TupleDTO<Set<String>,Integer> dto);
}
