//package Actors;
//
//import akka.actor.AbstractActor;
//import akka.actor.Props;
//import akka.actor.ActorRef;
//import models.SearchHistoryModel;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class SearchActor extends AbstractActor {
//    private final String query;
//    private final SearchHistoryModel shModel;
//    private final Set<String> seenResults = new HashSet<>();
//    private final ActorRef out;
//
//    public static Props props(String query, SearchHistoryModel shModel) {
//        return Props.create(SearchActor.class, () -> new SearchActor(query, shModel));
//    }
//
//    public SearchActor(String query, SearchHistoryModel shModel) {
//        this.query = query;
//        this.shModel = shModel;
//        this.out = getSender();
//        // Start with the latest 10 results
//        shModel.queryYoutube(query, 10).forEach(this::sendIfNotSeen);
//    }
//
//    @Override
//    public Receive createReceive() {
//        return receiveBuilder()
//                .match(NewSearchResult.class, this::handleNewSearchResult)
//                .matchAny(msg -> unhandled(msg))
//                .build();
//    }
//
//    private void handleNewSearchResult(NewSearchResult result) {
//        if (!seenResults.contains(result.getVideoId())) {
//            sendIfNotSeen(result);
//        }
//    }
//
//    private void sendIfNotSeen(Video video) {
//        if (seenResults.add(video.getVideoId())) {
//            out.tell(video.toJson(), getSelf());
//        }
//    }
//
//    @Override
//    public void postStop() {
//        // Cleanup logic if needed
//    }
//
//    public static class Completed {
//        public static final Completed INSTANCE = new Completed();
//    }
//}

