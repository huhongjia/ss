package com.ts.check;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> loadFile(File file, boolean isHead) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        String temp = (isHead ? br.readLine() : null);
        List<String> result = new ArrayList<String>();
        while ((temp = br.readLine()) != null) {
            result.add(temp);
        }
        br.close();

        return result;
    }

    public static void writeFile(String outeFile, String data) throws Exception {
        File file = new File(outeFile);
        if(file.exists()){
            file.delete();
        }
        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "GBK");
        BufferedWriter bw = new BufferedWriter(write);
        bw.write(data);
        bw.close();

    }
    
    public static void writeFileAppend(File file, String data) throws Exception {
        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file,true), "GBK");
        BufferedWriter bw = new BufferedWriter(write);
        bw.write(data);
        bw.close();

    }

    public static void writeFileAppend(File file, String data,String encode) throws Exception {
        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file,true));
        BufferedWriter bw = new BufferedWriter(write);
        bw.write(data);
        bw.close();

    }

}
