package com.neo.sk.arachnez.commons.Object;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: jiameng
 * Date: 2014/9/18
 * Time: 17:42
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
public class JobInfo {
  private int id;
  private String ip;
  private String name;
  private int userId = 0;
  private long lastLaunch;//最近一次启动时间
  private int lastActivity = 2;//0运行，1休眠，2未启动
  private int isDelete = 0;//0未删，1删除
  private long aliveTime = 0;//存活时间，用于判断任务是否异常终止
  private InetAddress addr = null;

  public JobInfo(String name, int userId, long lastLaunch,
                 int lastActivity, int isDelete, long aliveTime) {
    try {
      this.addr = InetAddress.getLocalHost();
      this.ip = addr.getHostAddress().toString();
      this.name = name;
      this.userId = userId;
      this.lastLaunch = lastLaunch;
      this.lastActivity = lastActivity;
      this.isDelete = isDelete;
      this.aliveTime = aliveTime;
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  public int getId() {
    return id;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public long getLastLaunch() {
    return lastLaunch;
  }

  public void setLastLaunch(long lastLaunch) {
    this.lastLaunch = lastLaunch;
  }

  public int getLastActivity() {
    return lastActivity;
  }

  public void setLastActivity(int lastActivity) {
    this.lastActivity = lastActivity;
  }

  public int getIsDelete() {
    return isDelete;
  }

  public void setIsDelete(int isDelete) {
    this.isDelete = isDelete;
  }

  public long getAliveTime() {
    return aliveTime;
  }

  public void setAliveTime(long aliveTime) {
    this.aliveTime = aliveTime;
  }

  @Override
  public String toString() {
    return "JobInfo{" +
        "id=" + id +
        ", ip='" + ip + '\'' +
        ", name='" + name + '\'' +
        ", userId=" + userId +
        ", lastLaunch=" + lastLaunch +
        ", lastActivity=" + lastActivity +
        ", isDelete=" + isDelete +
        ", aliveTime=" + aliveTime +
        ", addr=" + addr +
        '}';
  }

  //  public static void main(String[] args){
//    JobInfo job = new JobInfo("smth", 0, "000000", "111111", 0);
//    System.out.println(job.toString());
//  }
}
