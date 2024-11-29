package models;

import com.google.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Represents the current search history in the application.
 * Stores the search history and interfaces with the YouTube API.
 */
public class SearchHistoryModel {

    private final YouTube youtubeApiClient;
    private static final int MAX_SEARCHES = 10;
    private static final int RESULTS_PER_QUERY = 50;
    private LinkedList<SearchResult> searchHistory = new LinkedList<>();

    /**
     * Constructor for the SearchHistoryModel.
     *
     * @param youtubeApiClient YouTube API client for interactions with the YouTube API.
     */
    @Inject
    public SearchHistoryModel(YouTube youtubeApiClient) {
        this.youtubeApiClient = youtubeApiClient;
    }

    /**
     * @author Tomas Pereira
     *
     * Helper function.
     * Given a string of keywords, queries the Youtube API to retrieve 50 videos matching them.
     * Creates a Video object for each of these retrieved videos and stores them in a list.
     *
     * @param query The string of keywords being searched
     * @return The list of videos that are retrieved from the Youtube API using the given keywords
     */
    public List<Video> queryYoutube(String query) {
        return queryYoutubeWithNum(query, RESULTS_PER_QUERY);
    }

    /**
     * Given a string of keywords and the desired number of results, queries the YouTube API.
     *
     * @param query The search query string.
     * @param num   The number of videos to retrieve.
     * @return List of videos retrieved.
     */
    public List<Video> queryYoutubeWithNum(String query, int num) {
        try {
            JsonNode videosJson = youtubeApiClient.searchVideos(query, num);
            return extractVideosFromJson(videosJson);
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Using the YouTube API client, queries for a given number of video results.
     *
     * @param query      Query string being searched.
     * @param resultNum  Number of videos to retrieve.
     * @return JSON containing the information for the retrieved videos.
     */
    public JsonNode queryYoutube(String query, int resultNum) {
        try {
            return youtubeApiClient.searchVideos(query, resultNum);
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Given a query, calls the queryYoutube function and stores the search result in history.
     *
     * @param query The search query string.
     */
    public void queryAndStore(String query) {
        addSearchResult(query, queryYoutube(query));
    }

    /**
     * Given a query and the resulting video list, analyzes sentiment and stores the result in history.
     *
     * @param query  The query string.
     * @param videos The list of videos processed.
     */
    public void addSearchResult(String query, List<Video> videos) {
        if (searchHistory.size() == MAX_SEARCHES) {
            searchHistory.removeLast();
        }

        SubmissionSentiment sentimentAnalyzer = new SubmissionSentiment();
        String thisSentiment = sentimentAnalyzer.determineSentiment(videos);
        searchHistory.addFirst(new SearchResult(query, videos, thisSentiment));
    }

    /**
     * @author Sam Collin
     *
     * Method that retrieves all the information about a specific Youtube channel.
     *
     * This method calls the Youtube API to retrieve information about a specific channel identified by it's channelId given.
     * It retrieves the channelId, title, description, published date, country, customUrl, thumbnail URL, subscriber count, view count, and video count.
     * It then creates and returns a {@link Channel} object.
     *
     * @param channelId The ID of the channel whose information will be retrieved.
     * @return A {@link Channel} object containing all the information, if not found, it returns null.
     */
    public Channel getChannelDetails(String channelId) {
        try {
            JsonNode channelJson = youtubeApiClient.getChannelDetails(channelId);

            return StreamSupport.stream(channelJson.get("items").spliterator(), false)
                    .findFirst()
                    .map(channelNode -> new Channel(
                            channelNode.get("id").asText(),
                            channelNode.get("snippet").get("title").asText(),
                            channelNode.get("snippet").get("description").asText(),
                            channelNode.get("snippet").get("publishedAt").asText(),
                            channelNode.get("snippet").has("country") ? channelNode.get("snippet").get("country").asText() : "N/A", "N/A", // Custom URL
                            channelNode.get("snippet").get("thumbnails").get("default").get("url").asText(),
                            channelNode.get("statistics").get("subscriberCount").asInt(),
                            channelNode.get("statistics").get("hiddenSubscriberCount").asBoolean(),
                            channelNode.get("statistics").get("viewCount").asInt(),
                            channelNode.get("statistics").get("videoCount").asInt()
                    ))
                    .orElse(null);

        } catch (Exception e) {
            System.err.println("Error retrieving channel details: " + e.getMessage());
        }
        return null;
    }

    /**
     * @author Sam Collin
     *
     * This method retrieves the latest videos belonging to a specific youtube channel.
     *
     * This method calls the Youtube API to retrieve a user specified quantity of videos associated to a certain channel
     * identified by the given ID as parameters.<br>
     * It will return a list of {@link Video} objects containing info like videoId, title, channelId, channelTitle, description, ...
     *
     * @param channelId The channel where the latest videos are searched.
     * @param maxResults The number of videos that will be retrieved.
     * @return a list of {@link Video} objects containing the maxResults latest videos. If none is found, the list will just be empty.
     */
    public List<Video> getChannelVideos(String channelId, int maxResults) {
        try {
            JsonNode videosJson = youtubeApiClient.getVideosByChannelId(channelId, maxResults);
            return extractVideosFromJson(videosJson);
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Extracts a list of Video objects from a JSON node using streams.
     *
     * @param videosJson JSON containing video information.
     * @return List of Video objects.
     */
    private List<Video> extractVideosFromJson(JsonNode videosJson) {
        if (videosJson == null || !videosJson.has("items")) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(videosJson.get("items").spliterator(), false)
                .map(item -> {
                    try {
                        return new Video(
                                item.get("id").get("videoId").asText(),
                                item.get("snippet").get("title").asText(),
                                item.get("snippet").get("channelId").asText(),
                                item.get("snippet").get("channelTitle").asText(),
                                item.get("snippet").get("description").asText(),
                                item.get("snippet").get("thumbnails").get("default").get("url").asText()
                        );
                    } catch (Exception e) {
                        System.err.println("no new videos to extract");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Getter for the search history list.
     *
     * @return LinkedList of SearchResults representing the current search history.
     */
    public LinkedList<SearchResult> getSearchHistory() {
        return searchHistory;
    }
}
