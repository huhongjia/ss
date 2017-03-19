package com.ts.spider;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    String name;

    Map<String, Meta> metas = new HashMap<String, Meta>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Meta> getMetas() {
        return metas;
    }

    public void setMetas(Map<String, Meta> metas) {
        this.metas = metas;
    }

}
