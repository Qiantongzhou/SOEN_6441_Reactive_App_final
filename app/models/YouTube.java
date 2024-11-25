package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Tongzhou Qian
 * @author Sam Collin
 * Youtube class implements method to interact with the API of Youtube.
 * It allows to search for videos, and video details, channels and channel details.
 * It simplifies the interaction with the API and manage the response to get it in the right format.
 */
public class YouTube {

    private static final String API_KEY="AIzaSyBTdLng0J0bxQOYFhKhMrI23guTCVRI1xQ"; //Qian
    //private static final String API_KEY="AIzaSyDO584JNmQbEi6yDkuG_UgNVVAtF4vHclU";   //Tomas
    private static final String API_URL="https://www.googleapis.com/youtube/v3/";

    /**
     * @author Tongzhou Qian
     * @author Sam Collin
     * Retrieves the last {@code maxResults} videos posted related to a given query.
     * @param query Search query entered by the user. Only handles single word for now.
     * @param maxResults The number of videos we want to retrieve.
     * @return {@link JsonNode} having a list of videos from the channel given ordered from the most recent.
     * @throws Exception if an error occurs.
     */
    public JsonNode searchVideos(String query, int maxResults) throws Exception {
        // Ensures that the format of query is correct when using multiple keywords
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // Complete the url to make the request
        String url = API_URL + "search?part=snippet&q=" + encodedQuery + "&maxResults=" + maxResults + "&order=date&key=" + API_KEY;
        return makeRequest(url);
    }

    /**
     * @author Qian Tonghzhou
     * Given a channel ID, queries the Youtube API to return the details for that channel
     * @param channelId The string containing the channel ID to be queried
     * @return The JSON information for that channel
     * @throws Exception When the API call is bad
     */
    // Get channel details by channel ID
    public JsonNode getChannelDetails(String channelId) throws Exception {
        String url = API_URL + "channels?part=snippet,statistics&id=" + channelId + "&key=" + API_KEY;
        return makeRequest(url);
    }

    /**
     * @author Sam Collin
     * Retrieves the last {@code maxResults} videos posted by a given {@link Channel}.
     * @param channelId The channel where from the videos will be searched.
     * @param maxResults The number of videos we want to retrieve.
     * @return {@link JsonNode} having a list of videos from the channel given ordered from the most recent.
     * @throws Exception if there is an error.
     */
    public JsonNode getVideosByChannelId(String channelId, int maxResults) throws Exception {
        // Get video details only from the channel
        String url = API_URL + "search?part=snippet&channelId=" + channelId + "&maxResults=" + maxResults + "&order=date&key=" + API_KEY;
        return makeRequest(url);
    }

    private JsonNode makeRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the connection properties
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Get the response code
        int responseCode = connection.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            // Read the response line by line
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response JSON
            return Json.parse(response.toString());

    }
}
