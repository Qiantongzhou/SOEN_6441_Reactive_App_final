package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.Channel;
import models.SearchHistoryModel;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import akka.stream.javadsl.Flow;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import play.libs.streams.ActorFlow;
import com.google.inject.Inject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SearchController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final SearchHistoryModel shModel;

    @Inject
    public SearchController(ActorSystem actorSystem, Materializer materializer, SearchHistoryModel shModel) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.shModel = shModel;
    }
    /**
     * Renders the index page with an empty search history.
     *
     * @return a Result rendering the index page with an empty list
     */
    public Result index() {
        return ok(views.html.index.render(new LinkedList<>()));
    }
    public WebSocket searchSocket() {
        return WebSocket.Json.accept(request -> {
            return ActorFlow.actorRef(out -> Props.create(Actors.SearchActor.class, out, shModel), actorSystem, materializer);
        });
    }

}
