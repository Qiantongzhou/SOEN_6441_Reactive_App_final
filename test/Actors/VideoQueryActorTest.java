package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.time.Duration;


import java.util.*;

import static org.junit.Assert.*;

public class VideoQueryActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("VideoQueryActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    // Mock SearchHistoryModel
    class MockSearchHistoryModel extends SearchHistoryModel {

        private List<Video> videosToReturn;

        public MockSearchHistoryModel(List<Video> videosToReturn) {
            super(null); // Assuming no YouTube client is needed for the mock
            this.videosToReturn = videosToReturn;
        }

        @Override
        public List<Video> queryYoutubeWithNum(String query, int numResults) {
            // Return the predefined list of videos
            return videosToReturn;
        }

        public void setVideosToReturn(List<Video> videos) {
            this.videosToReturn = videos;
        }
    }

    @Test
    public void testInitialTickWithNewVideos() {
        new TestKit(system) {{
            List<Video> mockVideos = Arrays.asList(
                    new Video("id1", "Title 1", "channelId1", "Channel 1", "Description 1", "thumbnail1"),
                    new Video("id2", "Title 2", "channelId2", "Channel 2", "Description 2", "thumbnail2")
            );

            MockSearchHistoryModel shModel = new MockSearchHistoryModel(mockVideos);

            final TestKit parentProbe = new TestKit(system);

            final ActorRef videoQueryActor = system.actorOf(
                    Props.create(VideoQueryActor.class, () -> new VideoQueryActor("test query", parentProbe.getRef(), shModel))
            );

            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());

            SearchResult searchResult = parentProbe.expectMsgClass(Duration.ofSeconds(1), SearchResult.class);

            assertEquals("test query", searchResult.query);
            assertEquals(2, searchResult.videos.size());
            assertEquals("", searchResult.sentiment);
        }};
    }

    @Test
    public void testSubsequentTickWithNoNewVideos() {
        new TestKit(system) {{
            List<Video> mockVideos = Collections.singletonList(
                    new Video("id1", "Title 1", "channelId1", "Channel 1", "Description 1", "thumbnail1")
            );

            MockSearchHistoryModel shModel = new MockSearchHistoryModel(mockVideos);

            final TestKit parentProbe = new TestKit(system);

            final ActorRef videoQueryActor = system.actorOf(
                    Props.create(VideoQueryActor.class, () -> new VideoQueryActor("test query", parentProbe.getRef(), shModel))
            );

            // First Tick
            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());
            SearchResult firstResult = parentProbe.expectMsgClass(Duration.ofSeconds(1), SearchResult.class);
            assertEquals(1, firstResult.videos.size());

            // Second Tick
            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());
            parentProbe.expectNoMessage(Duration.ofSeconds(1));
        }};
    }

    @Test
    public void testSubsequentTickWithNewVideos() {
        new TestKit(system) {{
            List<Video> initialVideos = Arrays.asList(
                    new Video("id1", "Title 1", "channelId1", "Channel 1", "Description 1", "thumbnail1")
            );

            MockSearchHistoryModel shModel = new MockSearchHistoryModel(initialVideos);

            final TestKit parentProbe = new TestKit(system);

            final ActorRef videoQueryActor = system.actorOf(
                    Props.create(VideoQueryActor.class, () -> new VideoQueryActor("test query", parentProbe.getRef(), shModel))
            );

            // First Tick
            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());
            SearchResult firstResult = parentProbe.expectMsgClass(Duration.ofSeconds(1), SearchResult.class);
            assertEquals(1, firstResult.videos.size());

            // Update mock videos with a new video
            List<Video> updatedVideos = Arrays.asList(
                    new Video("id1", "Title 1", "channelId1", "Channel 1", "Description 1", "thumbnail1"),
                    new Video("id2", "Title 2", "channelId2", "Channel 2", "Description 2", "thumbnail2")
            );
            shModel.setVideosToReturn(updatedVideos);

            // Second Tick
            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());
            SearchResult secondResult = parentProbe.expectMsgClass(Duration.ofSeconds(1), SearchResult.class);
            assertEquals(1, secondResult.videos.size());
            assertEquals("id2", secondResult.videos.get(0).getVideoId());
        }};
    }

    @Test
    public void testSchedulerCancellationOnStop() {
        new TestKit(system) {{
            MockSearchHistoryModel shModel = new MockSearchHistoryModel(Collections.emptyList());

            final TestKit parentProbe = new TestKit(system);

            final TestActorRef<VideoQueryActor> videoQueryActorRef = TestActorRef.create(
                    system,
                    Props.create(VideoQueryActor.class, () -> new VideoQueryActor("test query", parentProbe.getRef(), shModel))
            );

            VideoQueryActor actor = videoQueryActorRef.underlyingActor();

            // Verify scheduler is initialized
            assertNotNull(actor.cancellable);
            assertFalse(actor.cancellable.isCancelled());

            // Stop the actor
            videoQueryActorRef.stop();

            // Verify scheduler is cancelled
            assertTrue(actor.cancellable.isCancelled());
        }};
    }

    @Test
    public void testEmptyVideoList() {
        new TestKit(system) {{
            List<Video> emptyVideos = Collections.emptyList();

            MockSearchHistoryModel shModel = new MockSearchHistoryModel(emptyVideos);

            final TestKit parentProbe = new TestKit(system);

            final ActorRef videoQueryActor = system.actorOf(
                    Props.create(VideoQueryActor.class, () -> new VideoQueryActor("test query", parentProbe.getRef(), shModel))
            );

            videoQueryActor.tell(new VideoQueryActor.Tick(), ActorRef.noSender());

            parentProbe.expectNoMessage(Duration.ofSeconds(1));
        }};
    }
}
