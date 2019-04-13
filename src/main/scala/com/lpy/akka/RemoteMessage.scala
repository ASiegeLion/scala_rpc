package com.lpy.akka

trait RemoteMessage extends  Serializable

case class RegisterWorker(id : String ,memory : Int,cores :  Int)extends RemoteMessage

case class Heartbeat(id : String) extends RemoteMessage

case class RegisteredWorker(masterUrl : String) extends RemoteMessage

case object SendHeartbeat

case object CheckTimeOutWorker