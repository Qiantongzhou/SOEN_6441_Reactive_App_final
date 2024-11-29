package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SearchHistoryModel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.*;

public class WordStatsActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("WordStatsActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    // Mock SearchHistoryModel
    static class MockSearchHistoryModel extends SearchHistoryModel {

        private final boolean shouldThrowException;

        public MockSearchHistoryModel(boolean shouldThrowException) {
            super(null); // Pass null or a mock YouTube client if necessary
            this.shouldThrowException = shouldThrowException;
        }

        @Override
        public JsonNode queryYoutube(String query, int maxResults) {
            if (shouldThrowException) {
                throw new RuntimeException("Simulated exception");
            } else {
                // Return a mock JsonNode with video data
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rootNode = mapper.createObjectNode();
                ArrayNode itemsArray = mapper.createArrayNode();

                // Create mock video items
                ObjectNode video1 = createVideoNode("Hello World", "First description.");
                ObjectNode video2 = createVideoNode("Hello Again", "Second description.");

                itemsArray.add(video1);
                itemsArray.add(video2);

                rootNode.set("items", itemsArray);
                return rootNode;
            }
        }

        private ObjectNode createVideoNode(String title, String description) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode videoNode = mapper.createObjectNode();
            ObjectNode snippetNode = mapper.createObjectNode();
            snippetNode.put("title", title);
            snippetNode.put("description", description);
            videoNode.set("snippet", snippetNode);
            return videoNode;
        }
    }

    @Test
    public void testWordStatsActorSuccess() {
        new TestKit(system) {{
            SearchHistoryModel shModel = new MockSearchHistoryModel(false);
            final TestKit parentProbe = new TestKit(system);

            // Use the props method to create the WordStatsActor
            final ActorRef wordStatsActor = system.actorOf(
                    WordStatsActor.props("test query", parentProbe.getRef(), shModel)
            );

            // Expect a WordStatsResult message
            WordStatsActor.WordStatsResult result = parentProbe.expectMsgClass(
                    Duration.ofSeconds(5), WordStatsActor.WordStatsResult.class
            );

            // Verify the result
            assertEquals("test query", result.query);
            assertNotNull(result.wordStats);
            assertFalse(result.wordStats.isEmpty());

            // Check word counts
            Map<String, Long> wordStats = result.wordStats;
            assertEquals(Long.valueOf(2L), wordStats.get("hello"));
            assertEquals(Long.valueOf(1L), wordStats.get("world"));
            assertEquals(Long.valueOf(1L), wordStats.get("again"));
            assertEquals(Long.valueOf(1L), wordStats.get("first"));
            assertEquals(Long.valueOf(2L), wordStats.get("description"));
            assertEquals(Long.valueOf(1L), wordStats.get("second"));
        }};
    }

    @Test
    public void testWordStatsActorFailure() {
        new TestKit(system) {{
            SearchHistoryModel shModel = new MockSearchHistoryModel(true);
            final TestKit parentProbe = new TestKit(system);

            // Use the props method to create the WordStatsActor
            final ActorRef wordStatsActor = system.actorOf(
                    WordStatsActor.props("test query", parentProbe.getRef(), shModel)
            );

            // Expect a Failure message
            Status.Failure failure = parentProbe.expectMsgClass(
                    Duration.ofSeconds(5), Status.Failure.class
            );

            // Verify the exception
            assertTrue(failure.cause() instanceof RuntimeException);
            assertEquals("Simulated exception", failure.cause().getMessage());
        }};
    }

    @Test
    public void testActorTerminatesAfterSuccess() {
        new TestKit(system) {{
            SearchHistoryModel shModel = new MockSearchHistoryModel(false);
            final TestKit parentProbe = new TestKit(system);

            // Use the props method to create the WordStatsActor
            final ActorRef wordStatsActor = system.actorOf(
                    WordStatsActor.props("test query", parentProbe.getRef(), shModel)
            );

            // Watch the actor
            watch(wordStatsActor);

            // Expect a WordStatsResult message
            parentProbe.expectMsgClass(Duration.ofSeconds(5), WordStatsActor.WordStatsResult.class);

            // Expect the actor to terminate
            expectTerminated(Duration.ofSeconds(5), wordStatsActor);
        }};
    }

    @Test
    public void testActorTerminatesAfterFailure() {
        new TestKit(system) {{
            SearchHistoryModel shModel = new MockSearchHistoryModel(true);
            final TestKit parentProbe = new TestKit(system);

            // Use the props method to create the WordStatsActor
            final ActorRef wordStatsActor = system.actorOf(
                    WordStatsActor.props("test query", parentProbe.getRef(), shModel)
            );

            // Watch the actor
            watch(wordStatsActor);

            // Expect a Failure message
            parentProbe.expectMsgClass(Duration.ofSeconds(5), Status.Failure.class);

            // Expect the actor to terminate
            expectTerminated(Duration.ofSeconds(5), wordStatsActor);
        }};
    }
}
