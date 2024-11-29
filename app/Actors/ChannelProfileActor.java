package Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.Channel;
import models.SearchHistoryModel;
import models.Video;

import java.util.List;

/**
 * @author Sam Collin
 * Actor responsible for fetching and processing channel profile information and its videos.
 */
public class ChannelProfileActor extends AbstractActor {

    private final String channelId;
    private final ActorRef parent;
    private final SearchHistoryModel shModel;

    /**
     * @author Sam Collin
     * Constructor for the ChannelProfileActor.
     * @param channelId The ID of the channel to fetch information for.
     * @param parent The parent actor to send results to.
     * @param shModel The search history model for fetching channel data.
     */
    public ChannelProfileActor(String channelId, ActorRef parent, SearchHistoryModel shModel) {
        this.channelId = channelId;
        this.parent = parent;
        this.shModel = shModel;
    }

    /**
     * @author Sam Collin
     * Method to create Props for ChannelProfileActor.
     * @param channelId The ID of the channel to fetch information for.
     * @param parent The parent actor to send results to.
     * @param shModel The search history model for fetching channel data.
     * @return Props instance for creating the actor.
     */
    public static Props props(String channelId, ActorRef parent, SearchHistoryModel shModel) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(channelId, parent, shModel));
    }

    /**
     * @author Sam Collin
     * Defines the behavior of the actor by setting up message handlers.
     * @return The Receive object with message handling logic.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProcessing.class, msg -> {
                    fetchChannelProfile();
                })
                .build();
    }

    /**
     * @author Sam Collin
     * Initializes the actor by sending a StartProcessing message to itself.
     */
    @Override
    public void preStart() {
        self().tell(new StartProcessing(), self());
    }

    /**
     * @author Sam Collin
     * Fetches channel details and videos, sends the result to the parent actor, and stops the actor.
     */
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

    /**
     * @author Sam Collin
     * Marker message class to initiate processing in the actor.
     */
    public static class StartProcessing {}

    /**
     * @author Sam Collin
     * Represents the result of a channel profile request, containing the channel details and associated videos.
     */
    public static class ChannelProfileResult {
        public final Channel channel;
        public final List<Video> videos;

        public ChannelProfileResult(Channel channel, List<Video> videos) {
            this.channel = channel;
            this.videos = videos;
        }
    }
}
