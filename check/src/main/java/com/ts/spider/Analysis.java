package com.ts.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ts.check.FileUtils;
import com.ts.config.PropertiesConfig;

public class Analysis {

    public static void main(String[] args) throws Exception {
        Properties p = new PropertiesConfig().loadConfig(args);

        String fileName = "/Users/apple/git/ss/check/Product Dashboard_ all.htm";
        new Analysis().analysis(fileName, p, "iREd1fuUyB");
    }

    private static void processUserInfo(List<UserInfo> infos, Properties p, String cookie) throws Exception {
        String[] colums = p.getProperty("data.colums").split(",");
        System.out.println("***********<start spdier single user>**********");

        String outFile = p.getProperty("data.dest") + File.separator + "result.csv";
        File file = new File(outFile);
        if (file.exists()) {
            file.delete();
        }

        for (int i = 0; i < infos.size(); i++) {
            UserInfo info = infos.get(i);
            for (String clm : colums) {
                Meta meta = info.getMetas().get(clm);
                if (meta == null) {
                    continue;
                }

                // 抓取BUG详情
                List<BugInfo> bugs = spiderBugDetail(meta.getUrl(), p, cookie);
                System.out.println("[" + i + "/" + infos.size() + "]" + info.getName() + " find " + clm + ":"
                        + bugs.size());

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
            }
        }
        System.out.println("***********<end spdier single user>**********");
    }

    private static List<BugInfo> spiderBugDetail(String url, Properties p, String cookie) throws Exception {

        String data = spiderContent(url, cookie);

        // String fileName = "/Users/apple/git/ss/check/Bug List.htm";
        // String data = loadFile(fileName);
        String sp = "bz_buglist_header bz_first_buglist_header";
        data = data.substring(data.indexOf(sp) + sp.length() + 2);

        String entTag = "</tr>";
        int end = data.indexOf(entTag);
        String head = data.substring(0, end);

        List<Meta> metas = HtmlRegexpUtil.parseATag(head);
        String content = data.substring(end + entTag.length(), data.indexOf("</table"));
        return HtmlRegexpUtil.parseBugDetail(content, metas);
    }

    private static String spiderContent(String urlStr, String cookie) throws Exception {

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");// 网页默认“GET”提交方式
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
        connection
                .setRequestProperty("User-Agent",
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

        List<UserInfo> list = filterLocalUsers(infos, p);
        System.out.println("过滤前人员总数：" + infos.size() + ",过滤后：" + list.size());

        processUserInfo(list, p, cookie);
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
