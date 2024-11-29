package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.Channel;
import models.SearchHistoryModel;
import models.Video;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ChannelProfileActorTest {

    private static ActorSystem actorSystem;
    private SearchHistoryModel mockSearchHistoryModel;

    /**
     * @author Sam Collin
     * Initializes the ActorSystem and mocks the SearchHistoryModel before each test.
     */
    @Before
    public void init() {
        actorSystem = ActorSystem.create();
        mockSearchHistoryModel = Mockito.mock(SearchHistoryModel.class);
    }

    /**
     * @author Sam Collin
     * Tests the happy path where the channel profile and videos are fetched successfully.
     */
    @Test
    public void testFetchChannelProfileSuccess() {
        new TestKit(actorSystem) {{
            // Mocking SearchHistoryModel behavior
            Channel mockChannel = new Channel(
                    "channelId1", "Channel Title", "Description", "2022-01-01T00:00:00Z",
                    "US", "http://example.com/customUrl", "http://example.com/thumbnail.jpg",
                    1000, false, 5000, 100
            );
            List<Video> mockVideos = Arrays.asList(
                    new Video("videoId1", "Video Title 1", "channelId1", "Channel Title", "Description 1", "http://example.com/video1.jpg"),
                    new Video("videoId2", "Video Title 2", "channelId1", "Channel Title", "Description 2", "http://example.com/video2.jpg")
            );
            when(mockSearchHistoryModel.getChannelDetails(anyString())).thenReturn(mockChannel);
            when(mockSearchHistoryModel.getChannelVideos(anyString(), Mockito.anyInt())).thenReturn(mockVideos);

            // Creating the actor with mock dependencies
            final Props props = ChannelProfileActor.props("channelId1", getRef(), mockSearchHistoryModel);
            final ActorRef channelProfileActor = actorSystem.actorOf(props);

            // Expecting a ChannelProfileResult message
            ChannelProfileActor.ChannelProfileResult result = expectMsgClass(ChannelProfileActor.ChannelProfileResult.class);

            // Assertions
            assertEquals(mockChannel, result.channel);
            assertEquals(mockVideos, result.videos);
        }};
    }

    /**
     * @author Sam Collin
     * Tests the failure scenario where an exception is thrown while fetching channel profile.
     */
    @Test
    public void testFetchChannelProfileFailure() {
        new TestKit(actorSystem) {{
            // Mocking SearchHistoryModel to throw an exception
            when(mockSearchHistoryModel.getChannelDetails(anyString())).thenThrow(new RuntimeException("Error fetching channel details"));

            // Creating the actor with mock dependencies
            final Props props = ChannelProfileActor.props("channelId1", getRef(), mockSearchHistoryModel);
            final ActorRef channelProfileActor = actorSystem.actorOf(props);

            // Expecting a Failure message
            akka.actor.Status.Failure failure = expectMsgClass(akka.actor.Status.Failure.class);

            // Assertions
            assertEquals("Error fetching channel details", failure.cause().getMessage());
        }};
    }

    /**
     * @author Sam Collin
     * Shuts down the ActorSystem after each test.
     */
    @After
    public void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }
}
