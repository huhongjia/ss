package com.ts.check;

public enum DayType {

    WORKDAY("工作日"), WEEKEND("周末");

    String desc;

    DayType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
