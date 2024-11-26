package Actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import play.http.websocket.Message;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import akka.actor.Cancellable;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
public class SearchActor extends AbstractActor {
    private final ActorRef out;
    private final SearchHistoryModel shModel;
    private final Set<String> receivedVideoIds = new HashSet<>();
    private ActorRef videoQueryActor;
    private ActorRef sentimentActor;
    private Cancellable heartbeat;

    public SearchActor(ActorRef out, SearchHistoryModel shModel) {
        this.out = out;
        this.shModel = shModel;
    }

    @Override
    public void preStart(){
        sentimentActor = getContext().actorOf(SubmissionSentimentActor.props());
        heartbeat = getContext().getSystem().scheduler().schedule(
                Duration.Zero(),
                Duration.create(30, TimeUnit.SECONDS),
                self(),
                new Ping(),
                getContext().getSystem().dispatcher(),
                self()
        );
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
                        handleWordStats(query);
                    }else if ("pong".equals(type)) {
                        //do nothing rightnow
                    }
                })
                .match(Ping.class, ping -> sendPing())
                // Handle Pong messages from client
                .match(Pong.class, pong -> handlePong())
                .match(SearchResult.class, this::handleVideoResults)
                .match(WordStatsActor.WordStatsResult.class, this::handleWordStatsResult)
                .match(SubmissionSentimentActor.SentimentResult.class,
                        result -> updateSearchSummary(result.getSentiment()))
                .build();
    }

    /**
     * @author Tomas Pereira
     * @param sentiment The sentiment result string for the set of videos that has been processed.
     *
     * After receiving a sentiment result message, the SearchActor sends it up to be displayed
     */
    private void updateSearchSummary(String sentiment){
        ObjectNode summaryUpdate = Json.newObject();
        summaryUpdate.put("type", "summary");
        summaryUpdate.put("sentiment", sentiment);
        out.tell(summaryUpdate, self());
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

        // After getting the list, send a message to process the sentiment
        sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(message.videos), self());
    }


    private void handleWordStats(String query) {
        // Create a WordStatsActor to process the word statistics
        ActorRef wordStatsActor = getContext().actorOf(WordStatsActor.props(query, self(), shModel));
    }

    private void handleWordStatsResult(WordStatsActor.WordStatsResult result) {
        // Send the word stats back to the client via WebSocket
        ObjectNode message = Json.newObject();
        message.put("type", "wordStats");
        message.put("query", result.query);
        message.set("wordStats", Json.toJson(result.wordStats));
        out.tell(message, self());
    }


    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                100,
                Duration.create("10 minute"),
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build()
        );
    }

    @Override
    public void postStop() {
        if (videoQueryActor != null) {
            getContext().stop(videoQueryActor);
        }
        if (heartbeat != null && !heartbeat.isCancelled()) {
            heartbeat.cancel();
        }

    }
    private void sendPing() {
        ObjectNode message = Json.newObject();
        message.put("type", "ping");
        out.tell(message, self());
    }

    private void handlePong() {
        // Optionally log or handle pong response
    }
    // Message classes
    public static class Ping {}
    public static class Pong {}
}
