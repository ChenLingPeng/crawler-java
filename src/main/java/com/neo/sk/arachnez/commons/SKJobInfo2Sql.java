package com.neo.sk.arachnez.commons;

import com.neo.sk.arachnez.commons.Object.JobInfo;
import com.neo.sk.arachnez.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jiameng
 * Date: 2014/9/18
 * Time: 17:24
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
public class SKJobInfo2Sql {
  private static final Logger logger = LoggerFactory.getLogger(SKJobInfo2Sql.class);
  private JobInfo job = null;
  private Timer timer = null;
  private TimerTask heartBeat = null;//心跳
  private String jobName = null;

  private Calendar calendar = null;
  private TimerTask dayTask = null;//每天数据记录

  public SKJobInfo2Sql(String jobName) {
    this.jobName = jobName;
    job = new JobInfo(jobName, 0, 0, 2, 0, 0);
    initJob();//初始化Job
    timer = new Timer("Timer-" + jobName);

    heartBeat = new HeartBeat();
    timer.schedule(heartBeat, 0, 10*1000);

    dayTask = new DataByDayTask();
    calendar = Calendar.getInstance();
    calendar.set(Calendar.AM_PM, Calendar.PM);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 55);
    calendar.set(Calendar.SECOND, 30);
    calendar.set(Calendar.MILLISECOND, 0);
    Date time = calendar.getTime();
//    timer.scheduleAtFixedRate(dayTask, time, 86400000);
    timer.scheduleAtFixedRate(dayTask, time, 86400000);
  }

  public Timer getTimer(){
    return timer;
  }

  public String getJobName(){
    return this.jobName;
  }

  public void initJob(){
    updateJob(this.job);
  }

  public void setRun(){
    this.job.setLastActivity(0);
    long launchTime = System.currentTimeMillis();//change
    this.job.setLastLaunch(launchTime);
    updateJob(this.job);
  }

  public void setSleep(){
    this.job.setLastActivity(1);
    updateJob(this.job);
  }

  public void setStop(){
    this.job.setLastActivity(2);
    updateJob(this.job);
  }

  public void updateJob(JobInfo job) {
      insertJob(job);
  }

  /**
   * 主要用于插入job，以及更新Job当前状态
   * @param job
   */
  private synchronized void insertJob(JobInfo job) {
    try {
      if (isJobExist(job.getName())) {//如果存在，更新
        String sql = "Update arachnez_jobs set job_lastLaunch=?," +
            "job_lastActivity=? ,job_alivetime=? where job_name= ?";
        Object[] params = {job.getLastLaunch(), job.getLastActivity(), job.getAliveTime(), job.getName()};
        DbUtil.update(sql, params);
      } else {
        String sql = "Insert into arachnez_jobs(job_ip,job_name,job_userid," +
            "job_lastActivity,job_isdelete,job_alivetime) values(?,?,?,?,?,?)";
        Object[] params = {job.getIp(), job.getName(),
            job.getUserId(), job.getLastActivity(), job.getIsDelete(), job.getAliveTime()};
        DbUtil.update(sql, params);
      }
    } catch (SQLException e) {
      logger.warn(e.getMessage(),e);
    }
  }

  private boolean isJobExist(String name) throws SQLException {
    String sql = "select * from arachnez_jobs where job_name = " + "'" + name + "'";
    List<Object[]> list = DbUtil.selectArrayList(sql);
    return list != null && list.size() != 0;
  }

  private long count(long startTime, long endTime, String jobName){
    try {
      String sql = "select SUM(e_count) from ara_everytime " +
          "where e_jobname = ? and e_starttime >= ? and e_endtime <= ?";
      Object[] params = {jobName, startTime, endTime};
      List<Object[]> list = DbUtil.selectArrayList(sql, params);
      if(list != null && list.size() != 0 )
        return ((BigDecimal)(list.get(0)[0])).longValue();
    } catch (SQLException e) {
      logger.warn(e.getMessage(), e);
    }
    return 0l;
  }

  private void insertDataByDay(String jobName, long count, long date){
    try {
      String sql = "Insert into ara_everyday (e_jobname,e_count,e_time) values(?,?,?)";
      Object[] params = {jobName, count, date};
      DbUtil.update(sql, params);
    } catch (SQLException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  private long getStartTime(){
    Calendar calendar = null;
    calendar = Calendar.getInstance();
    calendar.set(Calendar.AM_PM, Calendar.AM);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date time = calendar.getTime();
    return time.getTime();
  }

  private long getEndTime(){
    Calendar calendar = null;
    calendar = Calendar.getInstance();
    calendar.set(Calendar.AM_PM, Calendar.PM);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date time = calendar.getTime();
    return time.getTime();
  }

  class HeartBeat extends TimerTask {
    @Override
    public void run() {
      long date = System.currentTimeMillis();
//      logger.info("send a heart beat to job table");
      SKJobInfo2Sql.this.job.setAliveTime(date);
      updateJob(SKJobInfo2Sql.this.job);
    }
  }

  class DataByDayTask extends TimerTask {
    @Override
    public void run() {
      long start = getStartTime();
      long end = getEndTime();
      logger.info("statistic day data start");
      long c = SKJobInfo2Sql.this.count(start, end, getJobName());
      insertDataByDay(SKJobInfo2Sql.this.getJobName(), c, end);
    }
  }
}
