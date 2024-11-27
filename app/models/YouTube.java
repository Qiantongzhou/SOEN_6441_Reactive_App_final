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
 * YouTube class implements methods to interact with the API of YouTube.
 * It allows searching for videos, video details, channels, and channel details.
 * It simplifies the interaction with the API and manages the response to get it in the right format.
 */
public class YouTube {

    private static final String[] API_KEYS = {
            "AIzaSyBTdLng0J0bxQOYFhKhMrI23guTCVRI1xQ", // Qian
            "AIzaSyDHODjlC0o8VS9DE9KW40YofzxGN2tsk9M", // Tomas
            "AIzaSyDO584JNmQbEi6yDkuG_UgNVVAtF4vHclU"  // Additional
    };

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/";

    private int currentApiKeyIndex = 0; // Track which API key to use

    /**
     * Retrieves the last {@code maxResults} videos posted related to a given query.
     * @param query Search query entered by the user. Only handles single words for now.
     * @param maxResults The number of videos we want to retrieve.
     * @return {@link JsonNode} having a list of videos from the channel given ordered from the most recent.
     * @throws Exception if an error occurs.
     */
    public JsonNode searchVideos(String query, int maxResults) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API_URL + "search?part=snippet&q=" + encodedQuery + "&maxResults=" + maxResults + "&order=date&key=";
        return tryRequestWithFallback(url);
    }

    /**
     * Given a channel ID, queries the YouTube API to return the details for that channel.
     * @param channelId The string containing the channel ID to be queried.
     * @return The JSON information for that channel.
     * @throws Exception When the API call fails.
     */
    public JsonNode getChannelDetails(String channelId) throws Exception {
        String url = API_URL + "channels?part=snippet,statistics&id=" + channelId + "&key=";
        return tryRequestWithFallback(url);
    }

    /**
     * Retrieves the last {@code maxResults} videos posted by a given {@link Channel}.
     * @param channelId The channel where from the videos will be searched.
     * @param maxResults The number of videos we want to retrieve.
     * @return {@link JsonNode} having a list of videos from the channel given ordered from the most recent.
     * @throws Exception if there is an error.
     */
    public JsonNode getVideosByChannelId(String channelId, int maxResults) throws Exception {
        String url = API_URL + "search?part=snippet&channelId=" + channelId + "&maxResults=" + maxResults + "&order=date&key=";
        return tryRequestWithFallback(url);
    }

    private JsonNode tryRequestWithFallback(String urlWithoutKey) throws Exception {
        Exception lastException = null;

        for (int i = 0; i < API_KEYS.length; i++) {
            String apiKey = API_KEYS[currentApiKeyIndex];
            String url = urlWithoutKey + apiKey;

            try {
                return makeRequest(url);
            } catch (Exception e) {
                lastException = e;
                System.out.println("API Do not work: "+API_KEYS[currentApiKeyIndex]+" trying next one");
                // Move to the next API key
                currentApiKeyIndex = (currentApiKeyIndex + 1) % API_KEYS.length;
            }
        }

        // If all API keys fail, return a failure response
        if (lastException != null) {
            return Json.parse("{\"error\":\"All API keys failed: " + lastException.getMessage() + "\"}");
        }

        return Json.parse("{\"error\":\"Unknown failure occurred.\"}");
    }

    private JsonNode makeRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the connection properties
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Get the response code
        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
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
        } else {
            throw new Exception("Failed with HTTP code: " + responseCode);
        }
    }
}
