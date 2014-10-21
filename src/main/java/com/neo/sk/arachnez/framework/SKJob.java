package com.neo.sk.arachnez.framework;

import chen.bupt.httpclient.commons.Constants;
import chen.bupt.httpclient.utils.InputStreamUtils;
import chen.bupt.httpclient.utils.ResponseUtils;
import com.neo.sk.arachnez.client.SKClientJob;
import com.neo.sk.arachnez.commons.SKDeque;
import com.neo.sk.arachnez.commons.SKJobInfo2Sql;
import com.neo.sk.arachnez.commons.SKProperties;
import com.neo.sk.arachnez.commons.SKURL;
import com.neo.sk.arachnez.proxy.ProxyPool;
import com.neo.sk.arachnez.util.URLUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/8/26
 * Time: 11:42
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
public class SKJob implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(SKJob.class);
  private static final int POOLSIZE = 200;
  private static final int INC = 1;

  private SKProperties properties;
  private List<SKURL> seeds;
  private CloseableHttpClient userclient;
  private SKClientJob clientJob;
  private SKDeque<SKURL> urlPool;
  private boolean isactive;
  private boolean runnable;
  private SKJobInfo2Sql jobInfo2Sql = null;//change
//  private List<DefaultHttpClient> clients;

  public SKJob(SKProperties properties) {
    this.properties = properties;
    this.urlPool = new SKDeque<>();
    this.isactive = false;
    this.seeds = new ArrayList<>();
    this.setClientJob(properties.getSKCJob());
    this.setHttpClient(properties.getSKCJob().getCookieStore());
    this.runnable = true;
    jobInfo2Sql = new SKJobInfo2Sql(getJobName());//change
  }

  private void setHttpClient(CookieStore cookieStore) {
    MessageConstraints messageConstraints = MessageConstraints.custom()
        .setMaxHeaderCount(200)
        .setMaxLineLength(5000)
        .build();

    ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setMalformedInputAction(CodingErrorAction.IGNORE)
        .setUnmappableInputAction(CodingErrorAction.IGNORE)
        .setCharset(Consts.UTF_8)
        .setBufferSize(64 * 1024)
        .setMessageConstraints(messageConstraints)
        .build();

    RequestConfig globalConfig = RequestConfig.custom()
        .setCookieSpec(CookieSpecs.BEST_MATCH)
        .setCircularRedirectsAllowed(false)
        .setRedirectsEnabled(false)
        .setConnectTimeout(10000)
        .setSocketTimeout(10000)
//        .setConnectionRequestTimeout(10000)
        .build();

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(this.properties.getThreadNum() + 5);
    cm.setDefaultMaxPerRoute(this.properties.getThreadNum() + 5);
    cm.setDefaultConnectionConfig(connectionConfig);
    List<Header> headers = new ArrayList<>(4);
    headers.add(new BasicHeader("User-Agent", Constants.defaultUserAgent));
    headers.add(new BasicHeader("Accept", Constants.defaultAccept));
    headers.add(new BasicHeader("Accept-Encoding", Constants.defaultAcceptEncoding));
    headers.add(new BasicHeader("Accept-Language", Constants.defaultAcceptLanguage));

    userclient = HttpClients.custom().setConnectionManager(cm)
        .setDefaultRequestConfig(globalConfig)
        .setDefaultCookieStore(cookieStore)
        .setRetryHandler(new HttpRequestRetryHandler() {
          @Override
          public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= 2) {
              // Do not retry if over max retry count
              return false;
            }
            if (exception instanceof InterruptedIOException) {
              // Timeout
              return false;
            }
            if (exception instanceof UnknownHostException) {
              // Unknown host
              return false;
            }
            if (exception instanceof ConnectTimeoutException) {
              // Connection refused
              return false;
            }
            if (exception instanceof SSLException) {
              // SSL handshake exception
              return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
              // Retry if the request is considered idempotent
              return true;
            }
            return false;
          }
        })
