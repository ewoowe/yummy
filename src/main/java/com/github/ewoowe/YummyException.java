package com.github.ewoowe;

public class YummyException extends RuntimeException {

    private final int code;

    public static String buildMsg(String master, String...details) {
        StringBuilder sb = new StringBuilder();
        sb.append(master);
        for (String msg : details)
            sb.append(", ").append(msg);
        return sb.toString();
    }

    public YummyException(YummyExceptionEnum yummyExceptionEnum, String...details) {
        this(yummyExceptionEnum.getCode(), buildMsg(yummyExceptionEnum.getMsg(), details));
    }

    public YummyException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
