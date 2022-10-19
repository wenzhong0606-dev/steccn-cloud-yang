
package com.steccn.common.annotation;

import java.lang.annotation.*;

/**
 * @author yequan
 * @desc 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {

    /**
     * 描述-业务名称，小于20个字符
     *
     * @return {String}
     */
    String value();

}
