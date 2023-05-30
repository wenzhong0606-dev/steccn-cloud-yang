package com.steccn.flinkcdc.constant;

import com.steccn.flinkcdc.config.DatasourceConfig;
import com.steccn.oa.constant.OaManagerConstant;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/05/25
 */
public interface CdcConstant {

    String UNALLOCATED_DEPARTMENT_CODE = "Unallocated";
    Integer NUM_2 = 2;
    String JOB_TITLE_RONGHUA = "熔化班长";
    /**
     * 部门映射
     */
    @Getter
    @AllArgsConstructor
    enum OaMesDepartmentMapping {
        A("基片二车间", "基片二车间"),
        B("公司领导", "总经办"),
        C("行政人事部", "行政人事部"),
        D("财务部", "财务部"),
        E("销售部","销售部"),
        F("采购部","采购部"),
        G("安全环保部","安全环保部"),
        H("品质管理部","品质管理部"),
        I("技术中心","技术中心"),
        J("生产部","生产部"),
        K("原料车间","原料车间"),
        L("基片一车间","基片一车间"),
        M("基片二车间-熔化班长", "基片二车间熔化班长"),
        N("基片制造部","基片制造部"),
        O("深加工一车间","深加工一车间"),
        P("深加工二车间","深加工二车间"),
        Q("深加工三车间","深加工三车间"),
        R("深加工制造部","深加工制造部"),
        S("设备动力部","设备动力部"),
        T("上海凯盛节能-软件开发","软件开发");


        private String oaName;
        private String mesName;

        public static String getMesNameByOaName(String oaName) {
            for (CdcConstant.OaMesDepartmentMapping value : CdcConstant.OaMesDepartmentMapping.values()) {
                if (value.getOaName().equals(oaName)) {
                    return value.getMesName();
                }
            }
            return null;
        }
    }

    /**
     * 操作类型
     */
    @Getter
    @AllArgsConstructor
    enum OperateType{
        INITIAL(0,"初始化"),
        INSERT(1,"新增"),
        UPDATE(2, "修改"),
        DELETE(3, "删除");
        private Integer num;
        private String type;
    }
}
