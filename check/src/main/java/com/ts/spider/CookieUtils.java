package com.ts.spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class CookieUtils {
    static {
        try {
            System.setProperty("sun.net.client.defaultConnectTimeout", "60000");
            System.setProperty("sun.net.client.defaultReadTimeout", "60000");
        } catch (Exception localException) {
        }
    }

    public static String getCookie(Properties p)
            throws Exception {
        String cookie = login(p);

        String Bugzilla_logincookie = "";
        String[] arr = cookie.split(";");
        for (String tmp : arr) {
            String[] kvs = tmp.trim().split("=");
            if (kvs[0].equals("Bugzilla_login_request_cookie")) {
                Bugzilla_logincookie = kvs[1];
                break;
            }
        }
        return Bugzilla_logincookie;
    }

    private static String login(Properties p)
            throws Exception {
        URL url = new URL("http://bugzilla.unisoc.com/bugzilla/page.cgi");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        connection.setRequestProperty("Cache-Control", "max-age=0");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "bugzilla.spreadtrum.com");
        connection.setRequestProperty("Origin", "http://bugzilla.unisoc.com");
        connection.setRequestProperty("Referer", "http://bugzilla.unisoc.com/bugzilla/page.cgi");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

        StringBuffer sb = new StringBuffer();
        sb.append("Bugzilla_login=" + p.getProperty("login.username"));
        sb.append("&Bugzilla_password=" + p.getProperty("login.password"));
        sb.append("&Bugzilla_restrictlogin=on");
        sb.append("&GoAheadAndLogIn=Log in");

        connection.setRequestProperty("Content-Length", String.valueOf(sb.toString().length()));

        OutputStream os = connection.getOutputStream();
        os.write(sb.toString().getBytes());
        os.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String responseCookie = connection.getHeaderField("Set-Cookie");

        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader br1 = new BufferedReader(isr);

        StringBuilder sb1 = new StringBuilder();
        String temp;
        while ((temp = br1.readLine()) != null) {
            sb1.append(temp).append("\n");
        }
        return responseCookie;
    }

}
