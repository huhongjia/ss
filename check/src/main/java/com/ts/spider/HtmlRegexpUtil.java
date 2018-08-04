package com.ts.spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlRegexpUtil
{
    private static final String regxpForHtml = "<([^>]*)>";
    private static final String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*>";
    private static final String regxpForImaTagSrcAttrib = "src=\"([^\"]+)\"";
    private static final String a_tag = "<a(.*?)>(.*?)</a>";

    public static List<Meta> parseATag(String content)
    {
        Pattern pattern = Pattern.compile("<a(.*?)>(.*?)</a>");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        List<Meta> data = new ArrayList();
        int i = 0;
        while (result1)
        {
            matcher.appendReplacement(sb, "");
            String tmp = matcher.group(2);
            if (tmp.trim().length() > 0)
            {
                int idx = tmp.indexOf("<span");
                if (idx != -1) {
                    tmp = tmp.substring(0, idx);
                }
                Meta meta = new Meta("", i, tmp);
                data.add(meta);
            }
            result1 = matcher.find();
            i++;
        }
        matcher.appendTail(sb);
        return data;
    }

    public String replaceTag(String input)
    {
        if (!hasSpecialChars(input)) {
            return input;
        }
        StringBuffer filtered = new StringBuffer(input.length());
        for (int i = 0; i <= input.length() - 1; i++)
        {
            char c = input.charAt(i);
            switch (c)
            {
                case '<':
                    filtered.append("&lt;");
                    break;
                case '>':
                    filtered.append("&gt;");
                    break;
                case '"':
                    filtered.append("&quot;");
                    break;
                case '&':
                    filtered.append("&amp;");
                    break;
                default:
                    filtered.append(c);
            }
        }
        return filtered.toString();
    }

    public boolean hasSpecialChars(String input)
    {
        boolean flag = false;
        if ((input != null) && (input.length() > 0)) {
            for (int i = 0; i <= input.length() - 1; i++)
            {
                char c = input.charAt(i);
                switch (c)
                {
                    case '>':
                        flag = true;
                        break;
                    case '<':
                        flag = true;
                        break;
                    case '"':
                        flag = true;
                        break;
                    case '&':
                        flag = true;
                }
            }
        }
        return flag;
    }

    public static String filterHtml(String str)
    {
        Pattern pattern = Pattern.compile("<([^>]*)>");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1)
        {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String fiterHtmlTag(String str, String tag)
    {
        String regxp = "<\\s*" + tag + "\\s+([^>]*)\\s*>";
        Pattern pattern = Pattern.compile(regxp);
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1)
        {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String replaceHtmlTag(String str, String beforeTag, String tagAttrib, String startTag, String endTag)
    {
        String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)\\s*>";
        String regxpForTagAttrib = tagAttrib + "=\"([^\"]+)\"";
        Pattern patternForTag = Pattern.compile(regxpForTag);
        Pattern patternForAttrib = Pattern.compile(regxpForTagAttrib);
        Matcher matcherForTag = patternForTag.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result = matcherForTag.find();
        while (result)
        {
            StringBuffer sbreplace = new StringBuffer();
            Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag.group(1));
            if (matcherForAttrib.find()) {
                matcherForAttrib.appendReplacement(sbreplace, startTag + matcherForAttrib.group(1) + endTag);
            }
            matcherForTag.appendReplacement(sb, sbreplace.toString());
            result = matcherForTag.find();
        }
        matcherForTag.appendTail(sb);
        return sb.toString();
    }

    public static List<UserInfo> parseDIVATag(String content, List<Meta> metas)
    {
        Pattern pattern = Pattern.compile("<div(.*?)>(.*?)</div>");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        List<UserInfo> data = new ArrayList();
        int i = 0;

        UserInfo info = null;
        while (result1)
        {
            matcher.appendReplacement(sb, "");
            String tmp = matcher.group(2);
            if ("Total".equals(tmp)) {
                break;
            }
            if (tmp.contains("@"))
            {
                info = new UserInfo();
                info.setName(tmp);
                data.add(info);
            }
            else if (tmp.contains("href"))
            {
                for (Meta meta : metas) {
                    if (meta.getIndex() == i)
                    {
                        String url = matchLink(tmp);

                        Meta mt = new Meta(url, meta.getIndex(), meta.getName());
                        info.getMetas().put(mt.getName(), mt);
                        break;
                    }
                }
            }
            result1 = matcher.find();
            if (i >= metas.size()) {
                i = 0;
            } else {
                i++;
            }
        }
        matcher.appendTail(sb);
        return data;
    }

    private static String matchLink(String content)
    {
        Pattern pattern = Pattern.compile("<a href=\"(.*?)\"(.*?)>(.*?)</a>");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        if (result1)
        {
            matcher.appendReplacement(sb, "");
            return matcher.group(1);
        }
        matcher.appendTail(sb);
        return null;
    }

    public static List<BugInfo> parseBugDetail(String content, List<Meta> metas)
    {
        content = content.replace("\r", "").replace("\n", "");
        Pattern pattern = Pattern.compile("<td(.*?)>(.*?)</td>");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        List<BugInfo> data = new ArrayList();
        int i = 0;

        BugInfo info = null;
        while (result1)
        {
            matcher.appendReplacement(sb, "");
            String tmp = matcher.group(2).trim();
            Pattern p;
            if (i == 0)
            {
                info = new BugInfo();
                data.add(info);
            }
            else
            {
                if (i == 1)
                {
                    p = Pattern.compile("<a(.*?)>(.*?)</a>");
                    Matcher m = p.matcher(tmp);
                    if (m.find()) {
                        tmp = m.group(2);
                    }
                    info.setId(Long.valueOf(tmp));
                }
                for (Meta meta : metas) {
                    if (meta.getIndex() + 1 == i)
                    {
                        if (tmp.contains("span"))
                        {
                            Pattern pt = Pattern.compile("<span title=\"(.*?)\">(.*?)</span>");
                            Matcher m = pt.matcher(tmp);
                            if (m.find()) {
                                tmp = m.group(1);
                            }
                        }
                        info.getMetas().put(meta.getName(), tmp.replace("&#64;", "@"));
                        break;
                    }
                }
            }
            result1 = matcher.find();
            if (i >= metas.size()) {
                i = 0;
            } else {
                i++;
            }
        }
        matcher.appendTail(sb);
        return data;
    }

    public static Map<String, List<BugComment>> parseBugComment(String content)
    {
        Map<String, List<BugComment>> map = new HashMap();

        content = content.replace("\r", "").replace("\n", "");

        content = content.substring(content.indexOf("bz_comment_table"));
        content = content.substring(0, content.indexOf("</table>"));

        Pattern p1 = Pattern.compile("<a class=\"email\" href=\"mailto:(.*?)\"");
        Matcher m1 = p1.matcher(content);
        Pattern p2 = Pattern.compile("<span class=\"bz_comment_time\">(.*?)</span>");
        Matcher m2 = p2.matcher(content);
        Pattern p3 = Pattern.compile("<pre class=\"bz_comment_text\"(.*?)>(.*?)</pre>");
        Matcher m3 = p3.matcher(content);

        Pattern p4 = Pattern.compile("<span class=\"bz_comment_number\">(.*?)<a(.*?)href=\"(.*?)\">(.*?)</a>(.*?)</span>");
        Matcher m4 = p4.matcher(content);
        while ((m1.find()) && (m2.find()) && (m3.find()) && (m4.find()))
        {
            String name = m1.group(1).trim().replace("&#64;", "@");
            String time = m2.group(1).trim();
            String desc = m3.group(2).trim();
            String label = m4.group(4).trim();
            if (!label.equals("Description"))
            {
                BugComment comment = new BugComment(name, time, desc, label);

                List<BugComment> list = (List)map.get(comment.getUserName());
                if (list == null)
                {
                    list = new ArrayList();
                    map.put(comment.getUserName(), list);
                }
                list.add(comment);
            }
        }
        return map;
    }
}
