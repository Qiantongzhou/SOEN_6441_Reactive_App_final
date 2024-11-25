package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the WordStats.generateWordStats method.
 * This class contains unit tests to the word statistics generation
 * from video data represented in JSON format.
 *
 * Author: qiantongzhou
 */
public class WordstatTest {
    /**
     * Tests that the method returns an empty map when the input is null.
     * method handles null inputs without throwing exceptions.
     * @author Tongzhou Qian
     */
    @Test
    public void NullInput() {
        Map<String, Long> result = WordStats.generateWordStats(null);
        assertTrue(result.isEmpty(), "Result should be empty for null input");
    }
    /**
     * Tests that the method returns an empty map
     * Verifies that the method checks for the presence of 'items' before processing.
     *@author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void NoItemsField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode videos = mapper.createObjectNode();
        Map<String, Long> result = WordStats.generateWordStats(videos);
        assertTrue(result.isEmpty(), "Result should be empty when 'items' field is missing");
    }
    /**
     * Tests that the method returns an empty map
     * Ensures that an empty 'items' array doesn't cause any processing errors.
     *@author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void EmptyItems() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{ \"items\": [] }";
        JsonNode videos = mapper.readTree(jsonString);
        Map<String, Long> result = WordStats.generateWordStats(videos);
        assertTrue(result.isEmpty(), "Result should be empty");
    }


    /**
     * Tests that the method correctly counts words
     * Checks the aggregation and counting logic for multiple video entries.
     *@author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void MultipleVideos() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{ \"items\": [" +
                "{ \"snippet\": { \"title\": \"First Video\", \"description\": \"First description.\" } }," +
                "{ \"snippet\": { \"title\": \"Second Video\", \"description\": \"Second description.\" } }," +
                "{ \"snippet\": { \"title\": \"First Video\", \"description\": \"Another description.\" } }" +
                "] }";
        JsonNode videos = mapper.readTree(jsonString);

        Map<String, Long> result = WordStats.generateWordStats(videos);
        //counting number of words
        assertEquals(Long.valueOf(3), result.get("first"));
        assertEquals(Long.valueOf(3), result.get("video"));
        assertEquals(Long.valueOf(3), result.get("description"));
        assertEquals(Long.valueOf(2), result.get("second"));
        assertEquals(Long.valueOf(1), result.get("another"));
    }
    /**
     * Tests that the method returns an empty map
     * Ensures that videos without 'snippet' are skipped without causing errors.
     *  @author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void VideoWithoutSnippet() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{ \"items\": [{ }] }";
        JsonNode videos = mapper.readTree(jsonString);

        Map<String, Long> result = WordStats.generateWordStats(videos);
        assertTrue(result.isEmpty(), "Result should be empty when 'snippet' is missing");
    }
    /**
     * Tests that the method returns an empty map
     * Validates that the method handles missing 'title' and 'description' gracefully.
     *  @author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void VideoWithoutTitleDescription() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{ \"items\": [{ \"snippet\": { } }] }";
        JsonNode videos = mapper.readTree(jsonString);

        Map<String, Long> result = WordStats.generateWordStats(videos);
        assertTrue(result.isEmpty(), "Result should be empty when 'title' and 'description' are missing");
    }
    /**
     * Tests the method with mixed content
     * Checks the normalization and counting logic with various input scenarios.
     *  @author Tongzhou Qian
     * @throws Exception if JSON parsing fails
     */
    @Test
    public void Completetest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{ \"items\": [" +
                "{ \"snippet\": { \"title\": \"Java & Kotlin\", \"description\": \"Programming languages.\" } }," +
                "{ \"snippet\": { \"title\": \"Play Framework\", \"description\": \"A web framework for Java and Scala.\" } }," +
                "{ \"snippet\": { \"title\": \"\", \"description\": \"No title here.\" } }" +
                "] }";
        JsonNode videos = mapper.readTree(jsonString);

        Map<String, Long> result = WordStats.generateWordStats(videos);
        //counting words appeared in data
        assertEquals(Long.valueOf(2), result.get("java"));
        assertEquals(Long.valueOf(1), result.get("kotlin"));
        assertEquals(Long.valueOf(1), result.get("programming"));
        assertEquals(Long.valueOf(1), result.get("languages"));
        assertEquals(Long.valueOf(1), result.get("play"));
        assertEquals(Long.valueOf(2), result.get("framework"));
        assertEquals(Long.valueOf(1), result.get("web"));
        assertEquals(Long.valueOf(1), result.get("scala"));
        assertEquals(Long.valueOf(1), result.get("no"));
        assertEquals(Long.valueOf(1), result.get("title"));
        assertEquals(Long.valueOf(1), result.get("here"));
    }

}
