package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;

import java.util.Collections;

import static org.mockito.Mockito.mock;

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
     * Tests whether the SubmissionSentimentActor is well integrated with the SearchActor.
     * When the SearchActor receives a search result, it will call handleVideoResults.
     * This processes the video result, and then sends a message to the SubmissionSentimentActor to process the
     * sentiment on that set of videos from the search result.
     * This requires catching the result from 2 message returns: the searchResult message and the analyzeSentiment message.
     */
    @Test
    public void testSentimentAnalysisIntegrationSearchResult(){
        new TestKit(actorSystem){{
            final SearchHistoryModel mockSearchHistoryModel = mock(SearchHistoryModel.class);
            final ActorRef out = getTestActor();
            final ActorRef searchActor = actorSystem.actorOf(Props.create(SearchActor.class, out, mockSearchHistoryModel));

            Video happy = new Video("id1", "Happy", "ch1", "Channel 1", "Smile Smile Smile Joy Joy Joy", "thumb1");
            // Singleton list used due to typing issue of simple list
            SearchResult searchResult = new SearchResult("test query", Collections.singletonList(happy), null);

            // Send the search result to the SearchActor
            searchActor.tell(searchResult, getRef());

            // First Receive the video message and check
            ObjectNode videoExpected = Json.newObject();
            videoExpected.put("type", "video");
            videoExpected.set("data", Json.toJson(happy));
            expectMsgEquals(videoExpected);

            // Check equality for sentiment
            ObjectNode summaryExpected = Json.newObject();
            summaryExpected.put("type", "summary");
            summaryExpected.put("sentiment", ":-)");
            expectMsgEquals(summaryExpected);
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
