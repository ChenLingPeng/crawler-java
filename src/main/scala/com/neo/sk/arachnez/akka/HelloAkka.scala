package com.neo.sk.arachnez.akka

import akka.actor.{Actor, Props, ActorSystem}
import akka.actor.ActorDSL._

import scala.util.Random

/**
 * User: Taoz
 * Date: 7/14/2014
 * Time: 12:18 AM
 */
object HelloAkka {

  implicit val system = ActorSystem("HelloSystem")


  class EchoServer0 extends Actor{
    override def receive: Receive = {
      case a: String => println("I got: " + a)
      case _ => println("Huh?")
    }
  }

  class EchoServer1(name: String) extends Actor{
    override def receive: Receive = {
      case msg => println("server:" + name + " echo:" + msg + " by " + Thread.currentThread().getName)
    }
  }

  def test3() {
    val servers = (1 to 4).map{ x =>
      system.actorOf(Props(new EchoServer1(x.toString)))
    }

    (1 to 300).foreach{ i =>
      servers(Random.nextInt(4)) ! "count" + i
    }
  }

  def test2() {
    val helloActor = actor (new Act {
      become {
        case a: String => println("I got: " + a)
        case _ => println("Huh?")
      }
    })

    helloActor ! "hi"
    helloActor ! "GOgo"
    helloActor ! 123

  }

  def test1() {

    val helloActor = system.actorOf(Props[EchoServer0], "a1")
    helloActor ! "hi"
    helloActor ! "GOgo"
    helloActor ! 123

  }

  def main(args: Array[String]) {

    test3()
    system.shutdown()
  }







}
