package com.neo.sk.arachnez.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/8/26
 * Time: 16:46
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃
 * 　　┃　　　┃
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 */
public class SKContext {
    private static final Logger logger = LoggerFactory.getLogger(SKContext.class);
    private String MAXFILE = null;
    private String basePath = System.getProperty("user.dir");

    public SKContext(String fileName){
        MAXFILE = fileName;
    }

    //读写各类最大文章id的函数
    public Map<String, Object> GetOldMaxArticleList() {
        return GetOldMaxArticleListFromFile();
    }

    private Map<String, Object> GetOldMaxArticleListFromFile() {
        Map<String, Object> maxList = new ConcurrentHashMap<>();
        try {
            //TODO need to change the path
//            File file = new File("E:\\Arachnez\\src\\main\\resources\\jobs\\" + MAXFILE);//获取保存各类最大文章id的文件,本地用这个路径
//            File file = new File(basePath + "/src/main/resources/jobs/" + MAXFILE);//获取保存各类最大文章id的文件
            File file = new File(basePath + "/jobs/" + MAXFILE);//获取保存各类最大文章id的文件
            if(file.exists()) {
                BufferedReader reader;
                reader = new BufferedReader(new FileReader(file));
                String line = null;
                while((line = reader.readLine()) != null){
                    maxList.put(line.split(":")[0], Long.parseLong(line.split(":")[1]));
                }
                reader.close();
            }else{
                file.createNewFile();
            }
        } catch(IOException e) {
          logger.warn(e.getMessage(), e);
//            e.printStackTrace();
        }
        return maxList;
    }

    public void saveMaxList(Map<String, Object> newMaxArticleList) {
        logger.info("set max idlist ");
        writeIdToFile(newMaxArticleList);
    }

    public synchronized void writeIdToFile(Map<String, Object> maxlist) {
        try {
//            File file = new File("E:\\Arachnez\\src\\main\\resources\\jobs\\" + MAXFILE);//获取保存各类最大文章id的文件，本地用这个
//            File file = new File(basePath + "/src/main/resources/jobs/" + MAXFILE);//获取保存各类最大文章id的文件
            File file = new File(basePath + "/jobs/" + MAXFILE);//获取保存各类最大文章id的文件
            if (file.exists()) {
                BufferedWriter writer = null;
                writer = new BufferedWriter(new FileWriter(file));
                StringBuffer sb = new StringBuffer("");
                for (String category : maxlist.keySet()) {
                    sb.append(category).append(":").append(String.valueOf(maxlist.get(category))).append("\n");
                }
                writer.write(sb.toString());
                writer.close();
            }
        } catch (IOException e) {
          logger.warn(e.getMessage(), e);
//            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        SKContext sk = new SKContext("smth\\maxfile_test.txt");
        sk.GetOldMaxArticleList();
    }
}
