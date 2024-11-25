package models;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WordStats {

    //if to remove words, now we dont need
    private static final Set<String> ignore = Set.of();
    /**
     * @author Tongzhou Qian
     * Stream pipLine for handling Json format video info
     * @param videos video info returned by YouTube api formated in { \"items\": [" +
     *               {
     *                 "items": [
     *                   { "snippet": { "title": "First Video", "description": "First description." } },
     *                   { "snippet": { "title": "Second Video", "description": "Second description." } }
     *                 ]
     *               }
     */
    public static Map<String, Long> generateWordStats(JsonNode videos) {
        // Check if 'videos' contains 'items'
        if (videos == null || !videos.has("items")) {
            return Collections.emptyMap();
        }
        // Stream through each video item in the "items" array

        return StreamSupport.stream(videos.get("items").spliterator(), false)
                // Map each video item to a stream of words
                .flatMap(video -> {
                    // Get the "snippet"
                    JsonNode snippet = video.get("snippet");
                    // If "snippet" is null
                    if (snippet == null) return Stream.empty();
                    // Get the title and description
                    String title = snippet.has("title") ? snippet.get("title").asText() : "";
                    String description = snippet.has("description") ? snippet.get("description").asText() : "";
                    return Arrays.stream(normalizeText(title + " " + description));
                })
                // Filter out empty words
                .filter(word -> !word.isEmpty() && !ignore.contains(word))

                // Collect words into a map with word as key and frequency
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()))
                // Convert the map to a sorted stream
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                // Collect the sorted entries into a new LinkedHashMap
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static String[] normalizeText(String text) {
        return text.toLowerCase().split("\\W+");
    }
}
