package actors

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive
import play.libs.Akka

class StreamManagerActor extends Actor{
  //create all these instances upon the creation of StreamManager
  val driverManager = DriverManagerActor.driverManagerActor
  //val tripManager = TripManagerActor.tripManagerActor
  val streamListenerActor = StreamListenerActor.streamListenerActor

  override def receive: Receive = {
    case StartStreaming => {
      //todo:keep listening to a stream and upon event ,update driver and trips
      // but the prob here is we can not block on this,becoz then we can not send a mess asking it to stop processing
      //so create another  actor(streamListenerActor) whose sole purpose is to listen on events and uupdates drivers and trips
      streamListenerActor ! Listen
    }
    case StopStreaming => {
      context.stop(streamListenerActor)
    }
  }
}
object StreamManagerActor{
  lazy val streamManagerActor: ActorRef = Akka.system.actorOf(Props(classOf[StreamManagerActor]))
}

case object StartStreaming
case object StopStreaming
