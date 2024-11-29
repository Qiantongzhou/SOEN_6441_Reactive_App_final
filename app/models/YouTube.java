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

    public static String[] apiKeys = {
            "AIzaSyBTdLng0J0bxQOYFhKhMrI23guTCVRI1xQ", // Qian
            "AIzaSyDHODjlC0o8VS9DE9KW40YofzxGN2tsk9M", // Tomas
            "AIzaSyDO584JNmQbEi6yDkuG_UgNVVAtF4vHclU",  // Additional
            "AIzaSyCfxCkaQRsxb5U7bFao_X7rynO5hYQ2qLU" // Sam perso
    };

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/";

    private int currentApiKeyIndex = 0; // Track which API key to use

    private final HttpClient httpClient;

    // Default constructor for production
    public YouTube() {
        this(new HttpUrlConnectionClient());
    }

    // Constructor for dependency injection (testing)
    public YouTube(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // Constructor for dependency injection with custom API keys (testing)
    public YouTube(HttpClient httpClient, String[] apiKeys) {
        this.httpClient = httpClient;
        this.apiKeys = apiKeys;
    }

    public JsonNode searchVideos(String query, int maxResults) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API_URL + "search?part=snippet&q=" + encodedQuery + "&maxResults=" + maxResults + "&order=date&key=";
        return tryRequestWithFallback(url);
    }

    public JsonNode getChannelDetails(String channelId) throws Exception {
        String url = API_URL + "channels?part=snippet,statistics&id=" + channelId + "&key=";
        return tryRequestWithFallback(url);
    }

    public JsonNode getVideosByChannelId(String channelId, int maxResults) throws Exception {
        String url = API_URL + "search?part=snippet&channelId=" + channelId + "&maxResults=" + maxResults + "&order=date&key=";
        return tryRequestWithFallback(url);
    }

    private JsonNode tryRequestWithFallback(String urlWithoutKey) throws Exception {
        Exception lastException = null;

        for (int i = 0; i < apiKeys.length; i++) {
            String apiKey = apiKeys[currentApiKeyIndex];
            String url = urlWithoutKey + apiKey;

            try {
                String response = httpClient.get(url);
                return Json.parse(response);
            } catch (Exception e) {
                lastException = e;
                System.out.println("API key failed: " + apiKey + ". Trying next one.");
                // Move to the next API key
                currentApiKeyIndex = (currentApiKeyIndex + 1) % apiKeys.length;
            }
        }

        // If all API keys fail, return a failure response
        if (lastException != null) {
            return Json.parse("{\"error\":\"All API keys failed: " + lastException.getMessage() + "\"}");
        }

        return Json.parse("{\"error\":\"Unknown failure occurred.\"}");
    }
}
