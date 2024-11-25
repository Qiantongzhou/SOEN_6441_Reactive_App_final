package models;

import com.google.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tomas Pereira
 *
 * Represents the current search history in the application.
 * Stores the search history and interfaces with the Youtube API.
 */
public class SearchHistoryModel {


    private final YouTube youtubeApiClient;
    private static final int MAX_SEARCHES = 10;
    private static final int RESULTS_PER_QUERY = 50;
    private LinkedList<SearchResult> searchHistory = new LinkedList<>();

    /**
     * @author Tomas Pereira
     * Constructor for the SearchHistory model.
     * @param youtubeApiClient YoutubeApiClient which takes care of the API interactions for the SearchHistoryModel.
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
    public List<Video> queryYoutube(String query){

        List<Video> videos = new ArrayList<>();

        try {
            JsonNode videosJson = youtubeApiClient.searchVideos(query, RESULTS_PER_QUERY);

                for (JsonNode item : videosJson.get("items")) {
                    try {
                        Video video = new Video(
                                item.get("id").get("videoId").asText(),
                                item.get("snippet").get("title").asText(),
                                item.get("snippet").get("channelId").asText(),
                                item.get("snippet").get("channelTitle").asText(),
                                item.get("snippet").get("description").asText(),
                                item.get("snippet").get("thumbnails").get("default").get("url").asText()
                        );
                        videos.add(video);
                    }
                    catch (Exception e){
                        System.err.println("Error in Extracting from Video:" + e);

                    }
                }

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error with API response: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return videos;
    }

    /**
     * @author Tongzhou Qian
     *
     * Using the YoutubeApiClient, queries for a given number of video results.
     *
     * @param query Query string being search.
     * @param Result_num Integer number of videos to be returned.
     * @return JSON containing the information for each of the returned videos.
     */
    public JsonNode queryYoutube(String query,int Result_num){

        JsonNode videosJson = null;

        try {
            videosJson = youtubeApiClient.searchVideos(query, Result_num);

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error with API response: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return videosJson;
    }

    /**
     * @author Tomas Pereira
     * Given a string query. Calls the queryYoutube function and stores the pair of Query string and Video list in the search history.
     * @param query The query string being searched
     */
    public void queryAndStore(String query){
        addSearchResult(query, queryYoutube(query));
    }

    /**
     * @author Tomas Pereira
     *
     * Given a query and the resulting video list. Analyzes the sentiment across these videos and stores the
     * information in a SearchResult object. The SearchResult is then added to the searchHistory list.
     *
     * @param query The query string being searched
     * @param videos The list of videos being processed
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
     * @author Tomas Pereira
     *
     * Getter for the SearchHistory list.
     *
     * @return LinkedList of SearchResults representing the current searchHistory.
     */
    public LinkedList<SearchResult> getSearchHistory() {
        return searchHistory;
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
    public Channel getChannelDetails(String channelId){
        try{
            JsonNode channelJson = youtubeApiClient.getChannelDetails(channelId);

                JsonNode channelNode = channelJson.get("items").get(0);
                String id = channelNode.get("id").asText();
                String title = channelNode.get("snippet").get("title").asText();
                String description = channelNode.get("snippet").get("description").asText();
                String publishedAt = channelNode.get("snippet").get("publishedAt").asText();
                String country = channelNode.get("snippet").has("country") ? channelNode.get("snippet").get("country").asText() : "N/A";
                String customUrl =  "N/A";
                String thumbnailUrl = channelNode.get("snippet").get("thumbnails").get("default").get("url").asText();
                int subscriberCount = channelNode.get("statistics").get("subscriberCount").asInt();
                boolean hiddenSubscriberCount = channelNode.get("statistics").get("hiddenSubscriberCount").asBoolean();
                int viewCount = channelNode.get("statistics").get("viewCount").asInt();
                int videoCount = channelNode.get("statistics").get("videoCount").asInt();

                return new Channel(id, title, description, publishedAt, country, customUrl, thumbnailUrl,
                        subscriberCount, hiddenSubscriberCount, viewCount, videoCount);

        } catch (Exception e) {
            System.err.println("Retrieving channel details encounter a problem : " + e.getMessage());}
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
    public List<Video> getChannelVideos(String channelId, int maxResults){

        List<Video> channelVideos = new ArrayList<>();
        try {
            JsonNode videosJson = youtubeApiClient.getVideosByChannelId(channelId, maxResults);

                for (JsonNode item : videosJson.get("items")) {
                    Video video = new Video(
                            item.get("id").get("videoId").asText(),
                            item.get("snippet").get("title").asText(),
                            item.get("snippet").get("channelId").asText(),
                            item.get("snippet").get("channelTitle").asText(),
                            item.get("snippet").get("description").asText(),
                            item.get("snippet").get("thumbnails").get("default").get("url").asText()
                    );
                    channelVideos.add(video);
                }


        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error with API response: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other Error: " + e.getMessage());
        }
        return channelVideos;
    }


    // CAN LATER ADD ANY FUNCTIONALITIES NEEDED


}
