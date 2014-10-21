package com.neo.sk.arachnez.commons;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/9/2
 * Time: 12:53
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
 */
public class SKStatistic {
  private static final Logger logger = LoggerFactory.getLogger(SKStatistic.class);
  private String jobName = null;
  private long downSum; //全部下载总数
  private long itemSum; //全部增量总数
  private long downAdd; //一次下载数
  private long itemAdd; //一次增量数
//  private long dayAdd = 0l;//一天爬取总量
  private long startTime = 0l;//每次爬取开始时间
  private long endTime = 0l;//每次爬取结束时间
  private boolean flag = true;//每次开始爬取和结束的标志位，true代表开始新一轮爬取
  private String statistics;  //统计文件
  private SKData2Sql data2Sql = null;
//  private Timer timer = null;
//  private Calendar calendar = null;
//  private TimerTask dayTask = null;

  public SKStatistic(String jobName) {
    this.jobName = jobName;
    downSum = 0l;
    downAdd = 0l;
    itemSum = 0l;
    itemAdd = 0l;
    this.statistics = "statistics";
    data2Sql = new SKData2Sql(jobName);
//    timer = new Timer("Timer-statis-" + jobName);
//    dayTask = new DataByDayTask();
//    calendar = Calendar.getInstance();
//    calendar.set(Calendar.AM_PM, Calendar.PM);
//    calendar.set(Calendar.HOUR_OF_DAY, 23);
//    calendar.set(Calendar.MINUTE, 55);
//    calendar.set(Calendar.SECOND, 0);
//    calendar.set(Calendar.MILLISECOND, 0);
//    Date time = calendar.getTime();
////    timer.scheduleAtFixedRate(dayTask, time, 86400000);
//    timer.scheduleAtFixedRate(dayTask, time, 86400000);
  }

  public String getJobName() {
    return jobName;
  }

  public synchronized void addItems(long down, long size) {
    try {
      if (flag) {//如果是true，说明新一轮爬取刚开始，那么可以插入数据
        logger.info("new loop, time init");
        startTime = System.currentTimeMillis();
        flag = false;
      }
      downAdd = downAdd + down;
      itemAdd = itemAdd + size;
//      dayAdd = dayAdd + size;
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }

  }

  private void statiItems(String filePath) {
    downSum = downSum + downAdd;
    itemSum = itemSum + itemAdd;
    if(startTime == 0l){
      startTime = System.currentTimeMillis();
    }
    endTime = System.currentTimeMillis();
    data2Sql.insertDataByTime(itemAdd, startTime, endTime);//每次爬取的数量
    outputStatic(filePath);
    data2Sql.updateDataAll(itemSum);//总量
    downAdd = 0l;
    itemAdd = 0l;
    startTime = 0l;
    endTime = 0l;
    flag = true;
//        calculateStatic(jobName);
  }

  private void outputStatic(String filePath) {
    try {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
      File f = new File(filePath);
      if (!f.exists())
        f.mkdirs();
      OutputStreamWriter osw = new OutputStreamWriter(
          new FileOutputStream(filePath + statistics, true), "UTF-8");
      BufferedWriter writer = new BufferedWriter(osw);
      writer.write(df.format(new Date()) + " # " + downSum + " # " + itemSum
          + " # " + downAdd + " # " + itemAdd + "\n");
      writer.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }


//  class DataByDayTask extends TimerTask {
//    @Override
//    public void run() {
//      long date = System.currentTimeMillis();
//      logger.info("statistic day data start");
//      data2Sql.insertDataByDay(SKStatistic.this.dayAdd, date);
//      SKStatistic.this.dayAdd = 0l;
//    }
//  }

//    private void calculateStatic(String jobName) {
//        MyStatistic myStatistic = new MyStatistic();
//        myStatistic.getStatistics(this.statistics);
//        myStatistic.outputFile(this.statistics + "_" ,jobName);
//    }

//    private void deleteLogFile() {
//        MyLog.deleteLogFile();
//    }

  public synchronized void flushData(String filePath) {
    logger.info("flush statistic data!");
    statiItems(filePath);
//        deleteLogFile();
  }
}
