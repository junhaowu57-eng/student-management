package com.example.student_management.common;

public class Result {
    private int code;
    private String message;
    private Object data;
    public Result(Integer code,String message,Object data){
        this.code=code;
        this.message=message;
        this.data=data;
    }
    public static Result success(Object data){
        return new Result(200,"success",data);
    }
    public Integer getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }
    public Object getData(){
        return data;
    }
    public static Result error(Integer code,String message){
        return new Result(code,message,null);
    }
}
