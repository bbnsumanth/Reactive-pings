package actors

import akka.actor._
import play.libs.Akka
import utils.Ping
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.HashSet
import scala.concurrent.ExecutionContextExecutor

/**
  * DriverManagerActor to create driverActors and kill them and also to manage watchers on driver.
  */
class DriverManagerActor extends Actor{
  println(s"DriverManager Actor created")

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  protected[this] var driverWatchers: HashSet[(String,ActorRef)] = HashSet.empty[(String,ActorRef)]

  override def receive: Receive = {
    case killDriver @ KillDriver(Some(driverId)) =>
      // if there is a StockActor for the symbol forward this message
      context.child(driverId).foreach(_.forward(killDriver))
    case killDriver @ KillDriver(None) =>
      // if no symbol is specified, forward to everyone
      context.children.foreach(_.forward(killDriver))
    case removeWatcher @ RemoveWatcher(watcher) =>
      watchers = watchers - watcher
      context.children.foreach((_.forward(removeWatcher)))
    case registerWatcher @ RegisterWatcher(watcher) => {
      println(s"register watcher mess received in DriverManager from ${watcher.toString()}")
      watchers = watchers + watcher
      //context.children.foreach((_.forward(registerWatcher)))
      //todo:since driver actors are created in StreamListener,pass this message to them
      StreamListenerActor.streamListenerActor ! registerWatcher
    }
    case RegisterAllWatcherOnDriver(driver_id) => {
      watchers.foreach(watcher => {
        context.child(driver_id).foreach(_.forward(RegisterWatcher(watcher)))
      })
    }
    case RegisterWatcherForDriver(watcher,driver_id) => {
      driverWatchers = driverWatchers.+((driver_id,watcher))
      StreamListenerActor.streamListenerActor ! RegisterWatcherForDriver(watcher,driver_id)
      //context.child(driver_id).foreach(_.forward(RegisterWatcherForDriver(watcher,driver_id)))
    }
    case removeWatcherForDriver @ RemoveWatcherForDriver(watcher,driver_id) =>
      watchers = watchers - watcher
      //todo:pass a message to StreamListener and then to driver to remove this wattcher
      StreamListenerActor.streamListenerActor ! RemoveWatcherForDriver(watcher,driver_id)
  }
}

object DriverManagerActor {
  lazy val driverManagerActor: ActorRef = Akka.system.actorOf(Props(classOf[DriverManagerActor]))
}
case class RegisterAllWatcherOnDriver(driver_id:String)
case class UpdateDriver(ping:Ping)