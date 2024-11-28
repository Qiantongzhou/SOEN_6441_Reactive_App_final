package Actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.SubmissionSentiment;
import models.Video;

import java.util.List;

public class SubmissionSentimentActor extends AbstractActor {

    private final SubmissionSentiment sentimentAnalyzer;

    /**
     * @author Tomas Pereira
     *
     * Constructor for the SubmissionSentimentActor
     */
    public SubmissionSentimentActor(){
        this.sentimentAnalyzer = new SubmissionSentiment();
    }

    /**
     * @author Tomas Pereira
     * @return Properties for the SubmissionSentimentActor
     */
    public static Props props(){
        return Props.create(SubmissionSentimentActor.class);
    }

    /**
     * @author Tomas Pereira
     * @return The Receive object which matches the message received. In this case, always AnalyzeSentiment.
     *
     * Overload of the createReceive method which allows the actor to wait for and receive messages.
     */
    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(AnalyzeSentiment.class, this::handleAnalyzeSentiment)
                .build();
    }

    /**
     * @author Tomas Pereira
     * @param request Message of type AnalyzeSentiment
     *
     * Method call handing requests of type AnalyzeSentiment.
     * Calls the determineSentiment method of the SubmissionSentiment class to perform analysis.
     * Send the returned sentiment string back to the message sender.
     */
    private void handleAnalyzeSentiment(AnalyzeSentiment request){
        List<Video> videos = request.getVideos();
        String sentiment = sentimentAnalyzer.determineSentiment(videos);
        getSender().tell(new SentimentResult(sentiment, videos), self());
    }

    /**
     * @author Tomas Pereira
     *
     * Inner class for processing the sentiment for a list of videos
     */
    public static class AnalyzeSentiment {
        private final List<Video> videos;

        public AnalyzeSentiment(List<Video> videos){
            this.videos = videos;
        }

        public List<Video> getVideos(){
            return videos;
        }
    }

    /**
     * @author Tomas Pereira
     *
     * Inner class for sending the result of sentiment analysis.
     * Contains both the sentiment and list of videos in order to send them jointly to the clientside.
     */
    public static class SentimentResult {
        private final String sentiment;
        private final List<Video> videos;

        public SentimentResult(String sentiment, List<Video> videos){
            this.sentiment = sentiment;
            this.videos = videos;
        }

        public String getSentiment(){
            return sentiment;
        }
        public List<Video> getVideos() {return videos;}
    }

}
