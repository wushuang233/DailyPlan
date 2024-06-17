package com.example.test;

// 定义一个与您的 JSON 响应匹配的 Java 类
public class LoginResponse {
    private int code;
    private String msg;
    private Data data;

    // 为 code、msg 和 data 提供 getter 和 setter 方法
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // 定义一个与 "data" 对象匹配的内部类
    public class Data {
        private String userId;
        private String token;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}