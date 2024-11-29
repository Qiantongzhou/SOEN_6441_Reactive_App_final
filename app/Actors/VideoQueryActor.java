package Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import models.SearchHistoryModel;
import models.SearchResult;
import models.Video;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoQueryActor extends AbstractActor {
    private final String query;
    private final ActorRef parent;
    private final SearchHistoryModel shModel;
    private final Set<String> sentVideoIds = new HashSet<>();
    Cancellable cancellable;

    public VideoQueryActor(String query, ActorRef parent, SearchHistoryModel shModel) {
        this.query = query;
        this.parent = parent;
        this.shModel = shModel;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, t -> {
                    checkForNewVideos();
                })
                .build();
    }

    @Override
    public void preStart() {
        cancellable = getContext().system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(20, "seconds"),
                self(),
                new Tick(),
                getContext().system().dispatcher(),
                self()
        );
    }

    @Override
    public void postStop() {
            cancellable.cancel();

    }

    private void checkForNewVideos() {
        List<Video> videos = shModel.queryYoutubeWithNum(query, 10);
        List<Video> newVideos = new ArrayList<>();
        for (Video video : videos) {
            if (!sentVideoIds.contains(video.getVideoId())) {
                sentVideoIds.add(video.getVideoId());
                newVideos.add(video);
            }
        }
        if (!newVideos.isEmpty()) {
            parent.tell(new SearchResult(query,newVideos,""), self());
        }
    }

    public static class Tick { }
}
