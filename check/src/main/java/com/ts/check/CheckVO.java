package com.ts.check;

import java.util.Date;
import java.util.List;

public class CheckVO implements Comparable<CheckVO>{

    String date;
    
    Date checkDay;
    
    List<PersonVO> vos;
    
    public CheckVO(String date, List<PersonVO> vos, Date checkDay) {
        this.date = date;
        this.vos = vos;
        this.checkDay = checkDay;
    }

    public Date getCheckDay() {
        return checkDay;
    }

    public void setCheckDay(Date checkDay) {
        this.checkDay = checkDay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<PersonVO> getVos() {
        return vos;
    }

    public void setVos(List<PersonVO> vos) {
        this.vos = vos;
    }

    public int compareTo(CheckVO o) {
        if(this.getCheckDay().before(o.getCheckDay())){
            return -1;
        }
        
        return 1;
    }

}
