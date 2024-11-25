package controllers;

import models.Channel;
import models.Video;
import models.SearchHistoryModel;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.wordstats;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static models.WordStats.generateWordStats;

/**
 * Controller class for handling search-related operations.
 * This class manages video searches, displays word statistics,
 * and shows channel profiles using asynchronous operations.
 *
 * @Author: Tongzhou Qian
 * @Author: Sam Collin
 * @Author: Tomas Pereira
 */
public class SearchController extends Controller {
    private final SearchHistoryModel shModel;

    /**
     * Constructs a new SearchController with the specified SearchHistoryModel.
     *
     * @param shModel the SearchHistoryModel instance used for querying and storing search data
     */
    @Inject
    public SearchController(SearchHistoryModel shModel) {
        this.shModel = shModel;
    }

    /**
     * Renders the index page with an empty search history.
     *
     * @return a Result rendering the index page with an empty list
     */
    public Result index() {
        return ok(views.html.index.render(new LinkedList<>()));
    }

    /**
     * Handles the video search functionality.
     * Asynchronously processes the search query, stores the results,
     * and updates the search history displayed on the index page.
     *
     * @param query the search query string entered by the user
     * @return a CompletionStage of Result rendering the index page with updated search history
     */
    public CompletionStage<Result> searchVideos(String query) {
        // Asynchronously handle the query and response
        return CompletableFuture.supplyAsync(() -> {
            // Perform query and store operation
            shModel.queryAndStore(query);
            // Fetch the updated search history
            return shModel.getSearchHistory();
        }).thenApply(searchHistory -> ok(views.html.index.render(searchHistory)));
    }

    /**
     * Provides additional word statistics based on the search query.
     * Asynchronously retrieves and generates word statistics from the search results.
     *
     * @param query the search query string for which to generate statistics
     * @return a CompletionStage of Result rendering the word statistics page
     */
    public CompletionStage<Result> MoreStats(String query) {
        // Asynchronously handle the generation of word statistics
        return CompletableFuture.supplyAsync(() -> {
            return generateWordStats(shModel.queryYoutube(query, 50));
        }).thenApply(Morestats -> ok(wordstats.render(query, Morestats)));
    }

    /**
     * Displays the profile of a specific YouTube channel.
     * Asynchronously retrieves channel details and the latest videos for display.
     *
     * @param channelId the unique identifier of the YouTube channel
     * @return a CompletionStage of Result rendering the channel profile page with details and videos
     */
    public CompletionStage<Result> showChannelProfile(String channelId) {
        // Handle the asynchronous retrieval of channel information
        return CompletableFuture.supplyAsync(() -> {
            Channel channelDetails = shModel.getChannelDetails(channelId);
            List<Video> latestVideos = shModel.getChannelVideos(channelId, 10);
            return ok(views.html.channel.render(channelDetails, latestVideos));
        });
    }
}