//        .setRetryHandler(new DefaultHttpRequestRetryHandler())
        .setDefaultHeaders(headers)
        .build();

  }

  public void setClientJob(SKClientJob clientJob) {
    this.clientJob = clientJob;
  }

  public String getJobName() {
    return properties.getJobName();
  }

  void setSeeds(List<String> seeds) {
    this.seeds.clear();
    for (String seed : seeds) {
      seed = seed.trim();
      if (!"".equals(seed) && !seed.startsWith("#")) {
        SKURL skurl = new SKURL(seed, true);
        this.seeds.add(skurl);
      }
    }
  }

  @Override
  public void run() {
    Thread[] threads = new Thread[this.properties.getThreadNum()];
//    Timer timer = new Timer("adjust");
//    SizeAdjustTask task = new SizeAdjustTask();
//    timer.schedule(task, 10 * 60 * 1000, 10 * 60 * 1000);
    while (true) {
      jobInfo2Sql.setRun();
      logger.info(this.getJobName() + "=running，update job table set activity=0");
      this.clientJob.initContext();
      urlPool.clear();
      URLUtils.clearJobURL(this.getJobName());
//      int startInd = 0;

      urlPool.addAll(seeds);
//      urlPool.addAll(seeds.subList(startInd, Math.min(startInd + INC, seeds.size())));
//      urlPool.addFirst(seeds.get(startInd));
//      startInd++;
      for (int i = 0; i < this.properties.getThreadNum(); i++) {
        threads[i] = new Thread(new CoreThread(), "corethread-" + i);
        threads[i].start();
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          logger.warn(e.getMessage(), e);
        }
      }

      while (this.runnable && existAliveThread(threads)) {
        try {
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
          logger.warn(e.getMessage(), e);
        }
        if (urlPool.getFuzzySize() < 50000) {
          List<String> seeds = URLUtils.loadJobURL(this.getJobName());
          for (String seed : seeds) {
            urlPool.offer(new SKURL(seed, false));
          }
        }
//        if (urlPool.size() < 10000 && startInd < seeds.size()) {
//          urlPool.addFirst(seeds.get(startInd));
//          startInd++;
////          int endInd = Math.min(startInd + INC, seeds.size());
////          urlPool.addAll(seeds.subList(startInd, endInd));
////          startInd = endInd;
//        }
      }

      if (!this.runnable) {
        break;
      }
      this.clientJob.flush(System.getProperty("user.dir") + "/statistic/"
          + properties.getJobName() + "/");
      int second = properties.getDelaySeconds();
      if (second == 0) {
        break;
      } else {
        try {
//          this.clientJob.flush(System.getProperty("user.dir") + "/statistic/"
//              + properties.getJobName() + "/");
          logger.info(properties.getJobName() + " is sleeping");
          jobInfo2Sql.setSleep();
          logger.info(this.getJobName() + "=sleeping，update job table set activity=1");
          Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
          logger.warn(e.getMessage(), e);
          if (!this.runnable) {
            logger.info("thread interrupted");
            break;
          }
        }
        if (!this.runnable) {
          logger.info("thread interrupted");
          break;
        }
      }
    }
    jobInfo2Sql.setStop();
//    timer.cancel();
    logger.info(this.getJobName() + "=stop，update job table set activity=2");
    logger.info(this.getJobName() + " is over");
//    logger.info("will dump url need fetch");
//    List<String> urls = new ArrayList<>(urlPool.size());
//    for(SKURL skurl:urlPool){
//      urls.add(skurl.getUrl());
//    }
//    try {
//      FileUtils.writeLines(new File(this.getJobName() + "_url.txt"), urlPool);
//    } catch (IOException e) {
//      logger.warn(e.getMessage(), e);
//    }
//    logger.info("dump over");
  }

  private boolean existAliveThread(Thread[] threads) {
    boolean flag = false;
    for (Thread t : threads) {
      if (t.isAlive()) {
        flag = true;
      } else {
//        logger.info(t.getName() + " is not active");
      }
    }
    return flag;
  }

  private void fetchHTTP(final SKURL skurl) {
//    CHttpGet skHttpGet = new CHttpGet(skurl.getUrl());
    HttpGet httpGet = new HttpGet(skurl.getUrl());
    String proxy = ProxyPool.getProxy();

    if (properties.isProxy() && !skurl.isseed()) {
      if (proxy != null) {
        String[] tmp = proxy.split(":");
        if (tmp.length == 2) {
          HttpHost httpHost = new HttpHost(tmp[0], Integer.parseInt(tmp[1]));
          // 会覆盖global，setRedirectsEnabled 需要设置
          RequestConfig config = RequestConfig.custom()
              .setProxy(httpHost)
              .setConnectTimeout(10000)
//              .setConnectionRequestTimeout(10000)
              .setSocketTimeout(10000)
              .setRedirectsEnabled(false)
              .setCircularRedirectsAllowed(false)
              .setCookieSpec(CookieSpecs.BEST_MATCH)
              .build();
          httpGet.setConfig(config);
//          userclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
        }
      }
    }
    if (skurl.isseed()) {
//      logger.info("is seed, will process"+skurl.getUrl());
    }
    CloseableHttpResponse response = null;
    try {
//      logger.info("【cshi】");
      long begin = System.currentTimeMillis();
      response = userclient.execute(httpGet);
      long during = System.currentTimeMillis() - begin;
//      logger.info("cost "+during+" ms");
    } catch (IOException e) {
      if (properties.isProxy() && proxy != null) {
        ProxyPool.remove(proxy);
      }
      if (response != null) try {
        response.close();
      } catch (IOException e1) {
//        e1.printStackTrace();
      }
      return;
    }
//    logger.info("before entity to string");
    String content = null;
    try {
      // TODO: will lock
      content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()), properties.getCharset());
