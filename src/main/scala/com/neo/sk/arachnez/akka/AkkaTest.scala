package com.neo.sk.arachnez.akka

import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/8
 * Time: 11:27
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



object AkkaTest {
  case object Tick

  val system = ActorSystem("clp")

  class MainActor extends Actor with ActorLogging{
//    context.setReceiveTimeout(2 seconds)
    implicit val timeout2 = Timeout(5 second)
    override def receive: Receive = {
      case a:Int =>
        log.info("main actor receive "+a+", will deliver")
        val child = context.actorOf(Props[ChildActor](new ChildActor(a)), a.toString)
        context watch child
        child ! a
      case b:String =>
        log.info("receive from "+sender.path.name +" with path "+sender.path)
//      case ReceiveTimeout =>
//        log.info("timeout " + sender.path.name) // deadLetter
      case c:Double =>
        val child = context.actorOf(Props[ChildActor](new ChildActor(100)), 100.toString)
        context watch child
        log.info("receive double")
//        val f = ask(child, 100)
        val f = child ? 100
        f.onComplete( _ => log.info("i'm complete"))
      case Tick =>
        val child = context.actorOf(Props[ChildActor](new ChildActor(101)), 101.toString)
        context watch child
        child ! "haha"
        log.info("tick")
      case Terminated(actor) =>
        log.info(actor.path.name+" is over")
    }

  }

  class ChildActor(index:Int) extends Actor with ActorLogging{
//    context.setReceiveTimeout(2 seconds)
    override def receive: Receive = {
      case a:Int =>
        log.info("child actor "+index+" receive "+a)
//        Thread.sleep(2500)
        sender ! a.toString
        throw new NullPointerException("nullp")
        context.stop(self)
//      case ReceiveTimeout =>
//        log.info("timeout " + sender.path.name)
    }

    override def unhandled(a:Any): Unit ={
      log.info("unhandled "+a.toString)
    }

    @scala.throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      log.info("preStart "+index)
    }

    @scala.throws[Exception](classOf[Exception])
    override def postStop(): Unit = {
      log.info("postStop "+index)
    }

    @scala.throws[Exception](classOf[Exception])
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info("preStart "+index +" with reason "+reason +" message "+message.getOrElse("none"))
    }

    @scala.throws[Exception](classOf[Exception])
    override def postRestart(reason: Throwable): Unit = {
      log.info("preStart "+index +" with reason "+reason)
    }
  }

  def main (args: Array[String]) {
    val mainactor = system.actorOf(Props[MainActor](new MainActor), "main-actor")
    (1 to 3).map( a=>
      mainactor ! a
    )
    mainactor ! 3.4
    mainactor ! Tick
    Thread.sleep(5000)
    system.shutdown()
  }

}
