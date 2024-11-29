package Actors;

import akka.actor.*;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Channel;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;

import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

public class SearchActorTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("SearchActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    // Mock SearchHistoryModel
    static class MockSearchHistoryModel extends SearchHistoryModel {
        public MockSearchHistoryModel() {
            super(null); // Pass null or a mock YouTube client if necessary
        }

        // Override methods if needed
    }

    // ForwardingActor to forward messages to TestProbes
    public static class ForwardingActor extends AbstractActor {
        private final ActorRef target;

        public ForwardingActor(ActorRef target) {
            this.target = target;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(msg -> target.forward(msg, getContext()))
                    .build();
        }
    }

    @Test
    public void testHandleSearchQuery() {
        new TestKit(system) {{
            final TestKit outProbe = new TestKit(system);
            SearchHistoryModel shModel = new MockSearchHistoryModel();

            final TestKit videoQueryActorProbe = new TestKit(system);
            final TestKit sentimentActorProbe = new TestKit(system);

            Props videoQueryActorProps = Props.create(ForwardingActor.class, () -> new ForwardingActor(videoQueryActorProbe.getRef()));
            Props sentimentActorProps = Props.create(ForwardingActor.class, () -> new ForwardingActor(sentimentActorProbe.getRef()));

            final ActorRef searchActor = system.actorOf(
                    Props.create(SearchActor.class, () -> new SearchActor(
                            outProbe.getRef(),
                            shModel
                    ))
            );

            ObjectNode searchMessage = Json.newObject();
            searchMessage.put("type", "search");
            searchMessage.put("query", "test query");

            searchActor.tell(searchMessage, getRef());


            // Simulate SearchResult from VideoQueryActor
            List<Video> videos = Arrays.asList(
                    new Video("id1", "Title 1", "channelId1", "Channel 1", "Description 1", "thumbnail1"),
                    new Video("id2", "Title 2", "channelId2", "Channel 2", "Description 2", "thumbnail2")
            );
            SearchResult searchResult = new SearchResult("test query", videos,"");

            searchActor.tell(searchResult, videoQueryActorProbe.getRef());

            // Simulate SentimentResult from sentimentActor
            SubmissionSentimentActor.SentimentResult sentimentResult = new SubmissionSentimentActor.SentimentResult(":-|",videos);
            searchActor.tell(sentimentResult, sentimentActorProbe.getRef());

            // Verify that client receives queryResult
            ObjectNode clientMessage = outProbe.expectMsgClass(Duration.ofSeconds(1), ObjectNode.class);

            if(clientMessage.get("type").asText()!="ping"){
            assertEquals("queryResult", clientMessage.get("type").asText());
            assertEquals("test query", clientMessage.get("query").asText());
            assertEquals(":-|", clientMessage.get("sentiment").asText());}
        }};
    }



    @Test
    public void testHandleWordStats() {
        new TestKit(system) {{
            final TestKit outProbe = new TestKit(system);
            SearchHistoryModel shModel = new MockSearchHistoryModel();

            final TestKit wordStatsActorProbe = new TestKit(system);

            Props wordStatsActorProps = Props.create(ForwardingActor.class, () -> new ForwardingActor(wordStatsActorProbe.getRef()));

            final ActorRef searchActor = system.actorOf(
                    Props.create(SearchActor.class, () -> new SearchActor(
                            outProbe.getRef(),
                            shModel
                    ))
            );

            ObjectNode wordStatsMessage = Json.newObject();
            wordStatsMessage.put("type", "wordStats");
            wordStatsMessage.put("query", "test query");

            searchActor.tell(wordStatsMessage, getRef());

            // Simulate WordStatsResult from WordStatsActor
            Map<String, Long> wordStats = new HashMap<>();
            wordStats.put("test", 10L);
            wordStats.put("query", 5L);

            WordStatsActor.WordStatsResult wordStatsResult = new WordStatsActor.WordStatsResult("test query", wordStats);

            searchActor.tell(wordStatsResult, wordStatsActorProbe.getRef());

            // Verify that client receives wordStats
            ObjectNode clientMessage = outProbe.expectMsgClass(Duration.ofSeconds(1), ObjectNode.class);

            assertEquals("wordStats", clientMessage.get("type").asText());
            assertEquals("test query", clientMessage.get("query").asText());
            // Additional assertions can be added
        }};
    }

    @Test
    public void testPingPong() {
        new TestKit(system) {{
            final TestKit outProbe = new TestKit(system);
            SearchHistoryModel shModel = new MockSearchHistoryModel();

            final ActorRef searchActor = system.actorOf(
                    Props.create(SearchActor.class, () -> new SearchActor(
                            outProbe.getRef(),
                            shModel
                    ))
            );

            // Simulate Ping message
            searchActor.tell(new SearchActor.Ping(), ActorRef.noSender());

            // Verify that client receives ping
            ObjectNode pingMessage = outProbe.expectMsgClass(Duration.ofSeconds(1), ObjectNode.class);
            assertEquals("ping", pingMessage.get("type").asText());

            // Simulate Pong message from client
            ObjectNode pongMessage = Json.newObject();
            pongMessage.put("type", "pong");

            searchActor.tell(pongMessage, getRef());

            // No further action needed as handlePong() does nothing
        }};
    }
}