//      content = InputStreamUtils.entity2String(response.getEntity(), properties.getCharset());
    } catch (Exception e) {
//      logger.warn(e.getMessage(),e);
    }
    int code = 888;
    try {
      code = ResponseUtils.getResponseStatus(response);
    } catch (Exception e) {
    }
    if (code != 200) {
//      logger.info("after entity to code " + code + " ---- " + skurl.getUrl());
    } else {
      skurl.setContent(content);
    }
    skurl.setStausCode(code);
//    logger.info("before consume");
    try {
      EntityUtils.consume(response.getEntity());
//      httpGet.releaseConnection();
//      response.close();
    } catch (IOException e) {
//      logger.warn(e.getMessage(), e);
    } finally {
      try {
        response.close();
      } catch (IOException e) {
//        logger.warn(e.getMessage(), e);
      }
    }
//    logger.info("after consume");
    if (skurl.isseed()) {
      logger.info("seed ok now");
    }
  }

  private class CoreThread implements Runnable {

    @Override
    public void run() {
      logger.info(SKJob.this.getJobName() + ": " + Thread.currentThread().getName() + " is start!");
      while (true) {
        try {
          SKURL skurl = null;
          int count = 30;
          // 等30秒
          skurl = urlPool.poll();
          while (count > 0 && skurl == null) {
//            if (count % 10 == 0)
//              logger.info("get url null with url poolsize: " + urlPool.getFuzzySize() + " with count down: " + count);
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
//              logger.warn(e.getMessage(), e);
            }
            skurl = urlPool.poll();
            count--;
          }
          try {
            if (skurl != null) {
//              logger.info("begin process");
//          logger.info("before using fetchHTTP "+skurl.getUrl());
              fetchHTTP(skurl);
//          logger.info("after using fetchHTTP "+skurl.getUrl());
              int code = skurl.getStausCode();
              if (code == 200 && skurl.getContent() != null && !"".equals(skurl.getContent().trim())) {
                if (urlPool.getFuzzySize() % 100 == 0)
                  logger.info("【【【【【【【【urlpool】】】】】】】】: " + SKJob.this.getJobName() + ": 【" + urlPool.getFuzzySize() + "】");
//            logger.info("before using extract "+skurl.getUrl());
                try {
                  extract(skurl);
                } catch (Exception e) {
//                  logger.warn(e.getMessage(), e);
                }
//            logger.info("after using extract "+skurl.getUrl());
//            logger.info("extract(url)-after-urlpool" + urlPool.size());
              } else if (code / 100 == 3 || code == 888 || code == 404) {

              } else if (skurl.shouldRetry()) {
                skurl.setContent(null);
                urlPool.offer(skurl);
              }
//              logger.info("end process");
            } else {
              break;
            }
          } catch (Exception e) {
          }
          if (!SKJob.this.runnable) {
            break;
          }
        } catch (Throwable e) {

        }
      }
//      logger.info(SKJob.this.getJobName() + ": " + Thread.currentThread().getName() + " is finish!");
      if (urlPool.isEmpty()) {
        logger.info(SKJob.this.getJobName() + ": " + Thread.currentThread().getName() + " finish normal");
      } else {
        logger.info(SKJob.this.getJobName() + ": " + Thread.currentThread().getName() + " not finish normal");
      }
    }
  }

  private class FetchThread implements Callable<HttpResponse> {
    private HttpGet httpGet;
    private DefaultHttpClient httpClient;

    private FetchThread(final DefaultHttpClient httpClient, final HttpGet httpGet) {
      this.httpClient = httpClient;
      this.httpGet = httpGet;
    }

    @Override
    public HttpResponse call() throws Exception {
      return httpClient.execute(this.httpGet);
    }
  }


  private void extract(SKURL skurl) {
    this.clientJob.getExtractor().process(skurl, properties.getJobName());
  }

  // add no-seed url
  void addUrl(String url) {
//    if (new Random().nextInt(1000) == 0)
//      logger.info("will add url: " + url);
    SKURL skurl = new SKURL(url, false);
    if (!urlPool.offer(skurl)) {
      logger.warn("add url " + url + " not ok");
    }
    if (urlPool.getFuzzySize() > 100000) {
      List<String> seeds = new ArrayList<>(50000);
      for (int i = 0; i < 50000; i++) {
        seeds.add(urlPool.poll().getUrl());
      }
      URLUtils.saveJobURL(seeds, this.getJobName());
    }
  }

  void kill() {
    logger.info("will kill " + this.getJobName());
    this.runnable = false;
    jobInfo2Sql.getTimer().cancel();
    Thread.currentThread().interrupt();
  }

  public boolean isActive() {
    return isactive || !urlPool.isEmpty();
  }

  private class SizeAdjustTask extends TimerTask {

    @Override
    public void run() {
      long start = System.currentTimeMillis();
      int size = SKJob.this.urlPool.getFuzzySize();
      SKJob.this.urlPool.adjustSize();
      int diff = SKJob.this.urlPool.getFuzzySize()-size;
      logger.info("adjust size using " + (System.currentTimeMillis() - start) + " mill-s with diff "+diff);
    }
  }
}