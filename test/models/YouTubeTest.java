package models;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class YouTubeTest {

    private YouTube youTube;
    private HttpClient mockHttpClient;

    @Before
    public void setUp() {
        mockHttpClient = Mockito.mock(HttpClient.class);
        // Initialize with default API key for tests
        youTube = new YouTube(mockHttpClient, new String[]{"test_api_key"});
    }

    @Test
    public void testSearchVideosSuccess() throws Exception {
        String query = "test";
        int maxResults = 5;
        String apiKey = "test_api_key";

        youTube = new YouTube(mockHttpClient, new String[]{apiKey});

        String expectedUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&maxResults=" + maxResults + "&order=date&key=" + apiKey;

        // Mock the HttpClient response
        String mockResponse = "{ \"items\": [ { \"id\": \"video1\" }, { \"id\": \"video2\" } ] }";
        when(mockHttpClient.get(expectedUrl)).thenReturn(mockResponse);

        // Call the method
        JsonNode result = youTube.searchVideos(query, maxResults);

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("items"));
        assertEquals(2, result.get("items").size());
    }
    @Test
    public void testApiKeyRotation() throws Exception {
        String query = "test";
        int maxResults = 5;

        String[] apiKeys = {"invalid_api_key", "valid_api_key"};
        youTube = new YouTube(mockHttpClient, apiKeys);

        String baseUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&maxResults=" + maxResults + "&order=date&key=";

        String urlWithFirstKey = baseUrl + apiKeys[0];
        String urlWithSecondKey = baseUrl + apiKeys[1];

        // Mock the HttpClient to throw exception for the first key
        when(mockHttpClient.get(urlWithFirstKey)).thenThrow(new Exception("Failed with HTTP code: 403"));
        // Mock the HttpClient response for the second key
        String mockResponse = "{ \"items\": [ { \"id\": \"video1\" } ] }";
        when(mockHttpClient.get(urlWithSecondKey)).thenReturn(mockResponse);

        // Call the method
        JsonNode result = youTube.searchVideos(query, maxResults);

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("items"));
        assertEquals(1, result.get("items").size());
    }
    @Test
    public void testGetChannelDetailsSuccess() throws Exception {
        String channelId = "UC123456";
        String apiKey = "test_api_key";

        youTube = new YouTube(mockHttpClient, new String[]{apiKey});

        String expectedUrl = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" +
                channelId + "&key=" + apiKey;

        // Mock the HttpClient response
        String mockResponse = "{ \"items\": [ { \"id\": \"" + channelId + "\" } ] }";
        when(mockHttpClient.get(expectedUrl)).thenReturn(mockResponse);

        // Call the method
        JsonNode result = youTube.getChannelDetails(channelId);

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("items"));
        assertEquals(1, result.get("items").size());
        assertEquals(channelId, result.get("items").get(0).get("id").asText());
    }
    @Test
    public void testGetVideosByChannelIdSuccess() throws Exception {
        String channelId = "UC123456";
        int maxResults = 5;
        String apiKey = "test_api_key";

        youTube = new YouTube(mockHttpClient, new String[]{apiKey});

        String expectedUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=" + channelId +
                "&maxResults=" + maxResults + "&order=date&key=" + apiKey;

        // Mock the HttpClient response
        String mockResponse = "{ \"items\": [ { \"id\": \"video1\" }, { \"id\": \"video2\" } ] }";
        when(mockHttpClient.get(expectedUrl)).thenReturn(mockResponse);

        // Call the method
        JsonNode result = youTube.getVideosByChannelId(channelId, maxResults);

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("items"));
        assertEquals(2, result.get("items").size());
    }
    @Test
    public void testApiFailure() throws Exception {
        String query = "test";
        int maxResults = 5;

        String[] apiKeys = {"invalid_key1", "invalid_key2"};
        youTube = new YouTube(mockHttpClient, apiKeys);

        String baseUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&maxResults=" + maxResults + "&order=date&key=";

        String urlWithFirstKey = baseUrl + apiKeys[0];
        String urlWithSecondKey = baseUrl + apiKeys[1];

        // Mock the HttpClient to throw exceptions for both keys
        when(mockHttpClient.get(urlWithFirstKey)).thenThrow(new Exception("Failed with HTTP code: 403"));
        when(mockHttpClient.get(urlWithSecondKey)).thenThrow(new Exception("Failed with HTTP code: 403"));

        // Call the method
        JsonNode result = youTube.searchVideos(query, maxResults);

        // Verify that an error JSON is returned
        assertNotNull(result);
        assertTrue(result.has("error"));
        assertTrue(result.get("error").asText().contains("All API keys failed"));
    }
    @Test
    public void testUnknownFailure() throws Exception {
        // Arrange
        String[] emptyApiKeys = {};
        youTube = new YouTube(mockHttpClient, emptyApiKeys);

        // Act
        JsonNode result = youTube.searchVideos("test query", 5);

        // Assert
        assertNotNull(result);
        assertTrue(result.has("error"));
        assertEquals("Unknown failure occurred.", result.get("error").asText());
    }

}
