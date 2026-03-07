package com.example.aihelper.common.result;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static Result success(){
        Result result = new Result();
        result.code = 200;
        result.message = "success";
        result.data = null;
        return result;
    }
    public static Result success(Object data){
        Result result = new Result();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static Result error(String msg){
        Result result = new Result();
        result.code = 500;
        result.message = msg;
        return result;
    }

}