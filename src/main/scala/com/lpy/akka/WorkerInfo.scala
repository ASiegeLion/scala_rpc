package com.lpy.akka

class WorkerInfo (val id : String , val memory :Int ,val cores : Int){
  var lastHeartbeatTime : Long = _
}
