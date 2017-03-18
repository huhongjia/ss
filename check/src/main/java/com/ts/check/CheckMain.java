package com.ts.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CheckMain {

    static Map<String, String> fixDate = new HashMap<String, String>();

    static {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public static void main(String[] args) throws Exception {
        long s = System.currentTimeMillis();
        CheckMain check = new CheckMain();
        check.start(args);
        long e = System.currentTimeMillis();
        System.out.println("cost:" + (e - s) + "ms");
    }

    private void start(String[] args) throws Exception {
        Properties p = loadConfig(args);
        System.out.println(p.get("source"));

        String sourceFiles = p.getProperty("source");
        sourceFiles = new String(sourceFiles.getBytes("iso-8859-1"), "UTF-8");
        if (sourceFiles == null || "".equals(sourceFiles)) {
            System.out.println("config.properties中未配置source");
            return;
        }

        File base = new File(sourceFiles);
        if (!base.isDirectory()) {
            System.out.println("config.properties中source不是目录或者不存在");
            return;
        }

        String outPath = p.getProperty("dest");
        outPath = new String(outPath.getBytes("iso-8859-1"), "UTF-8");
        File dest = new File(outPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        // 文件处理
        Map<String/* 城市 */, List<CheckVO>> result = processFiles(base, p);

        Iterator<String> it = result.keySet().iterator();
        while (it.hasNext()) {
            String city = it.next();
            List<CheckVO> datas = result.get(city);
            Collections.sort(datas);

            StringBuffer sb = new StringBuffer();
            sb.append("姓名").append(",").append("Site").append(",").append("日期").append(",").append("上班时间").append(",")
                    .append("下班时间").append(",").append("工作日/周末").append(",").append("工作时长").append(",").append("加班时长")
                    .append("\r\n");
            for (CheckVO vo : datas) {
                String day = vo.getDate();

                List<PersonVO> list = vo.getVos();
                for (PersonVO pv : list) {
                    //跨天处理
                    resetCrossDay(pv, p, day);

                    //日期类型转换
                    convertDayType(pv, p, day);

                    sb.append(pv.getName()).append(",").append(pv.getSite()).append(",").append(day).append(",")
                            .append(pv.getStart()).append(",").append(pv.getEnd()).append(",")
                            .append(pv.getDayType().getDesc()).append(",").append(pv.getDiff()).append(",")
                            .append(pv.getOverTime()).append("\r\n");
                }
            }

            String outFile = outPath + File.separator + city + ".csv";
            FileUtils.writeFile(outFile, sb.toString());
            System.out.println("写入完毕：" + outFile);

        }
    }

    /**
     * @param pv
     * @param p
     * @param day
     * @throws ParseException
     */
    private void resetCrossDay(PersonVO pv, Properties p, String day) throws ParseException {
        String key = day + "#" + pv.getName();
        String date = fixDate.get(key);
        if (date != null && !"".equals(date)) {
            pv.setEnd(date);
        }

        if (pv.getEnd() == null || "".equals(pv.getEnd()) || pv.getStart() == null || "".equals(pv.getStart())) {
            return;
        }

        // 单位小时
        double diff = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pv.getEnd()).getTime() - new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").parse(pv.getStart()).getTime()) / (1000d * 60d * 60d);
        BigDecimal bd = new BigDecimal(diff);
        bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        pv.setDiff(bd.doubleValue());
    }

    private void convertDayType(PersonVO pv, Properties p, String day) {
        String weekend2workday = p.getProperty("weekend2workday");
        if (!"".equals(weekend2workday)) {
            String[] arr = weekend2workday.split(",");
            for (String date : arr) {
                if (day.equals(date)) {
                    pv.setDayType(DayType.WORKDAY);
                }
            }
        }

        String workday2weekend = p.getProperty("workday2weekend");
        if (!"".equals(workday2weekend)) {
            String[] arr = workday2weekend.split(",");
            for (String date : arr) {
                if (day.equals(date)) {
                    pv.setDayType(DayType.WEEKEND);
                }
            }
        }

        double overTime = pv.getDiff();
        if (pv.getDayType() == DayType.WORKDAY) {
            overTime = pv.getDiff() - Integer.valueOf(p.getProperty("work.hour"));
        }
        pv.setOverTime(overTime);
    }

    private Map<String/* 城市 */, List<CheckVO>> processFiles(File base, Properties p) throws Exception {
        File[] files = base.listFiles();
        System.out.println("文件总数：" + files.length);
        Map<String/* 城市 */, List<CheckVO>> result = new HashMap<String, List<CheckVO>>();

        for (File file : files) {
            String fileName = file.getName();
            if (!fileName.endsWith(".csv")) {
                continue;
            }

            String config = "";
            String site = "";
            if (fileName.startsWith("TSNJ")) {
                site = "NJ";
            } else if (fileName.startsWith("TSSH")) {
                site = "SH";
            } else if (fileName.contains("BeiJing")) {
                site = "BJ";
            } else if (fileName.contains("Shenzhen")) {
                site = "SZ";
            } else {
                site = "CD";
            }

            config = p.getProperty("ts." + site);
            List<CheckVO> siteChecks = result.get(site);
            if (siteChecks == null) {
                siteChecks = new ArrayList<CheckVO>();
                result.put(site, siteChecks);
            }

            String[] arr = config.split("#");

            List<String> attens = FileUtils.loadFile(file, Boolean.valueOf(arr[1]));
            if (attens.size() == 0) {
                continue;
            }

            Map<String, LinkedList<CheckOnVO>> checkMap = new HashMap<String, LinkedList<CheckOnVO>>();
            for (String atten : attens) {
                if ("".equals(atten)) {
                    continue;
                }

                atten = atten.replace("，", ",");

                String[] idx = arr[0].split(",");
                String[] atArr = atten.split(",");
                String name = atArr[Integer.valueOf(idx[0])].trim();
                String time = atArr[Integer.valueOf(idx[1])].trim().replace("/", "-");

                LinkedList<CheckOnVO> list = checkMap.get(name);
                if (list == null) {
                    list = new LinkedList<CheckOnVO>();
                    checkMap.put(name, list);
                }

                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
                } catch (Exception e) {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
                }
                CheckOnVO cv = new CheckOnVO(name, date);
                list.add(cv);
            }

            List<PersonVO> vos = new ArrayList<PersonVO>();
            if (checkMap.isEmpty()) {
                continue;
            }

            // 迭代
            Iterator<String> it = checkMap.keySet().iterator();
            String day = null;
            while (it.hasNext()) {
                String name = it.next();
                LinkedList<CheckOnVO> list = checkMap.get(name);
                Collections.sort(list);

                PersonVO pv = new PersonVO();
                pv.setName(name);

                //是否跨夜检查
                CheckOnVO firstCheck = list.get(0);
                while (checkOverNight(firstCheck.getCheckDate(), p)) {
                    list.pop();

                    String lastDay = DateUtils.getLastDay(firstCheck.getCheckDate());
                    //                    fixDate
                    String key = lastDay + "#" + name;
                    fixDate.put(key, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(firstCheck.getCheckDate()));

                    if (list.size() == 0) {
                        firstCheck = null;
                        break;
                    }
                    firstCheck = list.get(0);
                }

                if (firstCheck == null) {
                    continue;
                }

                pv.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(firstCheck.getCheckDate()));
                pv.setEnd(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(list.get(list.size() - 1).getCheckDate()));
                pv.setDayType(DateUtils.getDayDes(firstCheck.getCheckDate()));

                // 单位小时
                double diff = (list.get(list.size() - 1).getCheckDate().getTime() - firstCheck.getCheckDate().getTime())
                        / (1000d * 60d * 60d);
                BigDecimal bd = new BigDecimal(diff);
                bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
                pv.setDiff(bd.doubleValue());

                if (day == null) {
                    day = new SimpleDateFormat("yyyy-MM-dd").format(firstCheck.getCheckDate());
                }
                pv.setSite(site);
                vos.add(pv);
            }

            CheckVO cvs = new CheckVO(day, vos, new SimpleDateFormat("yyyy-MM-dd").parse(day));
            siteChecks.add(cvs);
        }

        return result;
    }

    /**
     * @param checkDate
     * @param p
     * @return
     * @throws ParseException
     */
    private boolean checkOverNight(Date checkDate, Properties p) throws ParseException {
        String day = new SimpleDateFormat("yyyy-MM-dd").format(checkDate);
        String overTime = p.getProperty("over.time", "04:30:00");
        Date overDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(day + " " + overTime);
        if (checkDate.before(overDate)) {
            return true;
        }

        return false;

    }

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
