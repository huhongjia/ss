/*
 * $Id$ Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package com.ts.spider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * 
 * @author hongjia.hu
 * @date 2017-3-9
 */
public class SpiderMain {

    public static final int TIME_OUT = 60000;

    static {
        try {
            System.setProperty("sun.net.client.defaultConnectTimeout", TIME_OUT + "");
            System.setProperty("sun.net.client.defaultReadTimeout", TIME_OUT + "");
        } catch (Exception e) {
        }

    }

    public static void main(String[] args) throws Exception {

        Properties p = new SpiderMain().loadConfig(args);

        long s = System.currentTimeMillis();
        String cookie = login(p);
        long e = System.currentTimeMillis();

        String Bugzilla_logincookie = "";
        String[] arr = cookie.split(";");
        for (String tmp : arr) {
            String[] kvs = tmp.trim().split("=");
            if (kvs[0].equals("Bugzilla_logincookie")) {
                Bugzilla_logincookie = kvs[1];
                break;
            }
        }

        System.out.println("[Cookie]:" + Bugzilla_logincookie + ",others:" + cookie + ",cost:" + (e - s) / 100 + "s");

        if (Bugzilla_logincookie == "" || Bugzilla_logincookie == null) {
            System.out.println("登陆校验失败，请修改用户名或者密码重试!");
        }

        // long s = System.currentTimeMillis();
        // String content = spiderContent(from, to, Bugzilla_logincookie);
        // 生成文件
        // long e = System.currentTimeMillis();
        // String outFile = p.getProperty("data.dest") + File.separator + "bug_all.html";
        String fileName = p.getProperty("data.source");
        // FileUtils.writeFile(fileName, content);

        // System.out.println("Spider bug list,cost:"+(e-s)/1000+",生成文件："+outFile);

        new Analysis().analysis(fileName, p, Bugzilla_logincookie);
    }

    public Properties loadConfig(String[] args) throws Exception {
        Properties pro = new Properties();

        InputStream in = null;
        if (args == null || args.length == 0) {
            in = this.getClass().getClassLoader().getResourceAsStream("spider.properties");

        } else {
            in = new FileInputStream(new File(args[0]));
        }

        pro.load(in);
        in.close();

        return pro;
    }

    private static void analysis(String content) {
        // <div class="yui-dt-mask" style="display: none;"></div>
        System.out.println(content.indexOf("yui-dt-mask"));
    }

    /**
     * @param p
     * @return
     */
    private static String login(Properties p) throws Exception {
        URL url = new URL("http://bugzilla.unisoc.com/bugzilla/page.cgi");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);// 允许连接提交信息
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        connection.setRequestMethod("POST");// 网页默认“GET”提交方式
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        connection.setRequestProperty("Cache-Control", "max-age=0");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "bugzilla.spreadtrum.com");
        connection.setRequestProperty("Origin", "http://bugzilla.unisoc.com");
        connection.setRequestProperty("Referer", "http://bugzilla.unisoc.com/bugzilla/page.cgi");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

        StringBuffer sb = new StringBuffer();
        sb.append("Bugzilla_login=" + p.getProperty("login.username"));
        sb.append("&Bugzilla_password=" + p.getProperty("login.password"));
        sb.append("&Bugzilla_restrictlogin=on");
        sb.append("&GoAheadAndLogIn=Log in");
        // sb.append("&Bugzilla_login_token=1489829334-HTSZNH-rJBH-Yj8P72Fm0DP9vfRrWZFkAPcHzgJ61aI");
        connection.setRequestProperty("Content-Length", String.valueOf(sb.toString().length()));

        OutputStream os = connection.getOutputStream();
        os.write(sb.toString().getBytes());
        os.close();

        // 取Cookie
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String responseCookie = connection.getHeaderField("Set-Cookie");// 取到所用的Cookie

        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader br1 = new BufferedReader(isr);

        StringBuilder sb1 = new StringBuilder();
        String temp;
        while ((temp = br1.readLine()) != null) {
            sb1.append(temp).append("\n");
        }

        return responseCookie;

    }

    /**
     * @param from
     * @param to
     * @param cookie
     * @return
     * @throws Exception
     */
    private static String spiderContent(String from, String to, String cookie) throws Exception {

        URL url = new URL(
                "http://bugzilla.spreadtrum.com/bugzilla/page.cgi?id=productsummary.html&tab=&product=all&status=by_people_day&date_from="
                        + from + "&date_to=" + to + "&findby=Group&findrole=&grouplist=&show=Show");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");// 网页默认“GET”提交方式
        connection.setDoInput(true);
        connection.setDoOutput(true);// 允许连接提交信息
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        connection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "bugzilla.spreadtrum.com");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("Cookie", "DEFAULTFORMAT=specific; Bugzilla_login=2195; Bugzilla_logincookie="
                + cookie + "; PRODUCT_SUMMARY=all");
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

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
