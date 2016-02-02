import actors._
import akka.io.Tcp.Register
import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    val streamListener = StreamListenerActor.streamListenerActor
    streamListener ! StartStreaming
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    val streamListener = StreamListenerActor.streamListenerActor
    streamListener ! StopStreaming
  }

}