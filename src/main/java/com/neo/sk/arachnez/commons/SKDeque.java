package com.neo.sk.arachnez.commons;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/26
 * Time: 11:14
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
public class SKDeque<E> {
  private Deque<E> deque;
  private AtomicInteger fuzzySize;
  public SKDeque(){
    deque = new ConcurrentLinkedDeque();
    fuzzySize = new AtomicInteger(0);
  }

  public int getFuzzySize(){
    return this.fuzzySize.get();
  }

  public int getSize(){
    int size = this.deque.size();
    this.fuzzySize.set(size);
    return size;
  }

  public void clear(){
    this.deque.clear();
    this.fuzzySize.set(0);
  }

  public boolean addAll(Collection<? extends E> c){
    this.fuzzySize.addAndGet(c.size());
    return this.deque.addAll(c);
  }

  public E poll(){
    E e = this.deque.poll();
    if(e!=null)
      this.fuzzySize.decrementAndGet();
    return e;
  }

  public boolean isEmpty(){
    return this.deque.isEmpty();
  }

  public boolean offer(E obj){
    this.fuzzySize.incrementAndGet();
    return this.deque.offer(obj);
  }

  public void adjustSize(){
    this.fuzzySize.set(this.deque.size());
  }


}