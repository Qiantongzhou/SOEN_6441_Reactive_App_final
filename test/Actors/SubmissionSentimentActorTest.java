package Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.Video;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SubmissionSentimentActorTest {

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
     * Tests the functionality of the SubmissionSentiment when using message passing.
     * Similarly to the original model testing, verifies that the sentiment received matches the expectation.
     * Varies from the original in that it must first receive the message and then process it.
     */
    @Test
    public void testSentimentMessageSad(){
        new TestKit(actorSystem){{
            final Props props = SubmissionSentimentActor.props();
            final ActorRef sentimentActor = actorSystem.actorOf(props);

            Video happy = new Video("id1", "Sad", "ch1", "Channel 1", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb1");
            Video sad = new Video("id2", "Sad2", "ch2", "Channel 2", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb2");
            List<Video> videos = Arrays.asList(happy, sad);

            // Send the AnalyzeSentiment message
            sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(videos), getRef());

            // Check for expected return
            SubmissionSentimentActor.SentimentResult result = expectMsgClass(SubmissionSentimentActor.SentimentResult.class);
            assertEquals(":-(", result.getSentiment());
        }};
    }

    /**
     * @author Tomas Pereira
     *
     * Tests the functionality of the SubmissionSentiment actor by cross-checking videos in the result.
     */
    @Test
    public void testGetVideos(){
        new TestKit(actorSystem){{
            final Props props = SubmissionSentimentActor.props();
            final ActorRef sentimentActor = actorSystem.actorOf(props);

            Video happy = new Video("id1", "Sad", "ch1", "Channel 1", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb1");
            Video sad = new Video("id2", "Sad2", "ch2", "Channel 2", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb2");
            List<Video> videos = Arrays.asList(happy, sad);

            // Send the AnalyzeSentiment message
            sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(videos), getRef());

            // Check for expected return
            SubmissionSentimentActor.SentimentResult result = expectMsgClass(SubmissionSentimentActor.SentimentResult.class);

            assertEquals(videos, result.getVideos());
        }};
    }

    /**
     * @author Tomas Pereira
     *
     * Tests the functionality of the SubmissionSentiment actor by checking for the appropriate sentiment.
     */
    @Test
    public void testGetSentiment(){
        new TestKit(actorSystem){{
            final Props props = SubmissionSentimentActor.props();
            final ActorRef sentimentActor = actorSystem.actorOf(props);

            Video happy = new Video("id1", "Sad", "ch1", "Channel 1", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb1");
            Video sad = new Video("id2", "Sad2", "ch2", "Channel 2", "Unhappy Unhappy Unhappy Sad Sad Sad", "thumb2");
            List<Video> videos = Arrays.asList(happy, sad);

            // Send the AnalyzeSentiment message
            sentimentActor.tell(new SubmissionSentimentActor.AnalyzeSentiment(videos), getRef());

            // Check for expected return
            SubmissionSentimentActor.SentimentResult result = expectMsgClass(SubmissionSentimentActor.SentimentResult.class);

            assertEquals(":-(", result.getSentiment());
        }};
    }

    /**
     * @author Tomas Pereira
     * Tears down the generated actor system and nulls the reference.
     */
    @After
    public void teardown(){
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}
