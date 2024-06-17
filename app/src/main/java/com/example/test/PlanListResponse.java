package com.example.test;

import java.util.List;

public class PlanListResponse {
    private int code;
    private String msg;
    private List<PlanItem> data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public List<PlanItem> getData() {
        return data;
    }
}
