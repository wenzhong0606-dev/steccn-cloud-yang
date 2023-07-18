package com.steccn.authserver.util;

import cn.hutool.json.JSONUtil;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.Constant;
import com.steccn.common.vo.R;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * FeignOptional 用于判断feign返回的结果
 *
 * @author: yequan
 * @date: 2022-10-08 11:00:00
 */
@Slf4j
public final class OutSystemOptional<T> {

    private final T value;

    private OutSystemOptional() {
        this.value = null;
    }

    private OutSystemOptional(T value) {
        this.value = value;
    }

    public static <T> OutSystemOptional<T> of(T value) {
        if (log.isDebugEnabled()) {
            log.debug("方法:{}, 返回参数==>{}", Thread.currentThread().getStackTrace()[2], JSONUtil.parse(value));
        }
        return new OutSystemOptional<T>(value);
    }

    /**
     * 存放元素
     *
     * @param supplier    元素消费者 返回单个值
     * @param serviceName 服务名称
     * @param <T>         泛型
     * @return 自己对象
     */
    public static <T> OutSystemOptional<T> of(Supplier<T> supplier, String serviceName) {
        T value;
        try {
            value = supplier.get();
        } catch (RetryableException e) {
            throw new BusinessException(serviceName + "服务暂不可用");
        } catch (FeignException.ServiceUnavailable e) {
            throw new BusinessException(serviceName + "服务暂不可用");
        } catch (Exception ee) {
            log.error("{}", ee.getLocalizedMessage());
            throw new BusinessException(serviceName + "服务调用异常");
        }
        if (log.isDebugEnabled()) {
            log.debug("方法:{}, 返回参数==>{}", Thread.currentThread().getStackTrace()[2], JSONUtil.parse(value));
        }
        return new OutSystemOptional<T>(value);
    }

    /**
     * 判断状态码
     *
     * @return
     */
    public T get() {
        if (value == null) {
            throw new BusinessException("远程调用异常");
        }
        return value;
    }

}
