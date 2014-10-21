package com.neo.sk.arachnez.akka

import com.neo.sk.arachnez.commons.SKURL

import scala.collection.mutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/11
 * Time: 18:36
 *
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//
//   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗
// ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝
// ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗
// ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║
// ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝
//  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝
//  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░
//  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░
//           ░     ░ ░      ░  ░
// 
 *
 */
object Test extends App{
  private val urls = new Queue[String]

  urls.enqueue("1")
  urls.enqueue("2")
  urls.enqueue("3")
  println(urls.dequeue())
  println(urls.dequeue())
  println(urls.dequeue())
//  val t = Option(urls.dequeue())
//  if(t.isDefined){
//    println(t)
//  }else{
//    println("")
//  }

  val t = Option[String](null)
  if(t.isDefined){
    println("nnnn")
  }else{
    println("none")
  }

  val s = Some()

  val arr = new Array[String](5)
  arr(0)="1"
  arr(1)="2"
  arr(2)="3"
  println(arr(0))
  println(arr(1))
  println(arr(2))
  println(arr(3))


  case class A(a:Int)

  class B(a:Int){}

  val aaa = A(1)::A(2)::Nil

  val bbb = new B(1)::new B(2)::Nil

  aaa.map{
    case A(a) =>
      println(a)
  }

  bbb.map{
    case c:B =>
      println()
  }

}
