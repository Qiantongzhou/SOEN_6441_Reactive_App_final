package Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.Channel;
import models.SearchHistoryModel;
import models.Video;

import java.util.List;

public class ChannelProfileActor extends AbstractActor {

    private final String channelId;
    private final ActorRef parent;
    private final SearchHistoryModel shModel;

    public ChannelProfileActor(String channelId, ActorRef parent, SearchHistoryModel shModel) {
        this.channelId = channelId;
        this.parent = parent;
        this.shModel = shModel;
    }

    public static Props props(String channelId, ActorRef parent, SearchHistoryModel shModel) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(channelId, parent, shModel));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProcessing.class, msg -> {
                    fetchChannelProfile();
                })
                .build();
    }

    @Override
    public void preStart() {
        self().tell(new StartProcessing(), self());
    }

    private void fetchChannelProfile() {
        try {
            // Fetch channel details and videos
            Channel channel = shModel.getChannelDetails(channelId);
            List<Video> videos = shModel.getChannelVideos(channelId, 10);

            // Send the result to the parent actor
            parent.tell(new ChannelProfileResult(channel, videos), self());
        } catch (Exception e) {
            parent.tell(new akka.actor.Status.Failure(e), self());
        } finally {
            getContext().stop(self());
        }
    }

    public static class StartProcessing {}
    public static class ChannelProfileResult {
        public final Channel channel;
        public final List<Video> videos;

        public ChannelProfileResult(Channel channel, List<Video> videos) {
            this.channel = channel;
            this.videos = videos;
        }
    }
}
