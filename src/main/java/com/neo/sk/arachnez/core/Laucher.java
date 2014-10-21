package com.neo.sk.arachnez.core;

import com.neo.sk.arachnez.framework.Controller;
import com.neo.sk.arachnez.proxy.ProxyPool;
import com.neo.sk.arachnez.thrift.ThriftClient;
import com.neo.sk.arachnez.util.DbUtil;
import com.neo.sk.arachnez.util.URLUtils;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/8/25
 * Time: 14:43
 */
public class Laucher {
  private static final Logger logger = LoggerFactory.getLogger(Laucher.class);

  public static void main(String[] args) {
    try {
      DbUtil.init("c3p0.properties");
      Controller controller;
      ProxyPool.run();
      ThriftClient.init();
      URLUtils.clearAllURL();
      controller = Controller.getInstance();
    } catch (TTransportException e) {
      logger.warn(e.getMessage(), e);
    }
  }
}
