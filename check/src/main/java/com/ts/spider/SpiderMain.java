package com.ts.spider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class SpiderMain {
    public static void main(String[] args)
            throws Exception {
        Properties p = new SpiderMain().loadConfig(args);

        long s = System.currentTimeMillis();
        String cookie = CookieUtils.getCookie(p);
        long e = System.currentTimeMillis();

        String fileName = p.getProperty("data.source");
        new Analysis().analysis(fileName, p, cookie);
    }

    public Properties loadConfig(String[] args)
            throws Exception {
        Properties pro = new Properties();

        InputStream in = null;
        if ((args == null) || (args.length == 0)) {
            in = getClass().getClassLoader().getResourceAsStream("spider.properties");
        } else {
            in = new FileInputStream(new File(args[0]));
        }
        pro.load(in);
        in.close();

        return pro;
    }

    private static void analysis(String content) {
        System.out.println(content.indexOf("yui-dt-mask"));
    }

    private static String spiderContent(String from, String to, String cookie)
            throws Exception {
        URL url = new URL("https://bugzilla.spreadtrum.com/bugzilla/page.cgi?id=productsummary.html&tab=&product=all&status=by_people_day&date_from=" + from + "&date_to=" + to + "&findby=Group&findrole=&grouplist=&show=Show");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "bugzilla.spreadtrum.com");
        connection.setRequestProperty("Bugzilla_restrictlogin", "on");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("Cookie", "DEFAULTFORMAT=specific; Bugzilla_login=2195; Bugzilla_login_request_cookie=" + cookie + "; PRODUCT_SUMMARY=all");

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sbr = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sbr.append(temp);
        }
        return sbr.toString();
    }
}
