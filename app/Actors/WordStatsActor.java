package Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.SearchHistoryModel;
import java.util.Map;
import static models.WordStats.generateWordStats;

/**
 * Actor responsible for processing word statistics based on a query.
 * It interacts with a parent actor to send back the results.
 */
public class WordStatsActor extends AbstractActor {
    private final String query;
    private final ActorRef parent;
    private final SearchHistoryModel shModel;

    /**
     * Represents the result of word statistics processing.
     */
    public static class WordStatsResult {
        public final String query;
        public final Map<String, Long> wordStats;

        /**
         * Constructs a WordStatsResult.
         *
         * @param query the query string.
         * @param wordStats the word statistics as a map.
         */
        public WordStatsResult(String query, Map<String, Long> wordStats) {
            this.query = query;
            this.wordStats = wordStats;
        }
    }

    /**
     * Constructs a WordStatsActor.
     *
     * @param query the query string to process.
     * @param parent the parent actor to send results to.
     * @param shModel the search history model for querying video data.
     */
    public WordStatsActor(String query, ActorRef parent, SearchHistoryModel shModel) {
        this.query = query;
        this.parent = parent;
        this.shModel = shModel;
    }

    /**
     * Creates the Props for the WordStatsActor.
     *
     * @param query the query string to process.
     * @param parent the parent actor to send results to.
     * @param shModel the search history model for querying video data.
     * @return Props instance for creating the actor.
     */
    public static Props props(String query, ActorRef parent, SearchHistoryModel shModel) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(query, parent, shModel));
    }

    /**
     * Defines the behavior of the actor.
     *
     * @return the Receive instance describing the actor's behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProcessing.class, msg -> {
                    getWordStats();
                })
                .build();
    }

    /**
     * Lifecycle method called before the actor starts.
     * Sends a message to itself to start processing.
     */
    @Override
    public void preStart() {
        self().tell(new StartProcessing(), self());
    }

    /**
     * Processes the query to generate word statistics.
     * Communicates the result or any errors back to the parent actor.
     */
    private void getWordStats() {
        try {
            // Fetch videos related to the query
            Map<String, Long> wordStats = generateWordStats(shModel.queryYoutube(query, 50));

            // Send the word stats back to the parent actor (UserActor)
            parent.tell(new WordStatsResult(query, wordStats), self());
        } catch (Exception e) {
            // Handle exceptions if necessary
            parent.tell(new akka.actor.Status.Failure(e), self());
        } finally {
            // Stop the actor after processing
            getContext().stop(self());
        }
    }

    /**
     * Message class to signal the start of processing.
     */
    public static class StartProcessing { }
}
