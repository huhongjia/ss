package com.ts.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ts.check.FileUtils;
import com.ts.config.PropertiesConfig;

public class Analysis {

    public static void main(String[] args) throws Exception {
        Properties p = new PropertiesConfig().loadConfig(args, "spider.properties");

        String fileName = "D:\\data\\Product Dashboard  all.htm";
        new Analysis().analysis(fileName, p, "hNkt8GU3ib");
    }

    private static void processUserInfo(List<UserInfo> infos, Properties p, String cookie, String startDate,
            String endDate) throws Exception {
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
            UserInfo info = infos.get(i);
            for (String clm : colums) {
                Meta meta = info.getMetas().get(clm);
                if (meta == null) {
                    continue;
                }

                // 抓取BUG详情
                long s = System.currentTimeMillis();
                List<BugInfo> bugs = spiderBugDetail(meta.getUrl(), p, cookie, info.getName());
                if (bugs == null || bugs.size() == 0) {
                    String data = "抓取失败：" + info.getName() + "#" + meta.getUrl() + "\r\n";
                    String errorFile = p.getProperty("data.dest") + File.separator + "error.txt";
                    FileUtils.writeFile(errorFile, data);
                }
                long e = System.currentTimeMillis();

                System.out.println("[" + i + "/" + infos.size() + "]" + info.getName() + " find " + clm + ":"
                        + bugs.size() + "  cost:" + (e - s) / 1000 + "s");

                StringBuffer sb = new StringBuffer();
                for (BugInfo bug : bugs) {
                    String[] clms = p.getProperty("data.detail.colums").split(",");
                    sb.append(info.getName()).append(",");
                    for (String cm : clms) {
                        sb.append(bug.getMetas().get(cm)).append(",");
                    }
                    sb.append(clm).append("\r\n");
                }

                // 追加写入
                FileUtils.writeFileAppend(file, sb.toString());

                Boolean bool = Boolean.valueOf(p.getProperty("use.comment", "false"));
                if (!clm.equals("COMMENT") || !bool) {
                    continue;
                }
                StringBuffer sbNew = new StringBuffer();
                for (int j = 0; j < bugs.size(); j++) {
                    BugInfo bug = bugs.get(j);

                    String product = bug.getMetas().get("Product");
                    String url = "http://bugzilla.spreadtrum.com/bugzilla/show_bug.cgi?id=" + bug.getId();
                    System.out.println("[" + j + "/" + bugs.size() + "]Spider bug->" + bug.getId());

                    Map<String, List<BugComment>> comments = spiderBugComment(url, p, cookie);
                    List<BugComment> userComments = comments.get(info.getName());
                    if (userComments == null || userComments.size() == 0) {
                        continue;
                    }

                    for (BugComment comment : userComments) {
                        Pattern p1 = Pattern.compile("\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\]");
                        Matcher m1 = p1.matcher(comment.getDesc());
                        if (m1.find()) {
                            comment.setMark(true);
                        }

                        String date = comment.getTime().split(" ")[0];
                        if (!"".equals(startDate) && !"".equals(endDate)) {
                            if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0) {
                                continue;
                            }
                        }

                        sbNew.append(bug.getId()).append("|").append(comment.getUserName()).append("|").append(product)
                                .append("|").append(comment.getLabel()).append("|").append(date).append(" ")
                                .append(comment.getTime().split(" ")[1]).append("|").append(comment.getMark())
                                .append("|").append(comment.getDesc()).append("\r\n");
                    }
                }
                // 追加写入
                FileUtils.writeFileAppend(dfile, sbNew.toString(), "UTF-8");
            }
        }
        System.out.println("***********<end spdier single user>**********");
    }

    /**
     * 抓取BUG 备注
     * 
     * @param p
     * @param cookie
     * @param url @return
     */
    private static Map<String, List<BugComment>> spiderBugComment(String url, Properties p, String cookie) {
        List<BugComment> comments = new ArrayList<BugComment>();

        String data = "";
        try {
            data = spiderContent(url, cookie, p);
        } catch (Exception e) {
            int retryCnt = 0;
            while (retryCnt < 2) {
                System.out.println("[" + new Date() + "start retry：" + retryCnt + "，request url:" + url);
                try {
                    data = spiderContent(url, cookie, p);
                    if (!"".equals(data)) {
                        break;
                    }
                } catch (Exception e2) {
                }

                retryCnt++;
            }

            if ("".equals(data)) {
                return new HashMap<String, List<BugComment>>();
            }
        }

        return HtmlRegexpUtil.parseBugComment(data);
    }

    private static List<BugInfo> spiderBugDetail(String url, Properties p, String cookie, String name)
            throws Exception {

        String data = "";
        try {
            data = spiderContent(url, cookie, p);
        } catch (Exception e) {
            int retryCnt = 1;
            while (retryCnt < 3) {
                System.out.println("[" + new Date() + "]start retry：" + retryCnt + "，request url:" + url);
                try {
                    data = spiderContent(url, cookie, p);
                    if (!"".equals(data)) {
                        break;
                    }
                } catch (Exception e2) {
                }

                retryCnt++;
            }

            if ("".equals(data)) {
                return new ArrayList<BugInfo>();
            }

        }

        // String fileName = "/Users/apple/git/ss/check/Bug List.htm";
        // String data = loadFile(fileName);
        String sp = "bz_buglist_header bz_first_buglist_header";
        data = data.substring(data.indexOf(sp) + sp.length() + 2);

        String entTag = "</tr>";
        int end = data.indexOf(entTag);
        if (end == -1) {
            return new ArrayList<BugInfo>();
        }
        String head = data.substring(0, end);

        List<Meta> metas = HtmlRegexpUtil.parseATag(head);
        String content = data.substring(end + entTag.length(), data.indexOf("</table"));
        return HtmlRegexpUtil.parseBugDetail(content, metas);
    }

    private static String spiderContent(String urlStr, String cookie, Properties p) throws Exception {
        Integer timeOut = Integer.valueOf(p.getProperty("timeout", SpiderMain.TIME_OUT + ""));

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");// 网页默认“GET”提交方式
        connection.setConnectTimeout(timeOut);
        connection.setReadTimeout(timeOut);
        connection.setDoInput(true);
        connection.setDoOutput(true);// 允许连接提交信息
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

    private static String loadFile(String fileName) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
        String temp = null;
        StringBuffer sb = new StringBuffer();
        while ((temp = br.readLine()) != null) {
            sb.append(temp).append("\r\n");
        }

        return sb.toString();
    }

    public void analysis(String fileName, Properties p, String cookie) throws Exception {
        String content = loadFile(fileName);
        List<Meta> metas = analysisHead(content);
        List<UserInfo> infos = analysisData(content, metas);

        Matcher m1 = Pattern.compile("<input name=\"date_from\" size=\"10\" id=\"date_from\" value=\"(.*?)\"")
                .matcher(content);
        String startDate = "";
        if (m1.find()) {
            startDate = m1.group(1);
        }

        Matcher m2 = Pattern.compile("<input name=\"date_to\" size=\"10\" id=\"date_to\" value=\"(.*?)\"")
                .matcher(content);
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

        List<UserInfo> result = new ArrayList<UserInfo>();
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
