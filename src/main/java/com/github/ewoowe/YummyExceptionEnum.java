package com.github.ewoowe;

public enum YummyExceptionEnum {
    ILLEGAL_ARGUMENTS(1, "illegal arguments"),
    YAML_FILE_ERROR(2, "yaml file error"),
    KEY_NOT_EXIST(3, "key not exist"),
    KEY_NOT_ARRAY(4, "key not array"),
    KEY_IS_ARRAY(5, "key is array"),
    KEY_MUST_MAP(6, "key must be map");

    private int code;
    private String msg;

    YummyExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
