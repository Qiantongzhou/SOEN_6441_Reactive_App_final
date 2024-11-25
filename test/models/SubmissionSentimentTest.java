package models;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static models.SubmissionSentiment.singleSentiment;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Tomas Pereira
 * Unit Testing for the Submission Sentiment Part
 */
public class SubmissionSentimentTest {

    SubmissionSentiment sentimentAnalyzer;
    Set<String> happySet;
    Set<String> sadSet;


    /**
     * @author Tomas Pereira
     * Before the tests, creates the SubmissionSentiment object which will be used the call the private methods.
     */
    @Before
    public void init(){
        sentimentAnalyzer = new SubmissionSentiment();
        happySet = new HashSet<>(Arrays.asList("Happy", "Smile", "Joy"));
        sadSet = new HashSet<>(Arrays.asList("Unhappy", "Sad", "Cry", "Tears"));
    }

    /**
     * @author Tomas Pereira
     * Tests whether a sad description will work properly.
     * The sad file contains [Unhappy,Sad,Cry,Tears].
     * The descrption "Unhappy Unhappy Unhappy Unhappy Sad Sad Sad" is thus 100% unhappy
     */
    @Test
    public void testSadSentiment(){
        String description = "Unhappy Unhappy Unhappy Unhappy Sad Sad Sad";

        String result = singleSentiment(description, happySet, sadSet);
        assertEquals (":-(", result, "Description should result in a Sad sentiment");
    }

    /**
     * @author Tomas Pereira
     * Tests whether a sad description will work properly.
     * The happy file contains [Happy,Smile,Joy].
     * The descrption "Smile Smile Smile Joy Joy Joy" is thus 100% unhappy
     */
    @Test
    public void testHappySentiment(){
        String description = "Smile Smile Smile Joy Joy Joy";

        String result = singleSentiment(description, happySet, sadSet);
        assertEquals (":-)", result, "Description should result in a Happy sentiment");
    }

    /**
     * @author Tomas Pereira
     * Tests whether a sad description will work properly.
     * The descrption "Smile Smile Smile Sad Sad Sad" is 50% happy and 50% sad, which does not meet the requirement for
     * either sentiment.
     */
    @Test
    public void testNeutralSentiment(){
        String description = "Smile Smile Smile Sad Sad Sad";

        String result = singleSentiment(description, happySet, sadSet);
        assertEquals (":-|", result, "Description should result in a Neutral sentiment");
    }

    /**
     * @author Tomas Pereira
     * Tests where an empty description will work properly.
     * Given no words in the description, there can not be any % of words being happy or sad,
     * so the sentiment must be neutral
     */
    @Test
    public void testEmptyDescription(){
        String description = "";

        String result = singleSentiment(description, happySet, sadSet);
        assertEquals (":-|", result, "Empty Description should result in a Neutral sentiment");
    }

    /**
     * @author Tomas Pereira
     * Tests whether a given sentiment file will correctly load the set of words.
     */
    @Test
    public void testLoadSentimentFileSuccess(){
        String filePath = "test/models/happyTestFile.txt";

        Set<String> testSet = sentimentAnalyzer.loadSentimentFile(filePath);
        assertNotNull(testSet, "The set should not be null for a valid Sentiment File");
        assertEquals(Set.of("Happy", "Smile", "Joy"), testSet, "Words from loaded files do not match expected");
    }

    /**
     * @author Tomas Pereira
     * Tests that a non-existing sentiment file will correctly give a null set.
     */
    @Test
    public void testLoadSentimentFileFail(){
        String filePath = "test/models/fakeTestFile.txt";

        Set<String> testSet = sentimentAnalyzer.loadSentimentFile(filePath);
        assertNull(testSet);
    }

    /**
     * @author Tomas Pereira
     * Tests the Determine Sentiment method to ensure that sentiment for a list of videos is correctly processed.
     */
    @Test
    public void testDetermineSentimentHappy(){
        Video video1 = new Video("vidID1", "title1", "channelID1", "channelTitle1", "Happy Happy Happy", "thumb1");
        Video video2 = new Video("vidID2", "title2", "channelID2", "channelTitle2", "Joy Joy Joy", "thumb2");

        LinkedList<Video> vidList = new LinkedList<>();
        vidList.addFirst(video1);
        vidList.addFirst(video2);

        assertEquals(sentimentAnalyzer.determineSentiment(vidList), ":-|");
    }

    /**
     * @author Tomas Pereira
     * Nulls the created sets and Submission sentiment object once the tests are complete.
     */
    @After
    public void teardown(){
        sentimentAnalyzer = null;
        happySet = null;
        sadSet = null;
    }

}
