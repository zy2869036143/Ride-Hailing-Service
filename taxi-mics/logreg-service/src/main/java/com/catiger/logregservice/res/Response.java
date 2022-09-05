package com.catiger.logregservice.res;

import lombok.Data;

@Data
public class Response {
    private int code;
    private String msg;
    private String content;
    public Response(int code, String msg, Object obj) {
        this.code = code;
        this.content = JsonUtil.obj2Json(obj);
        this.msg = msg;
    }

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
