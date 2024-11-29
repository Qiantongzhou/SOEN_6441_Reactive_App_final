package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Sam Collin
 * @author Tomas Pereira
 */
class SearchHistoryModelTest {

    private static final int MAX_SEARCHES = 10;
    @InjectMocks
    private SearchHistoryModel searchHistoryModel;

    @Mock
    private YouTube mockYoutube;
    @Mock
    private SubmissionSentiment sentimentAnalyzer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @author Sam Collin
     * Defines the setup before each test. It creates the mock of the YouTube class and injects it in a new SearchHistoryModel instance.
     */
    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for queryYoutube when the API has returned an Empty JSON response.
     * This leads to the results being an empty list.
     */
    @Test
    public void testQueryYoutubeNull() throws Exception {

        // Return null from Mock API
        JsonNode emptyResponse = objectMapper.readTree("{}");
        when(searchHistoryModel.queryYoutube(anyString(),anyInt())).thenReturn(emptyResponse);

        List<Video> result = searchHistoryModel.queryYoutube("test");

        assertEquals(0, result.size(), "List should be empty when the JSON response is empty.");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for queryYoutube with a successful API request.
     * The JSON with 2 entries leads to a List of size 2, with all the video details matching.
     */
    @Test
    public void testQueryYoutubeSuccess() throws Exception {
        // Fake API Response simulating a success
        String fakeResponse = """
        {
            "items": [
                {
                    "id": { "videoId": "testVideoId1" },
                    "snippet": {
                        "title": "Test Video Title 1",
                        "channelId": "testChannelId1",
                        "channelTitle": "Test Channel 1",
                        "description": "Test Description 1",
                        "thumbnails": {
                            "default": { "url": "https://fakeurl1.com/thumbnail.jpg" }
                        }
                    }
                },
                {
                    "id": { "videoId": "testVideoId2" },
                    "snippet": {
                        "title": "Test Video Title 2",
                        "channelId": "testChannelId2",
                        "channelTitle": "Test Channel 2",
                        "description": "Test Description 2",
                        "thumbnails": {
                            "default": { "url": "https://fakeurl2.com/thumbnail.jpg" }
                        }
                    }
                }
            ]
        }
        """;

        // Fake the API call
        JsonNode data = objectMapper.readTree(fakeResponse);
        when(mockYoutube.searchVideos(anyString(), anyInt())).thenReturn(data);

        List<Video> result = searchHistoryModel.queryYoutube("test");

        // Check All is as Expected
        assertEquals(2, result.size(), "Expected 2 videos in the result list.");

        assertEquals("testVideoId1", result.getFirst().getVideoId());
        assertEquals("Test Video Title 1", result.getFirst().getTitle());
        assertEquals("testChannelId1", result.getFirst().getChannelId());
        assertEquals("Test Channel 1", result.getFirst().getChannelTitle());
        assertEquals("Test Description 1", result.getFirst().getDescription());
        assertEquals("https://fakeurl1.com/thumbnail.jpg", result.getFirst().getThumbnail());


        assertEquals("testVideoId2", result.get(1).getVideoId());
        assertEquals("Test Video Title 2", result.get(1).getTitle());
        assertEquals("testChannelId2", result.get(1).getChannelId());
        assertEquals("Test Channel 2", result.get(1).getChannelTitle());
        assertEquals("Test Description 2", result.get(1).getDescription());
        assertEquals("https://fakeurl2.com/thumbnail.jpg", result.get(1).getThumbnail());
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test for the case when the JSON is missing some field. Leads to that specific video being skipped.
     */
    @Test
    public void queryYoutubeMissingField() throws Exception {
        String fakeResponse = """
        {
            "items": [
                {
                    "id": { "videoId": "testVideoId1" },
                    "snippet": {
                        "title": "Test Video Title 1",
                        "channelId": "testChannelId1",
                        "channelTitle": "Test Channel 1",
                        "thumbnails": {
                            "default": { "url": "https://fakeurl1.com/thumbnail.jpg" }
                        }
                    }
                },
                {
                    "id": { "videoId": "testVideoId2" },
                    "snippet": {
                        "title": "Test Video Title 2",
                        "channelId": "testChannelId2",
                        "channelTitle": "Test Channel 2",
                        "description": "Test Description 2",
                        "thumbnails": {
                            "default": { "url": "https://fakeurl2.com/thumbnail.jpg" }
                        }
                    }
                }
            ]
        }
        """;

        // Fake the API call
        JsonNode data = objectMapper.readTree(fakeResponse);
        when(mockYoutube.searchVideos(anyString(), anyInt())).thenReturn(data);

        List<Video> result = searchHistoryModel.queryYoutube("test");

        assertEquals(1, result.size(), "A missing field in the Video JSON, should result in the video being skipped");
    }

    /**
     * @author Tongzhou Qian
     * @throws Exception
     * The test case throw API Exception when called with any String
     */
    @Test
    public void testQueryYoutubeWithRuntimeException() throws Exception {
        when(mockYoutube.searchVideos("testQuery", 50)).thenThrow(new RuntimeException("API error"));

        List<Video> videos = searchHistoryModel.queryYoutube("testQuery");

        assertTrue(videos.isEmpty(), "Expected an empty list when RuntimeException is thrown");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for when the query leads to an IO Exception (Network Issue)
     */
    @Test
    public void queryYoutubeIOException() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new IOException("Network error"));

        List<Video> result = searchHistoryModel.queryYoutube("test query");

        assertEquals(0, result.size(), "List should be empty when an IOException occurs.");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception When there is an error in the API call
     *
     * Test case for when the query leads to a Runtime Exception (bad response from API)
     */
    @Test
    public void queryYoutubeRuntimeException() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new RuntimeException("API error"));

        List<Video> result = searchHistoryModel.queryYoutube("test query");

        assertEquals(0, result.size(), "List should be empty when an RuntimeException occurs.");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for when the query leads to a general Exception
     */
    @Test
    public void queryYoutubeException() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new Exception("Error"));

