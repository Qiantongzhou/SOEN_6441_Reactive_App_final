package models;

import java.util.List;

/**
 * @author Tomas Pereira
 *
 * Represents a Search Result.
 * Stores the query used, list of videos returned, and the sentiment across the video descriptions.
 */
public class SearchResult {
    public String query;
    public List<Video> videos;
    public String sentiment;
    public SearchResult(String query, List<Video> videos, String sentiment) {
        this.query = query;
        this.videos = videos;
        this.sentiment = sentiment;
    }

}