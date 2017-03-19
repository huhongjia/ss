package com.ts.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfig {
    public Properties loadConfig(String[] args) throws Exception {
        Properties pro = new Properties();

        InputStream in = null;
        if (args == null || args.length == 0) {
            in = this.getClass().getClassLoader().getResourceAsStream("config.properties");

        } else {
            in = new FileInputStream(new File(args[0]));
        }

        pro.load(in);
        in.close();

        return pro;
    }
}
