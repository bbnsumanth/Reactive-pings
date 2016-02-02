package actors

import akka.actor.{Props, Actor, ActorRef}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import play.api.Logger

import play.libs.Json
import play.mvc.WebSocket

class WatcherActor(out: WebSocket.Out[String] ) extends Actor{
  //todo:send a message to manager actor asking him to notify all drivers
  println(s"WatcherActor created for ${out.toString}")

  override def receive: Receive = {

    case UpdatePing(ping) => {
      //println(s"WatcherActor:Received UpdatePing(${ping.toString}) message")
      val locationUpdateMessage: ObjectNode = Json.newObject
      locationUpdateMessage.put("pingId",ping.id)
      locationUpdateMessage.put("driverId",ping.driver_id)
      locationUpdateMessage.put("lat",ping.location._2)
      locationUpdateMessage.put("lng",ping.location._1)
      out.write(locationUpdateMessage.toString)
    }

    case LocationHistory(locations) => {
      val locationArray: ArrayNode = Json.newArray()
      locations.foreach(l => {
        val ping: ObjectNode = Json.newObject
        ping.put("lat",l._2)
        ping.put("lng",l._1)
        locationArray.addPOJO(ping)
      })
      out.write(locationArray.toString)
    }
  }
}

object WatcherActor{
  def props(out:WebSocket.Out[String]): Props = Props(new WatcherActor(out))
}

case class RegisterWatcherForDriver(watcher:ActorRef,driverId:String)
case class RemoveWatcherForDriver(watcher:ActorRef,driverId:String)

