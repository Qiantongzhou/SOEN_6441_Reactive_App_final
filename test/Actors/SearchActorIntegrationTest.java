package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Channel;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class SearchActorIntegrationTest {

    private static ActorSystem actorSystem;

    /**
     * @author Tomas Pereira
     * Before the tests, creates the necessary actor system.
     */
    @Before
    public void init(){
        actorSystem = ActorSystem.create();
    }


    /**
     * @author Tomas Pereira
     *
     * Test for the entire chain of query processing from a String search.
     * Passes through the VideoQueryActor, SubmissionSentimentActor, and Search Actor to deliver the result.
     */
    @Test
    public void testFullQueryResultProcessing(){
        new TestKit(actorSystem){{
            final SearchHistoryModel mockSearchHistoryModel = mock(SearchHistoryModel.class);
            final ActorRef out = getTestActor();
            final ActorRef searchActor = actorSystem.actorOf(Props.create(SearchActor.class, out, mockSearchHistoryModel));

            Video happy = new Video("id1", "Happy", "ch1", "Channel 1", "Smile Smile Smile Joy Joy Joy", "thumb1");
            List<Video> videos = Collections.singletonList(happy);

            // Mock response of Youtube API
            when(mockSearchHistoryModel.queryYoutube("test")).thenReturn(videos);

            // Test Actor Response to Query
            ObjectNode message = Json.newObject();
            message.put("type", "search");
            message.put("query", "test");
            searchActor.tell(message, getRef());

            // Mock Response from VideoActor
            searchActor.tell(new SearchResult("test", videos, ""), getRef());

            // Mock Response from Sentiment
            searchActor.tell(new SubmissionSentimentActor.SentimentResult(":-)", videos), getRef());

            // Checking response
            ObjectNode expected = Json.newObject();
            expected.put("type", "queryResult");
            expected.put("query", "test");
            expected.put("sentiment", ":-)");
            expected.set("videos", Json.toJson(videos));

            // Need to ignore ping messages
            ObjectNode received;
            do {
                received = expectMsgClass(ObjectNode.class);
            } while(received.has("type") && received.get("type").asText().equals("ping"));

            ObjectNode expected1 = Json.newObject();
            expected1.put("type", "ping");
            received = expectMsgClass(ObjectNode.class);
            if(received.has("type") && received.get("type").asText().equals("ping")){
            assertEquals(received, expected1);
            }else {
                assertEquals(received, expected);
            }
        }};
    }

    /**
     * @author Sam Collin
     * Tests the processing of a channel profile request in the SearchActor by verifying the received response matches the expected channel and video data.
     */
    @Test
    public void testChannelProfileProcessing() {
        new TestKit(actorSystem) {{
            // Mock dependencies
            final SearchHistoryModel mockSearchHistoryModel = mock(SearchHistoryModel.class);
            final ActorRef out = getTestActor();
            final ActorRef searchActor = actorSystem.actorOf(Props.create(SearchActor.class, out, mockSearchHistoryModel));

            // Mock channel and video data
            Channel mockChannel = new Channel(
                    "channelId1", "Channel Title", "Description", "2022-01-01T00:00:00Z",
                    "US", "http://example.com/customUrl", "http://example.com/thumbnail.jpg",
                    1000, false, 5000, 100
            );
            List<Video> mockVideos = Arrays.asList(
                    new Video("videoId1", "Video Title 1", "channelId1", "Channel Title", "Description 1", "http://example.com/video1.jpg"),
                    new Video("videoId2", "Video Title 2", "channelId1", "Channel Title", "Description 2", "http://example.com/video2.jpg")
            );

            // Create a mock message to request a channel profile
            ObjectNode message = Json.newObject();
            message.put("type", "channelProfile");
            message.put("channelId", "channelId1");

            // Send the request to the SearchActor
            searchActor.tell(message, getRef());

            // Mock response from the ChannelProfileActor
            searchActor.tell(new ChannelProfileActor.ChannelProfileResult(mockChannel, mockVideos), getRef());

            // Ignore `ping` messages and wait for the correct response
            ObjectNode received;
            do {
                received = expectMsgClass(ObjectNode.class);
            } while (received.has("type") && received.get("type").asText().equals("ping"));

            // Expected response to the client
            ObjectNode expected = Json.newObject();
            expected.put("type", "channelProfile");
            expected.set("channel", Json.toJson(mockChannel));
            expected.set("videos", Json.toJson(mockVideos));

            // Assert the expected response
            assertEquals(expected, received);
        }};
    }






    /**
     * @author Tomas Pereira
     * Tears down the generated actor system and nulls the reference.
     */
    @After
    public void tearDown(){
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}
