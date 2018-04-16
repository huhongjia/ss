package com.ts.split;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ts.check.FileUtils;
import com.ts.config.PropertiesConfig;

/**
 * Created by hongjia.hu on 2017/9/11.
 */
public class SpliterMain {

    public static void main(String[] args) throws Exception {
        Properties p = new PropertiesConfig().loadConfig(args, "spliter.properties");

        String source = p.getProperty("source");
        if (source == null || source.equals("")) {
            System.out.println("请在spliter.properties配置source");
            return;
        }

        String dest = p.getProperty("dest");
        if (dest == null || dest.equals("")) {
            System.out.println("请在spliter.properties配置dest");
            return;
        }

        Map<String, StringBuffer> resultMap = new HashMap<String, StringBuffer>();
        List<String> origs = FileUtils.loadFile(new File(source), true);
        int i = 0;
        for (String tmp : origs) {
            i++;
            String[] arr = tmp.split(",");
            if (tmp.contains("时间") || arr.length < 5) {
                continue;
            }

            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(arr[4]);
            } catch (Exception e) {
                date = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(arr[4]);
            }
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);

            StringBuffer sb = resultMap.get(dateStr);
            if (sb == null) {
                sb = new StringBuffer("序号,卡号,姓名,部门班组,时间,地点,通过,描述\r\n");
                resultMap.put(dateStr, sb);
            }
            sb.append(tmp).append("\r\n");
        }

        Iterator<String> it = resultMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String data = resultMap.get(key).toString();
            String fileName = String.format("TS-BeiJing-%s.csv", key);
            String outFile = dest + File.separator + fileName;
            FileUtils.writeFile(outFile, data);

            System.out.println("[ok]" + outFile);
        }
    }
}
