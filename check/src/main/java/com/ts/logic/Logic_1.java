/*
 * $Id$
 *
 * Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package com.ts.logic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ts.check.FileUtils;
import com.ts.config.PropertiesConfig;

/**
 * 
 * @author hongjia.hu
 * @date 2017-3-21
 */
public class Logic_1 {

    public static void main(String[] args) throws Exception {
        Properties p = new PropertiesConfig().loadConfig(args, "logic.properties");

        List<String> comments = FileUtils.loadFile(new File(p.getProperty("logic_1.file.comment")), true);
        List<String> groups = FileUtils.loadFile(new File(p.getProperty("logic_1.file.group")), true);
        Map<String, String> groupMap = new HashMap<String, String>();
        for (String data : groups) {
            String[] arr = data.split(",");
            groupMap.put(arr[1], arr[2]);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID").append(",").append("history.user_name").append("\r\n");
        for (String data : comments) {
            String[] arr = data.split(",");
            String userGroup = groupMap.get(arr[4]);
            String ownerGroup = groupMap.get(arr[5]);

            if (userGroup == null) {
                System.out.println("E列用户:" + arr[4] + "未找到Group信息，跳过比对，请完善！");
                continue;
            }

            if (ownerGroup == null) {
                System.out.println("E列用户:" + arr[5] + "未找到Group信息，跳过比对，请完善！");
                continue;
            }

            if (!userGroup.equals(ownerGroup)) {
                sb.append(arr[0]).append(",").append(arr[4]).append("\r\n");
            }
        }

        FileUtils.writeFile(p.getProperty("logic_1.file.result"), sb.toString());
    }
}
