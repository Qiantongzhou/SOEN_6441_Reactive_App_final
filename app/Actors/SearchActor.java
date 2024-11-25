package Actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActor extends AbstractActor {
    private final ActorRef out;
    private final SearchHistoryModel shModel;
    private final Set<String> receivedVideoIds = new HashSet<>();
    private ActorRef videoQueryActor;

    public SearchActor(ActorRef out, SearchHistoryModel shModel) {
        this.out = out;
        this.shModel = shModel;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, message -> {
                    String type = message.get("type").asText();
                    if ("search".equals(type)) {
                        String query = message.get("query").asText();
                        handleSearchQuery(query);
                    } else if ("channelProfile".equals(type)) {
                        String channelId = message.get("channelId").asText();
                        //handleChannelProfile(channelId);
                    } else if ("wordStats".equals(type)) {
                        String query = message.get("query").asText();
                        //handleWordStats(query);
                    }
                })
                .match(SearchResult.class, this::handleVideoResults)
//
                .build();
    }

    private void handleSearchQuery(String query) {
        if (videoQueryActor != null) {
            getContext().stop(videoQueryActor);
        }
        receivedVideoIds.clear();
        videoQueryActor = getContext().actorOf(Props.create(VideoQueryActor.class, query, self(), shModel));
    }


    private void handleVideoResults(SearchResult message) {
        for (Video video : message.videos) {
            if (!receivedVideoIds.contains(video.getVideoId())) {
                receivedVideoIds.add(video.getVideoId());
                ObjectNode response = Json.newObject();
                response.put("type", "video");
                response.set("data", Json.toJson(video));
                out.tell(response, self());
            }
        }
    }



    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create("1 minute"),
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build()
        );
    }

    @Override
    public void postStop() {
        if (videoQueryActor != null) {
            getContext().stop(videoQueryActor);
        }
    }
}
