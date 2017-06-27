package com.ts.spider;

/**
 * Created by hongjia.hu on 2017/6/27.
 */
public class BugComment {
    String userName;

    String desc;

    String time;

    String label;

    Boolean mark = false;

    public BugComment(String name, String time, String desc, String label) {
        this.userName = name;
        this.time = time;
        this.desc = desc;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Boolean getMark() {
        return mark;
    }

    public void setMark(Boolean mark) {
        this.mark = mark;
    }
}
