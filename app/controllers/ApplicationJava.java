package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.WebSocket;
import scala.Option;


public class ApplicationJava extends Controller {

    public WebSocket<String> ws() {
        return new WebSocket<String>() {
            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {
                //create an actor which is responsible for hangling messages coming from stockActor
                final ActorRef watcher = Akka.system().actorOf(WatcherActor.props(out));

                // send all WebSocket message to the DriverManagerActor
                in.onMessage(new F.Callback<String>() {
                    @Override
                    public void invoke(String string) throws Throwable {
                        RegisterWatcher registerWatcher = new RegisterWatcher(watcher);
                        DriverManagerActor.driverManagerActor().tell(registerWatcher,watcher);
                    }
                });

                // on close, tell the DriverManagerActor to remove this watcher on all driver actors
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        final Option<String> none = Option.empty();
                        DriverManagerActor.driverManagerActor().tell(new RemoveWatcher(watcher),watcher);
                        Akka.system().stop(watcher);
                    }
                });
            }
        };
    }

    public WebSocket<String> wsDriver(String driverId) {
        return new WebSocket<String>() {
            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {
                //create an actor which is responsible for hangling messages coming from stockActor
                final ActorRef watcher = Akka.system().actorOf(WatcherActor.props(out));
                RegisterWatcherForDriver registerWatcherForDriver = new RegisterWatcherForDriver(watcher ,driverId);
                DriverManagerActor.driverManagerActor().tell(registerWatcherForDriver,watcher);

//                // send all WebSocket message to the DriverManagerActor
//                in.onMessage(new F.Callback<String>() {
//                    @Override
//                    public void invoke(String string) throws Throwable {
//                        RegisterWatcher registerWatcher = new RegisterWatcher(watcher);
//                        DriverManagerActor.driverManagerActor().tell(registerWatcher,watcher);
//                    }
//                });
//
                // on close, tell the DriverManagerActor to remove this watcher on all driver actors
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        final Option<String> none = Option.empty();
                        DriverManagerActor.driverManagerActor().tell(new RemoveWatcher(watcher),watcher);
                        Akka.system().stop(watcher);
                    }
                });
            }
        };
    }
}
