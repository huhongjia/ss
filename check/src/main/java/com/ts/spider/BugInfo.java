package com.ts.spider;

import java.util.HashMap;
import java.util.Map;

public class BugInfo {
    Long id;
    
    Map<String,String> metas = new HashMap<String,String>();
    
    public Map<String, String> getMetas() {
        return metas;
    }

    public void setMetas(Map<String, String> metas) {
        this.metas = metas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