        List<Video> result = searchHistoryModel.queryYoutube("test query");

        assertEquals(0, result.size(), "List should be empty when an Exception occurs.");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for when the query (overloaded with Int param) leads to an IO Exception (Network Issue)
     */
    @Test
    public void queryYoutubeIOExceptionWithInt() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new IOException("Network error"));

        JsonNode result = searchHistoryModel.queryYoutube("test query", 5);

        assertNull(result, "Result should be Null when encountering an exception");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for when the query (overloaded with Int param) leads to a Runtime Exception (API Issue)
     */
    @Test
    public void queryYoutubeRuntimeExceptionWithInt() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new RuntimeException("Runtime Error"));

        JsonNode result = searchHistoryModel.queryYoutube("test query", 5);

        assertNull(result, "Result should be Null when encountering an exception");
    }

    /**
     * @author Tomas Pereira
     * @throws Exception
     *
     * Test case for when the query (overloaded with Int param) leads to a general Exception
     */
    @Test
    public void queryYoutubeExceptionWithInt() throws Exception{

        when(mockYoutube.searchVideos(anyString(), anyInt())).thenThrow(new Exception("Error"));

        JsonNode result = searchHistoryModel.queryYoutube("test query", 5);

        assertNull(result, "Result should be Null when encountering an exception");
    }

    /**
     * @author Tomas Pereira
     *
     * Test for the addSearchResult method when the history list is already at full capacity.
     * After the operation, Query 0 will be gone in favour of Query 10
     */
    @Test
    public void testAddSearchResultFullList(){

        // First populate with Max examples to fill the list
        when(sentimentAnalyzer.determineSentiment(anyList())).thenReturn("neutral");
        for (int i = 0; i < MAX_SEARCHES; i++) {
            List<Video> videos = new ArrayList<>();  // empty video list
            searchHistoryModel.addSearchResult("query" + i, videos);
        }

        // Initital checks
        assertEquals(MAX_SEARCHES, searchHistoryModel.getSearchHistory().size(), "List should be full before the operation.");
        assertEquals("query0", searchHistoryModel.getSearchHistory().getLast().query, "Tail of the list should be query0");

        // New Video to Add in
        List<Video> vidsToAdd = new ArrayList<>();
        searchHistoryModel.addSearchResult("test", vidsToAdd);

        // Check states
        List<SearchResult> historyToCheck = searchHistoryModel.getSearchHistory();
        assertEquals(MAX_SEARCHES, historyToCheck.size(), "Size should still be Max after the operation");
        assertEquals("test", historyToCheck.getFirst().query, "New Video queried by 'test' should now be at the head");
        assertEquals("query1", historyToCheck.getLast().query, "The new tail should be query1 after query0 is removed");
      
    }

    /**
     * @throws Exception
     * @author Sam Collin
     * This tests simulate a successful call to the api to retrieve specific information about a channel.
     * It creates its own fake JSON file and then pass it when the Youtube class is supposed to be called.
     * After that, the method getChannelDetails is just tested in the case everything works fine and we verify that all the information have been retrieved.
     */
    @Test
    public void testGetChannelDetails() throws Exception {

        String fakeChannelId = "0000000";

        String fakeChannelResponseFromAPI = """
                    {
                      "items": [
                        {
                          "id": "0000000",
                          "snippet": {
                            "title": "Fake Channel Title",
                            "description": "This description is a fake one.",
                            "publishedAt": "2024-11-08T12:00:00Z",
                            "country": "CA",
                            "thumbnails": {
                              "default": {
                                "url": "https://fakeurl.com/thumbnail.jpg"
                              }
                            }
                          },
                          "statistics": {
                            "subscriberCount": "999",
                            "hiddenSubscriberCount": false,
                            "viewCount": "14000",
                            "videoCount": "47"
                          }
                        }
                      ]
                    }
                """;

        // This step allows to create a json node object hich is what the API usually returns.
        JsonNode fakeJsonNode = new ObjectMapper().readTree(fakeChannelResponseFromAPI);

        // This is where we trick our program into thinking that the API returned this JSON node when it was called with the ID we've just created.
        when(mockYoutube.getChannelDetails(fakeChannelId)).thenReturn(fakeJsonNode);

        // We then make our call
        Channel result = searchHistoryModel.getChannelDetails(fakeChannelId);

        assertNotNull(result);
        assertEquals("0000000", result.getId());
        assertEquals("Fake Channel Title", result.getTitle());
        assertEquals("This description is a fake one.", result.getDescription());
        assertEquals("2024-11-08T12:00:00Z", result.getPublishedAt());
        assertEquals("CA", result.getCountry());
        assertEquals("https://fakeurl.com/thumbnail.jpg", result.getThumbnailUrl());
        assertEquals(999, result.getSubscriberCount());
        assertFalse(result.getHiddenSubscriberCount());
        assertEquals(14000, result.getViewCount());
        assertEquals(47, result.getVideoCount());

    }
    /**
     * @throws Exception
     * @author Sam Collin
     * This tests simulate a successful call to the api to retrieve specific information about a channel.
     * It creates its own fake JSON file and then pass it when the Youtube class is supposed to be called.
     * After that, the method getChannelDetails is just tested in the case everything works fine and we verify that all the information have been retrieved.
     */
    @Test
    public void testGetChannelDetailswithoutcountry() throws Exception {

        String fakeChannelId = "0000000";

        String fakeChannelResponseFromAPI = """
                    {
                      "items": [
                        {
                          "id": "0000000",
                          "snippet": {
                            "title": "Fake Channel Title",
                            "description": "This description is a fake one.",
                            "publishedAt": "2024-11-08T12:00:00Z",
                            "thumbnails": {
                              "default": {
                                "url": "https://fakeurl.com/thumbnail.jpg"
                              }
                            }
                          },
                          "statistics": {
                            "subscriberCount": "999",
                            "hiddenSubscriberCount": false,
                            "viewCount": "14000",
                            "videoCount": "47"
                          }
                        }
                      ]
                    }
                """;

        // This step allows to create a json node object hich is what the API usually returns.
        JsonNode fakeJsonNode = new ObjectMapper().readTree(fakeChannelResponseFromAPI);

        // This is where we trick our program into thinking that the API returned this JSON node when it was called with the ID we've just created.
        when(mockYoutube.getChannelDetails(fakeChannelId)).thenReturn(fakeJsonNode);

        // We then make our call
        Channel result = searchHistoryModel.getChannelDetails(fakeChannelId);

        assertNotNull(result);
        assertEquals("0000000", result.getId());
        assertEquals("Fake Channel Title", result.getTitle());
        assertEquals("This description is a fake one.", result.getDescription());
        assertEquals("2024-11-08T12:00:00Z", result.getPublishedAt());
        assertEquals("N/A", result.getCountry());
        assertEquals("https://fakeurl.com/thumbnail.jpg", result.getThumbnailUrl());
        assertEquals(999, result.getSubscriberCount());
        assertFalse(result.getHiddenSubscriberCount());
        assertEquals(14000, result.getViewCount());
        assertEquals(47, result.getVideoCount());

    }
    /**
     * @throws Exception
     * @author Sam Collin
     * This one is based on the same architecture than the prevous one but simulates the case where the API encounters an error. The function returns null then.
     */
    @Test
    public void testGetChannelDetailsFailed() throws Exception {

        String fakeChannelId = "0000000";

        // This is where we trick our program into thinking that the API returned and error bt returning a runtime exception
        when(mockYoutube.getChannelDetails(fakeChannelId)).thenThrow(new RuntimeException());


        // We then make our call
        Channel result = searchHistoryModel.getChannelDetails(fakeChannelId);

        // This time we want to make sure the result is null, because an exception occurs
        assertNull(result);
    }


    /**
     * @throws Exception
     * @author Sam Collin
     * This tests simulates a success when retrieving a list of videos from a given channel.
     */
    @Test
    public void testGetChannelVideos() throws Exception {

        String testChannelId = "0000000";
        int testMaxResults = 3;


        String fakeVideosListReceivedFromApi = """
                    {
                      "items": [
                        {
                          "id": {
                            "videoId": "01"
                          },
                          "snippet": {
                            "title": "Monday",
                            "channelId": "12121212",
                            "channelTitle": "Fake Channel",
                            "description": "Video 1",
                            "thumbnails": {
                              "default": {
                                "url": "https://fakeurl.com/vidMonday.jpg"
                              }
                            }
                          }
                        },
                        {
                          "id": {
                            "videoId": "02"
                          },
                          "snippet": {
                            "title": "Tuesday",
                            "channelId": "12121212",
                            "channelTitle": "Fake Channel",
                            "description": "Video 2",
                            "thumbnails": {
                              "default": {
                                "url": "https://fakeurl.com/vidTuesday.jpg"
                              }
                            }
                          }
                        },
                        {
                          "id": {
                            "videoId": "03"
                          },
                          "snippet": {
                            "title": "Wednesday",
                            "channelId": "12121212",
                            "channelTitle": "Fake Channel",
                            "description": "Video 3",
                            "thumbnails": {
                              "default": {
                                "url": "https://fakeurl.com/vidWednesday.jpg"
                              }
                            }
                          }
                        }
                      ]
                    }
                """;

        // Transform our string into JSON Node
        JsonNode fakeJsonNode = new ObjectMapper().readTree(fakeVideosListReceivedFromApi);
        // Returns it instead of real API call
        when(mockYoutube.getVideosByChannelId(testChannelId, testMaxResults)).thenReturn(fakeJsonNode);
        // Call the tested method
        List<Video> result = searchHistoryModel.getChannelVideos(testChannelId, testMaxResults);

        assertNotNull(result);
        assertEquals(3, result.size());

        // Validate the details of the third video
        Video video = result.get(2);
        assertEquals("03", video.getVideoId());
        assertEquals("Wednesday", video.getTitle());
        assertEquals("12121212", video.getChannelId());
        assertEquals("Fake Channel", video.getChannelTitle());
        assertEquals("Video 3", video.getDescription());
        assertEquals("https://fakeurl.com/vidWednesday.jpg", video.getThumbnail());
    }


    /**
     * @author Sam Collin
     * This tests simulates a success when retrieving an EMPTY list of videos from a given channel.
     */
    @Test
    public void testGetChannelVideosWithEmptyResponse() throws Exception {

        String testChannelId = "0000000";
        int testMaxResults = 3;


        String fakeEmptyVideosListReceivedFromApi = """
                    {
                      "items": []
                    }
                """;

        // Transform our string into JSON Node
        JsonNode fakeJsonNode = new ObjectMapper().readTree(fakeEmptyVideosListReceivedFromApi);
        // Returns it instead of real API call
        when(mockYoutube.getVideosByChannelId(testChannelId, testMaxResults)).thenReturn(fakeJsonNode);
        // Call the tested method
        List<Video> result = searchHistoryModel.getChannelVideos(testChannelId, testMaxResults);

        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    /**
     * @author Sam Collin
     * This tests simulates a failure caused by a RuntimeException when trying to retrieve a list of videos from a given channel.
     * It simulates an error with the API.
     */
    @Test
    public void testGetChannelVideosWithExceptionErrorApiResponse() throws Exception {
        // Fake channel ID and max results
        String fakeChannelId = "UC123456789";
        int maxResults = 3;

        // Simulate an exception being thrown by the YouTube API
        when(mockYoutube.getVideosByChannelId(fakeChannelId, maxResults)).thenThrow(new RuntimeException());

        // Call the method under test
        List<Video> result = searchHistoryModel.getChannelVideos(fakeChannelId, maxResults);

        // Assertions to validate the result
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    /**
     * @author Sam Collin
     * This tests simulates a failure caused by a IOException when trying to retrieve a list of videos from a given channel.
     * It simulates an error with the API.
     */
    @Test
    public void testGetChannelVideosWithIOException() throws Exception {
        // Fake channel ID and max results
        String fakeChannelId = "UC123456789";
        int maxResults = 3;

        // Simulate an exception being thrown by the YouTube API
        when(mockYoutube.getVideosByChannelId(fakeChannelId, maxResults)).thenThrow(new IOException());

        // Call the method under test
        List<Video> result = searchHistoryModel.getChannelVideos(fakeChannelId, maxResults);

        // Assertions to validate the result
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    /**
     * @author Sam Collin
     * This tests simulates a failure caused by a IOException when trying to retrieve a list of videos from a given channel.
     * It simulates an error with the API.
     */
    @Test
    public void testGetChannelVideosWithException() throws Exception {
        // Fake channel ID and max results
        String fakeChannelId = "UC123456789";
        int maxResults = 3;

        // Simulate an exception being thrown by the YouTube API
        when(mockYoutube.getVideosByChannelId(fakeChannelId, maxResults)).thenThrow(new Exception());

        // Call the method under test
        List<Video> result = searchHistoryModel.getChannelVideos(fakeChannelId, maxResults);

        // Assertions to validate the result
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    public void testQueryYoutubeWithNullQuery() {
        // Test queryYoutube(String) with null query
        List<Video> videos = searchHistoryModel.queryYoutube(null);
        assertNotNull(videos, "Expected an empty list, not null, when query is null");
        assertTrue(videos.isEmpty(), "Expected an empty list when query is null");

        // Test queryYoutube(String, int) with null query
        JsonNode result = searchHistoryModel.queryYoutube(null, 10);
        assertNull(result, "Expected null when query is null for JsonNode method");
    }

    @Test
    public void testQueryAndStoreWithNullQuery() {
        // Test queryAndStore(String) with null query
        assertDoesNotThrow(() -> searchHistoryModel.queryAndStore(null),
                "Expected queryAndStore to handle null query without throwing an exception");

        assertFalse(searchHistoryModel.getSearchHistory().isEmpty(),
                "Expected search history to remain empty when queryAndStore is called with null");
    }

    @Test
    public void testAddSearchResultWithNullArguments() {

        assertTrue(searchHistoryModel.getSearchHistory().isEmpty(),
                "Expected search history to remain empty when addSearchResult is called with null arguments");


        assertTrue(searchHistoryModel.getSearchHistory().isEmpty(),
                "Expected search history to remain empty when addSearchResult is called with null videos");
    }

    @Test
    public void testGetChannelDetailsWithNullChannelId() {
        // Test getChannelDetails(String) with null channelId
        Channel channel = searchHistoryModel.getChannelDetails(null);
        assertNull(channel, "Expected null when channelId is null");
    }

    @Test
    public void testGetChannelVideosWithNullChannelId() {
        // Test getChannelVideos(String, int) with null channelId
        List<Video> videos = searchHistoryModel.getChannelVideos(null, 10);
        assertNotNull(videos, "Expected an empty list, not null, when channelId is null");
        assertTrue(videos.isEmpty(), "Expected an empty list when channelId is null");
    }



}
