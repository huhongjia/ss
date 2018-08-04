package com.ts.spider;

import com.ts.check.FileUtils;
import com.ts.config.PropertiesConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analysis {
    public static void main(String[] args)
            throws Exception {
        Properties p = new PropertiesConfig().loadConfig(args, "spider.properties");

        String fileName = "D:\\data\\Product Dashboard  all.htm";
        new Analysis().analysis(fileName, p, "hNkt8GU3ib");
    }

    private static void processUserInfo(List<UserInfo> infos, Properties p, String cookie, String startDate, String endDate)
            throws Exception {
        String[] colums = p.getProperty("data.colums").split(",");
        System.out.println("***********<start spdier single user>**********");

        String outFile = p.getProperty("data.dest") + File.separator + "result.csv";
        File file = new File(outFile);
        if (file.exists()) {
            file.delete();
        }
        String detailFile = p.getProperty("data.dest") + File.separator + "result_detail.csv";
        File dfile = new File(detailFile);
        if (dfile.exists()) {
            dfile.delete();
        }
        for (int i = 0; i < infos.size(); i++) {
            UserInfo info = (UserInfo) infos.get(i);
            for (String clm : colums) {
                Meta meta = (Meta) info.getMetas().get(clm);
                if (meta != null) {
                    long s = System.currentTimeMillis();
                    List<BugInfo> bugs = null;
                    int retry = 0;
                    try {
                        bugs = spiderBugDetail(meta.getUrl(), p, cookie, info.getName());
                    } catch (Exception e) {
                        retry = 0;
                    }
                    while ((bugs == null) && (retry < 5)) {
                        long sleep = 2000L;
                        System.out.println("Spider Bug Cookie:" + cookie + "���������wait:" + sleep + "ms");
                        Thread.sleep(sleep);
                        String newCookie = CookieUtils.getCookie(p);
                        System.out.println("Spider Bug  Cookie:" + cookie + "���������������������������Cookie:" + newCookie + ",���������������" + ++retry);

                        bugs = spiderBugDetail(meta.getUrl(), p, newCookie, info.getName());
                        cookie = newCookie;
                    }
                    if ((bugs == null) || (bugs.size() == 0)) {
                        String data = "���������������" + info.getName() + "#" + meta.getUrl() + "\r\n";
                        String errorFile = p.getProperty("data.dest") + File.separator + "error.txt";
                        FileUtils.writeFile(errorFile, data);
                    }
                    long e = System.currentTimeMillis();

                    System.out.println("[" + i + "/" + infos.size() + "]" + info.getName() + " find " + clm + ":" + bugs
                            .size() + "  cost:" + (e - s) / 1000L + "s");

                    StringBuffer sb = new StringBuffer();
                    for (BugInfo bug : bugs) {
                        String[] clms = p.getProperty("data.detail.colums").split(",");
                        sb.append(info.getName()).append(",");
                        for (String cm : clms) {
                            sb.append((String) bug.getMetas().get(cm)).append(",");
                        }
                        sb.append(clm).append("\r\n");
                    }
                    FileUtils.writeFileAppend(file, sb.toString());

                    Boolean bool = Boolean.valueOf(p.getProperty("use.comment", "false"));
                    if ((clm.equals("COMMENT")) && (bool.booleanValue())) {
                        StringBuffer sbNew = new StringBuffer();
                        BugInfo bug;
                        String product;
                        String newCookie;
                        for (int j = 0; j < bugs.size(); j++) {
                            bug = (BugInfo) bugs.get(j);

                            product = (String) bug.getMetas().get("Product");
                            String url = "http://bugzilla.unisoc.com/bugzilla/show_bug.cgi?id=" + bug.getId();
                            System.out.println("[" + j + "/" + bugs.size() + "]Spider bug->" + bug.getId());

                            String data = spiderBugComment(url, p, cookie);
                            int ret = 0;
                            while ((data.indexOf("bz_comment_table") == -1) && (ret < 5)) {
                                long sleep = 2000L;
                                System.out.println("Cookie:" + cookie + "wait:" + sleep + "ms");
                                Thread.sleep(sleep);
                                newCookie = CookieUtils.getCookie(p);
                                System.out.println("Cookie:" + cookie + "Cookie:" + newCookie + ",���������������" + ++retry);
                                data = spiderBugComment(url, p, newCookie);
                                cookie = newCookie;
                            }
                            Map<String, List<BugComment>> comments = HtmlRegexpUtil.parseBugComment(data);
                            List<BugComment> userComments = (List) comments.get(info.getName());
                            if ((userComments != null) && (userComments.size() != 0)) {
                                for (BugComment comment : userComments) {
                                    Pattern p1 = Pattern.compile("\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\]");
                                    Matcher m1 = p1.matcher(comment.getDesc());
                                    if (m1.find()) {
                                        comment.setMark(Boolean.valueOf(true));
                                    }
                                    String date = comment.getTime().split(" ")[0];
                                    if (("".equals(startDate)) || ("".equals(endDate)) || (
                                            (date.compareTo(startDate) >= 0) && (date.compareTo(endDate) <= 0))) {
                                        sbNew.append(bug.getId()).append("|").append(comment.getUserName()).append("|").append(product).append("|").append(comment.getLabel()).append("|").append(date).append(" ").append(comment.getTime().split(" ")[1]).append("|").append(comment.getMark()).append("|").append(comment.getDesc()).append("\r\n");
                                    }
                                }
                            }
                        }
                        FileUtils.writeFileAppend(dfile, sbNew.toString(), "UTF-8");
                    }
                }
            }
        }
        System.out.println("***********<end spdier single user>**********");
    }

    private static String spiderBugComment(String url, Properties p, String cookie) {
        String data = "";
        try {
            data = spiderContent(url, cookie, p);
        } catch (Exception e) {
            int retryCnt = 0;
            while (retryCnt < 2) {
                System.out.println("[" + new Date() + "start retry���" + retryCnt + "���request url:" + url);
                try {
                    data = spiderContent(url, cookie, p);
                    if (!"".equals(data)) {
                        break;
                    }
                } catch (Exception localException1) {
                }
                retryCnt++;
            }
            if ("".equals(data)) {
                return "";
            }
        }
        return data;
    }

    private static List<BugInfo> spiderBugDetail(String url, Properties p, String cookie, String name)
            throws Exception {
        String data = "";
        try {
            data = spiderContent(url, cookie, p);
        } catch (Exception e) {
            int retryCnt = 1;
            while (retryCnt < 3) {
                System.out.println("[" + new Date() + "]start retry���" + retryCnt + "���request url:" + url);
                try {
                    data = spiderContent(url, cookie, p);
                    if (!"".equals(data)) {
                        break;
                    }
                } catch (Exception localException1) {
                }
                retryCnt++;
            }
            if ("".equals(data)) {
                return new ArrayList();
            }
        }
        String sp = "bz_buglist_header bz_first_buglist_header";
        data = data.substring(data.indexOf(sp) + sp.length() + 2);

        String entTag = "</tr>";
        int end = data.indexOf(entTag);
        if (end == -1) {
            return new ArrayList();
        }
        String head = data.substring(0, end);

        List<Meta> metas = HtmlRegexpUtil.parseATag(head);
        String content = data.substring(end + entTag.length(), data.indexOf("</table"));
        return HtmlRegexpUtil.parseBugDetail(content, metas);
    }

    private static String spiderContent(String urlStr, String cookie, Properties p)
            throws Exception {
        Integer timeOut = Integer.valueOf(p.getProperty("timeout", "60000"));

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeOut.intValue());
        connection.setReadTimeout(timeOut.intValue());
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "bugzilla.unisoc.com");
        connection.setRequestProperty("Bugzilla_restrictlogin", "on");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("Cookie", "LASTORDER=bug_status%2Cpriority%2Cassigned_to%2Cbug_id; Bugzilla_login=2195;Bugzilla_logincookie=" + cookie);
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


    private static List<Meta> analysisHead(String data) {
        String sp = "</colgroup>";
        data = data.substring(data.indexOf(sp) + sp.length());
        data = data.substring(data.indexOf("thead>") + 6, data.indexOf("</thead"));
        return HtmlRegexpUtil.parseATag(data);
    }

    private static List<UserInfo> analysisData(String data, List<Meta> metas) {
        String sp = "yui-dt-data";
        data = data.substring(data.indexOf(sp) + sp.length() + 2);
        data = data.substring(0, data.indexOf("tbody") - 2);
        return HtmlRegexpUtil.parseDIVATag(data, metas);
    }

    private static String loadFile(String fileName)
            throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
        String temp = null;
        StringBuffer sb = new StringBuffer();
        while ((temp = br.readLine()) != null) {
            sb.append(temp).append("\r\n");
        }
        return sb.toString();
    }

    public void analysis(String fileName, Properties p, String cookie)
            throws Exception {
        String content = loadFile(fileName);
        List<Meta> metas = analysisHead(content);
        List<UserInfo> infos = analysisData(content, metas);

        Matcher m1 = Pattern.compile("<input name=\"date_from\" size=\"10\" id=\"date_from\" value=\"(.*?)\"").matcher(content);
        String startDate = "";
        if (m1.find()) {
            startDate = m1.group(1);
        }
        Matcher m2 = Pattern.compile("<input name=\"date_to\" size=\"10\" id=\"date_to\" value=\"(.*?)\"").matcher(content);
        String endDate = "";
        if (m2.find()) {
            endDate = m2.group(1);
        }
        List<UserInfo> list = filterLocalUsers(infos, p);
        System.out.println("Persion size:" + infos.size() + ",after filter:" + list.size());

        processUserInfo(list, p, cookie, startDate, endDate);
    }

    private List<UserInfo> filterLocalUsers(List<UserInfo> infos, Properties p) {
        String[] users = p.getProperty("data.users").split(",");

        List<UserInfo> result = new ArrayList();
        for (UserInfo info : infos) {
            for (String uname : users) {
                if (info.getName().startsWith(uname)) {
                    result.add(info);
                }
            }
        }
        return result;
    }
}
