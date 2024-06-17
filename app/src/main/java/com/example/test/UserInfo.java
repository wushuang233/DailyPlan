package com.example.test;

public class UserInfo {
    private int code;
    private String msg;
    private Data data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String username;
        private int sex;
        private int age;
        private int plan_number;
        private String introduction;

        public String getUsername() {
            return username;
        }

        public int getSex() {
            return sex;
        }

        public int getAge() {
            return age;
        }

        public int getPlanNumber() {
            return plan_number;
        }

        public String getIntroduction() {
            return introduction;
        }
    }
}
