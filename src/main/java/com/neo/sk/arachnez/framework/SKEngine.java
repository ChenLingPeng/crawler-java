package com.neo.sk.arachnez.framework;

import com.neo.sk.arachnez.commons.SKProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/8/26
 * Time: 10:58
 * <p/>
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                            O\ = /O
 * //                        ____/`---'\____
 * //                      .   ' \\| |// `.
 * //                       / \\||| : |||// \
 * //                     / _||||| -:- |||||- \
 * //                       | | \\\ - /// | |
 * //                     | \_| ''\---/'' | |
 * //                      \ .-\__ `-` ___/-. /
 * //                   ___`. .' /--.--\ `. . __
 * //                ."" '< `.___\_<|>_/___.' >'"".
 * //               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //                 \ \ `-. \_ __\ /__ _/ .-` / /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //
 * //         .............................................
 * //
 * //   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗
 * // ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝
 * // ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗
 * // ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║
 * // ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝
 * //  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝
 * //  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░
 * //  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░
 * //           ░     ░ ░      ░  ░
 * //
 */
public class SKEngine {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SKEngine.class);
  private static Map<String, SKJob> jobs = new ConcurrentHashMap<>();

  static void removeJob(SKJob job){
    jobs.remove(job.getJobName());
  }

  static void addJob(SKProperties propertyFile, File seedFile){
    String jobName = propertyFile.getJobName();
    SKJob oldJob = jobs.get(jobName);
    if(oldJob!=null){
      oldJob.kill();
      jobs.remove(jobName);
    }

    SKJob skJob = new SKJob(propertyFile);
    try {
      List<String> seeds = FileUtils.readLines(seedFile);
      skJob.setSeeds(seeds);
      jobs.put(jobName, skJob);
      Thread t = new Thread(skJob,jobName+"-thread");
      t.start();
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  public static void addUrl(String jobName, String url){
    SKJob skJob = jobs.get(jobName);
    if(skJob!=null){
      skJob.addUrl(url);
    }
  }
}
