package com.steccn.common.vo;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.constant.StatusCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("接口返回结果数据结构")
public class R<T> {
    @ApiModelProperty("结果代码")
    private int code;
    @ApiModelProperty("结果说明")
    private String msg;
    @ApiModelProperty("结果数据")
    private T data;

    /**
     * 方法说明:默认返回成功状态
     *
     * @param data
     * @return
     * @author Jinglihui
     * @date 2022/7/15 17:52
     */
    public R(T data) {
        this.code = ResultCode.SUCCESS.getCode();
        this.msg = ResultCode.SUCCESS.getMsg();
        this.data = data;
    }

    /**
     * 方法说明 指定状态码
     *
     * @param data
     * @return
     * @author Jinglihui
     * @date 2022/7/15 17:54
     */
    public R(StatusCode statusCode, T data) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
        this.data = data;
    }

    /**
     * 方法说明
     *
     * @param null
     * @return
     * @author Jinglihui
     * @date 2022/7/15 17:57
     */
    public R(StatusCode statusCode) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
        this.data = null;
    }

    public static <K> R<K> success(K data) {
        R<K> r = new R<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.msg = ResultCode.SUCCESS.getMsg();
        r.data = data;

        return r;
    }

    public static R success() {
        R r = new R();
        r.code = ResultCode.SUCCESS.getCode();
        r.msg = ResultCode.SUCCESS.getMsg();

        return r;
    }

    public static R success(ResultCode resultCode) {
        R r = new R();
        r.code = resultCode.getCode();
        r.msg = resultCode.getMsg();

        return r;
    }

    public static <K> R<K> failure(K data) {
        R<K> r = new R<>();
        r.code = ResultCode.FAILURE.getCode();
        r.msg = ResultCode.FAILURE.getMsg();
        r.data = data;

        return r;
    }

    public static R failure() {
        R r = new R();
        r.code = ResultCode.FAILURE.getCode();
        r.msg = ResultCode.FAILURE.getMsg();

        return r;
    }

    public static R failure(ResultCode resultCode) {
        R r = new R();
        r.code = resultCode.getCode();
        r.msg = resultCode.getMsg();

        return r;
    }
}
