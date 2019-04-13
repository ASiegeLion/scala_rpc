package com.lpy.akka


import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._



class Master(val host:String ,val port:Int) extends  Actor {

  val idToWorker = new mutable.HashMap[String,WorkerInfo]()

  val workers = new mutable.HashSet[WorkerInfo]()

  val CHECK_INTERVAL = 15000


  override def preStart(): Unit = {
    println("preStart invoked")
    import  context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)//自己给自己发送心跳检测
  }

  override def receive: Receive = {
    case RegisterWorker(id,memory,cores) =>{
      if(!idToWorker.contains(id)){
        val workerInfo = new WorkerInfo(id,memory,cores)
        idToWorker(id) = workerInfo
        workers += workerInfo
        sender ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")
      }
    }
    case Heartbeat(id) =>{
      if(idToWorker.contains(id)){
        val workerInfo = idToWorker(id)

        val currentTime = System.currentTimeMillis()
        workerInfo.lastHeartbeatTime = currentTime
      }
    }
    case CheckTimeOutWorker =>{
      val currentTime = System.currentTimeMillis()
      val toRemove = workers.filter(x => currentTime - x.lastHeartbeatTime > CHECK_INTERVAL)
      for (w <- toRemove){
        workers -= w
        idToWorker -= w.id
      }
      println(workers.size)
    }
  }
}

object Master{
  def main(args: Array[String]): Unit = {
    val host:String = "127.0.0.1"
    val port:String= "8888"
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
      val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
      val actorSystem = ActorSystem("MasterSystem",config)
    val master = actorSystem.actorOf(Props[Master],"Master")
    master ! "hello"
    actorSystem.awaitTermination();


  }
}
