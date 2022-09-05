package com.catiger.logregservice.res;

import lombok.Data;

@Data
public class ResponseBrief {
    private int code;
    private String msg;
    private Object content;
    public ResponseBrief(int code, String msg, Object obj) {
        this.code = code;
        this.content = obj;
        this.msg = msg;
    }

    public ResponseBrief(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
