package Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.SearchHistoryModel;
import java.util.Map;
import static models.WordStats.generateWordStats;

public class WordStatsActor extends AbstractActor {
    private final String query;
    private final ActorRef parent;
    private final SearchHistoryModel shModel;

    public static class WordStatsResult {
        public final String query;
        public final Map<String, Long> wordStats;

        public WordStatsResult(String query, Map<String, Long> wordStats) {
            this.query = query;
            this.wordStats = wordStats;
        }
    }
    public WordStatsActor(String query, ActorRef parent, SearchHistoryModel shModel) {
        this.query = query;
        this.parent = parent;
        this.shModel = shModel;
    }

    public static Props props(String query, ActorRef parent, SearchHistoryModel shModel) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(query, parent, shModel));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProcessing.class, msg -> {
                    getWordStats();
                })
                .build();
    }

    @Override
    public void preStart() {
        self().tell(new StartProcessing(), self());
    }

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

    // Message classes
    public static class StartProcessing { }
}

