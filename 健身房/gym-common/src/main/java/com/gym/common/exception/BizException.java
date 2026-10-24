package com.gym.common.exception;

import com.gym.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final String msg;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    /**
     * 同时指定 ResultCode 与自定义错误消息
     */
    public BizException(ResultCode resultCode, String customMsg) {
        super(customMsg);
        this.code = resultCode.getCode();
        this.msg = customMsg;
    }

    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
        this.msg = msg;
    }
}
