package actors

import java.util.{Calendar}
import akka.actor.{Props, ActorRef, Actor}
import play.libs.Akka
import utils.{Ping, LocationHelper}
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class StreamListenerActor extends Actor{
  println(s"Stream Listner Actor created")
  var id:Long = 0
  val longitude  = 77.4908518
  val latitude = 12.9542946
  val radius = 15
  //todo:proper acctor model design ,there are bottle neckswhen we produced 500000 events we pushed 1.5L events
  def createActorAndAddWatchers(driverId: Int) = {
    val driverActor = context.actorOf(Props(new DriverActor(driverId.toString)),driverId.toString)
    DriverManagerActor.driverManagerActor ! RegisterAllWatcherOnDriver(driverId.toString)
    driverActor
  }

  override def receive: Receive = {
    case StartStreaming => {
      println(s"started streaming pings")
      //todo:listen to the stream and update drivers and trip actors
      //using actor schedular to generate a mess to this actor
      context.system.scheduler.schedule(Duration.Zero, 15000.millis, self,GeneratePing)
    }
    case GeneratePing => {
      //println(s"generating ping")
      (1 to 30000).foreach(driverId =>{
        val ping = Ping(id,driverId,LocationHelper.getLocation(longitude,latitude,radius),Calendar.getInstance().getTime().getTime)
        id=id+1
        val driverActor: ActorRef = context.child(driverId.toString).getOrElse {
         createActorAndAddWatchers(driverId)
        }
        driverActor ! UpdatePing(ping)
        //DriverManagerActor.driverManagerActor ! UpdateDriver(ping)

      })
      //println(s"generated ${id} messages")
    }
    case StopStreaming => {
      context.stop(self)
    }
    case registerWatcher @ RegisterWatcher(watcher) => {
      context.children.foreach((_.forward(registerWatcher)))
    }
    case RegisterWatcherForDriver(watcher,driver_id) => {
      context.child(driver_id).foreach(_.forward(RegisterWatcherForDriver(watcher,driver_id)))
    }
    case RemoveWatcherForDriver(watcher,driver_id) => {
      context.child(driver_id).foreach(_.forward(RegisterWatcherForDriver(watcher,driver_id)))
    }
  }
}

object StreamListenerActor{
  lazy val streamListenerActor: ActorRef = Akka.system.actorOf(Props(classOf[StreamListenerActor]))
}

case object Listen
case object GeneratePing