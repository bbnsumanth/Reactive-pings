package actors

import akka.actor.{Actor,ActorRef}
import utils.Ping
import scala.collection.immutable.HashSet

class DriverActor(driverId:String) extends Actor{

  println(s"Driver Actor created for ${driverId}")
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  protected[this] var locationHistory: List[(Double, Double)] = List.empty[(Double,Double)]
  protected[this] var driverWatchers: HashSet[ActorRef] = HashSet.empty[ActorRef]


  override def receive: Receive = {
    case UpdatePing(ping) => {
      //println(s"Received a ping update for with ${ping}  ")
      locationHistory = locationHistory.+:(ping.location)
      driverWatchers.foreach(_ ! UpdatePing(ping))
      watchers.foreach(_ ! UpdatePing(ping))
    }
    case RegisterWatcher(watcher) => {
      //println(s"register watcher mess received from ${watcher.toString()} in ${self.toString()}")
      watchers = watchers + watcher
    }
    case KillDriver(_) =>{
     //todo:kill this actor upon this message
    }
    case RemoveWatcher(_)=>{
      //remove the watcher from watchers list
      //if watchers list is empty,kil the driver actor
      watchers  = watchers - sender
      if (watchers.size == 0 && driverWatchers.size == 0) {
        context.stop(self)
      }
    }
    case RegisterWatcherForDriver(watcher,driver_id) => {
      driverWatchers = driverWatchers + watcher
      watcher ! LocationHistory(locationHistory)
    }
    case RemoveWatcherForDriver(watcher,driver_id) => {
      driverWatchers = driverWatchers - watcher
      if (watchers.size == 0 && driverWatchers.size == 0) {
        context.stop(self)
      }
    }
  }
}

//to remove a particular watcher on all driver actors
case class RemoveWatcher(watcher:ActorRef)
case class RegisterWatcher(watcher:ActorRef)
//to kill a particular driverActor if given a driverId,kill all if None is given
case class KillDriver(driverId:Option[String])
case class UpdatePing(ping:Ping)
case class LocationHistory(locationHistory:List[(Double,Double)])
