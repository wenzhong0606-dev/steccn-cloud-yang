package com.steccn.authserver.util;

import cn.hutool.json.JSONUtil;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.Constant;
import com.steccn.common.vo.R;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.function.Supplier;

/**
 * FeignOptional 用于判断feign返回的结果
 *
 * @author: yequan
 * @date: 2022-10-08 11:00:00
 */
@Slf4j
public final class FeignOptional<T> {

    private final R<T> value;

    private FeignOptional() {
        this.value = null;
    }

    private FeignOptional(R<T> value) {
        this.value = value;
    }

    public static <T> FeignOptional<T> of(R<T> value) {
        if (log.isDebugEnabled()) {
            log.debug("方法:{}, 返回参数==>{}", Thread.currentThread().getStackTrace()[2], JSONUtil.parse(value));
        }
        return new FeignOptional<T>(value);
    }

    /**
     * 存放元素
     *
     * @param supplier    元素消费者 返回单个值
     * @param serviceName 服务名称
     * @param <T>         泛型
     * @return 自己对象
     */
    public static <T> FeignOptional<T> of(Supplier<R<T>> supplier, String serviceName) {
        R<T> value;
        try {
            value = supplier.get();
        } catch (FeignException.ServiceUnavailable e) {
            throw new BusinessException(serviceName + "服务暂不可用");
        } catch (Exception ee) {
            log.error("{}", ee.getLocalizedMessage());
            throw new BusinessException(serviceName + "服务调用异常");
        }
        if (log.isDebugEnabled()) {
            log.debug("方法:{}, 返回参数==>{}", Thread.currentThread().getStackTrace()[2], JSONUtil.parse(value));
        }
        return new FeignOptional<T>(value);
    }

    /**
     * 判断状态码
     * @return
     */
    public T get() {
        if (value == null) {
            throw new BusinessException("远程调用异常");
        }
        if (Constant.SUCCESS.equals(value.getCode())) {
            return value.getData();
        } else {
            log.error("Feign 调用失败==>{}", JSONUtil.parse(value));
            throw new BusinessException(value.getMsg());
        }
    }

    public T orElseGet(Supplier<? extends T> other) {
        if (value == null) {
            return other.get();
        }
        if (Constant.SUCCESS.equals(value.getCode())) {
            return value.getData();
        } else {
            log.error("Feign 调用失败==>{}", JSONUtil.parse(value));
            return other.get();
        }
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value == null) {
            throw exceptionSupplier.get();
        }
        if (Constant.SUCCESS.equals(value.getCode())) {
            return value.getData();
        } else {
            log.error("Feign 调用失败==>{}", JSONUtil.parse(value));
            throw exceptionSupplier.get();
        }
    }
}
