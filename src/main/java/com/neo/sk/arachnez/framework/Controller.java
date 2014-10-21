package com.neo.sk.arachnez.framework;

import com.neo.sk.arachnez.commons.SKProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/8/25
 * Time: 15:36
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
public class Controller {
  private static final Logger logger = LoggerFactory.getLogger(Controller.class);
  private List<String> jobList = null;
  private HashMap<String, String> lastModiMap = null;
  private static Controller instance = null;
  //    private String jobsPath = "/src/main/resources/jobs";//本地测试用
  private String jobsPath = "/jobs";
  private String basePath = null;
  private long delayTime = 2 * 60 * 1000;
  private Timer timer = null;
  TimerTask task = null;

  private Controller() {
    basePath = System.getProperty("user.dir") + jobsPath;
    jobList = new ArrayList<>();
    lastModiMap = new HashMap<>();
    timer = new Timer();
    task = new InitTask(basePath);
    timer.schedule(task, 60*1000, delayTime);
  }

  public static Controller getInstance() {
    if (instance == null) {
      synchronized (Controller.class) {
        if (instance == null) {
          instance = new Controller();
        }
      }
    }
    return instance;
  }

  public void init(String basePath) {
    File f = new File(basePath);
    if (f.exists()) {
      for (File ff : f.listFiles()) {
        try{
          int flag = 0;
          String seedsPath = ff.getPath() + "/seeds.txt";
          String seeds = null;
          String prop = null;
          String smth = null;
          String total = null;
          jobList.add(ff.getName());
          for(File fi : ff.listFiles()){
//            logger.info(fi.getName());
            if(fi.getName().equals("seeds.txt")){
              seeds = Long.toString(fi.lastModified());
//            logger.info(seeds);
              flag++;
            }else if(fi.getName().equals("skspider.properties")){
              prop = Long.toString(fi.lastModified());
//            logger.info(prop);
              flag++;
            }else if(fi.getName().contains(".jar")){
              smth = Long.toString(fi.lastModified());
//            logger.info(smth);
              flag++;
            }
          }
          if(flag == 3)
            total = seeds + prop + smth;
          else
            continue;
//        logger.info("now " + total);
//        logger.info("before " + lastModiMap.get(ff.getName()));
          if (lastModiMap.containsKey(ff.getName()) && lastModiMap.get(ff.getName()).equals(total)) {
//          logger.info("This jobdir is not modified!");
            continue;
          } else {
            SKProperties sp = new SKProperties(ff.getPath());
            if (!sp.isUsable()) continue;
            File sf = new File(seedsPath);
            if (!sf.exists()) continue;
            lastModiMap.put(ff.getName(), total);
            SKEngine.addJob(sp, sf);
            logger.info("This jobdir[" + ff.getName() + "] is modified and add it to SKEngine");
          }
        }catch (Exception e){
          logger.warn(e.getMessage(),e);
        }
      }
    }
  }

  class InitTask extends TimerTask {
    private String bPath = null;

    InitTask(String basePath) {
      this.bPath = basePath;
    }

    @Override
    public void run() {
      logger.info("Controller TimerTask start!");
      init(bPath);
//      logger.info("Controller TimerTask over!");
    }
  }

  public List<String> getJobList() {
    return jobList;
  }

  public HashMap<String, String> getLastModiMap() {
    return lastModiMap;
  }

  public static void main(String[] args) {
//    Controller con = Controller.getInstance();
//    File f = new File("E:\\Arachnez\\jobs");
//    if (f.exists()) {
//      for (File ff : f.listFiles()) {
//        System.out.println(ff.getName());
//        int flag = 0;
//        String seeds;
//        String prop;
//        String smth;
//        String total = null;
//        for(File fi : ff.listFiles()){
//          if(fi.getName().equals("seeds.txt")){
//            System.out.println("=="+fi.lastModified());
//            seeds = Long.toString(fi.lastModified());
//            System.out.println("--"+seeds);
//            flag++;
//          }
//          if(fi.getName().equals("skspider.properties")){
//            prop = Long.toString(fi.lastModified());
//            System.out.println(prop);
//            flag++;
//          }
//          if(fi.getName().equals("smth.jar")){
//            smth = Long.toString(fi.lastModified());
//            System.out.println(smth);
//            flag++;
//          }
////          System.out.println(fi.getName());
//        }
//        if(flag != 3) continue;
//        System.out.println("huima");
//      }
//    }
  }
}
