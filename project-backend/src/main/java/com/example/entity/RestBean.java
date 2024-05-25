package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T>(int code, String msg, T data) {
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, "request success!", data);
    }

    public static <T> RestBean<T> success(T data, String msg) {
        return new RestBean<>(200, msg, data);
    }

    public static <T> RestBean<T> success() {
        return success(null);
    }
    public static <T> RestBean<T> fail(String msg) {
        return new RestBean<>(401, msg, null);
    }
    public static <T> RestBean<T> fail(int code, String msg) {
        return new RestBean<>(401, msg, null);
    }

    public String toJSONString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls); //處理NULL
    }
}
