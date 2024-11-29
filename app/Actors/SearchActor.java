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
                Duration.create(5, TimeUnit.SECONDS),
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
                        handleChannelProfile(channelId);
                    } else if ("wordStats".equals(type)) {
                        String query = message.get("query").asText();
                        handleWordStats(query);
                    }
                })
                .match(Ping.class, ping -> sendPing())

                .match(SearchResult.class, this::processSearchResult)
                .match(ChannelProfileActor.ChannelProfileResult.class, this::handleChannelProfileResult)
                .match(WordStatsActor.WordStatsResult.class, this::handleWordStatsResult)
                .match(SubmissionSentimentActor.SentimentResult.class, this::handleSentimentResult)
                .build();
    }


    private void handleSearchQuery(String query) {

        receivedVideoIds.clear();
        videoQueryActor = getContext().actorOf(Props.create(VideoQueryActor.class, query, self(), shModel));
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


        updatedResult.sentiment = result.getSentiment();
            // Can now send to client
        sendToClient(updatedResult);

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

    /**
     * @author Sam Collin
     * Handles the creation of a ChannelProfileActor to process channel information.
     * @param channelId the ID of the channel to process.
     */
    private void handleChannelProfile(String channelId) {
        // Creates a ChannelProfileActor to process channel information
        ActorRef channelProfileActor = getContext().actorOf(ChannelProfileActor.props(channelId, self(), shModel));
    }

    /**
     * @author Sam Collin
     * Handles the result from ChannelProfileActor and sends the channel details and videos to the client.
     * @param result the result containing channel details and videos.
     */
    private void handleChannelProfileResult(ChannelProfileActor.ChannelProfileResult result) {
        // Prepare the JSON message for the client
        ObjectNode message = Json.newObject();
        message.put("type", "channelProfile");
        message.set("channel", Json.toJson(result.channel));
        message.set("videos", Json.toJson(result.videos));

        // Sends data to the client via WebSocket
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
        heartbeat.cancel();


    }
    private void sendPing() {
        ObjectNode message = Json.newObject();
        message.put("type", "ping");
        out.tell(message, self());
    }


    // Message classes
    public static class Ping {}
}
