package com.ts.check;

import java.util.Date;

public class CheckOnVO implements Comparable<CheckOnVO>{

    String name;
    
    Date checkDate;

    public CheckOnVO(String name, Date checkDate) {
        this.name = name;
        this.checkDate = checkDate; 
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public int compareTo(CheckOnVO o) {
        
        if(this.getCheckDate().before(o.getCheckDate())){
            return -1;
        }
        
        return 1;
    }
}
