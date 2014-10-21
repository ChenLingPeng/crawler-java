package com.neo.sk.arachnez.commons;

import com.neo.sk.arachnez.util.DbUtil;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * User: jiameng
 * Date: 2014/9/19
 * Time: 16:44
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
public class SKData2Sql {
  private static final Logger logger = Logger.getLogger(SKData2Sql.class);
  private String jobName = null;

  public SKData2Sql(String jobName) {
    this.jobName = jobName;
  }

  public String getJobName() {
    return jobName;
  }

  public void insertDataByTime(long count, long startTime, long endTime) {
    try {
      if(startTime != 0){
        long time = System.currentTimeMillis();
        String sql = "Insert into ara_everytime (e_jobname,e_count,e_starttime,e_endTime) values(?,?,?,?)";
        Object[] params = {getJobName(), count, startTime, endTime};
        DbUtil.update(sql, params);
      }
    } catch (SQLException e) {
      logger.warn(e.getMessage());
      logger.warn(e);
    }
  }

  public void insertDataByHour(long count) {
    try {
      long time = System.currentTimeMillis();
      String sql = "Insert into ara_everyhour (e_jobname,e_count,e_time) values(?,?,?)";
      Object[] params = {getJobName(), count, time};
      DbUtil.update(sql, params);
    } catch (SQLException e) {
      logger.warn(e.getMessage());
      logger.warn(e);
    }
  }

  public void insertDataByDay(long count, long date) {
    try {
      String sql = "Insert into ara_everyday (e_jobname,e_count,e_time) values(?,?,?)";
      Object[] params = {getJobName(), count, date};
      DbUtil.update(sql, params);
    } catch (SQLException e) {
      logger.warn(e.getMessage());
      logger.warn(e);
    }
  }

  public void updateDataAll(long count) {
    try {
      String jobName = getJobName();
      long time = System.currentTimeMillis();
      if(isJobExistInAll()){
        String sql = "Update ara_all set e_count=?,e_time=? where e_jobname= ?";
        Object[] params = {count, time, jobName};
        DbUtil.update(sql, params);
      }else{
        String sql = "Insert into ara_all (e_jobname,e_count,e_time) values(?,?,?)";
        Object[] params = {jobName, count, time};
        DbUtil.update(sql, params);
      }
    } catch (SQLException e) {
      logger.warn(e.getMessage());
      logger.warn(e);
    }
  }

  private boolean isJobExistInAll() throws SQLException {
    String sql = "select * from ara_all where e_jobname = " + "'" + getJobName() + "'";
    List<Object[]> list = DbUtil.selectArrayList(sql);
    return list != null && list.size() != 0;
  }
}
