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
import java.util.LinkedList;
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

    private static final int MAX_QUERY_RESULTS = 10;
    private final LinkedList<SearchResult> queryBuffer = new LinkedList<>();

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
                .match(SearchResult.class, this::processSearchResult)
                .match(WordStatsActor.WordStatsResult.class, this::handleWordStatsResult)
                .match(SubmissionSentimentActor.SentimentResult.class, this::handleSentimentResult)
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

        // After getting the list, send a message to process the sentiment
        sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(message.videos), self());
    }

    /**
     * @author Tomas Pereira
     * @param searchResult The current result of the search, which should have blank sentiment.
     *
     * After receiving a SearchResult from the VideoQueryActor, the SearchActor will add it to the current buffer.
     * During this time, the SearchActor will also send a message to the SubmissionSentimentActor to process the
     * sentiment on the list of videos that it received. These will be joined back together during handleSentimentResult.
     */
    private void processSearchResult(SearchResult searchResult){
        // With the list of videos, can now get the sentiment
        sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(searchResult.videos), self());

        // Add it to the buffer -> The sentiment will still be empty at this point
        queryBuffer.addLast(searchResult);

        if(queryBuffer.size() > MAX_QUERY_RESULTS)
            queryBuffer.removeFirst();
    }

    /**
     * @author Tomas Pereira
     * @param result The sentiment result returned by the SubmissionSentimentActor
     *
     * Method for joining the sentiment of the current query back with its search result.
     * It will update the Sentiment of the latest query, as this is the one which the request was sent for.
     * Once updated, the result will be sent to the client through the sendToClient method.
     */
    private void handleSentimentResult(SubmissionSentimentActor.SentimentResult result){
        // Check the result we just added
        SearchResult updatedResult = queryBuffer.peekLast();

        if(updatedResult != null){
            updatedResult.sentiment = result.getSentiment();
            // Can now send to client
            sendToClient(updatedResult);
        }
    }

    /**
     * @author Tomas Pereira
     * @param result The complete, ready to send, SearchResult
     * Given a SearchResult, sends it to the client for display.
     */
    private void sendToClient(SearchResult result){
        ObjectNode message = Json.newObject();
        message.put("type", "queryResult");
        message.put("query", result.query);
        message.put("sentiment", result.sentiment);
        message.set("videos", Json.toJson(result.videos));
        out.tell(message, self());
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
