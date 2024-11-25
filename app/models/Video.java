package models;

/**
 * @author Tomas Pereira
 * @author Tongzhou Qian
 *
 * Represents a Video
 * Stores the video id, title, channel id, channel name, description, and video thumbnail.
 */
public class Video {
    private String videoId;
    private String title;
    private String channelId;
    private String channelTitle;
    private String description;
    private String thumbnail;

    /**
     * @author Tongzhou Qian
     * @param videoId The video's ID
     * @param title The video's title
     * @param channelId The video's channel id
     * @param channelTitle The video's channel title
     * @param description The video's description
     * @param thumbnail The video's thumbnail
     */
    public Video(String videoId, String title, String channelId, String channelTitle, String description, String thumbnail) {
        this.videoId = videoId;
        this.title = title;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    /**
     * @author Tomas Pereira
     * Getter for the description
     * @return String for the video's description
     */
    public String getDescription(){
        return description;
    }

    /**
     * @author Tomas Pereira
     * Getter for the video id
     * @return String for the video ID
     */
    public String getVideoId() {return videoId;}

    /**
     * @author Tomas Pereira
     * Getter for the video title
     * @return String for the video title
     */
    public String getTitle() {return title;}

    /**
     * @author Tomas Pereira
     * Getter for the channel ID
     * @return String for the channel ID
     */
    public String getChannelId() {return channelId;}

    /**
     * @author Tomas Pereira
     * Getter for the channel title
     * @return String for the channel title
     */
    public String getChannelTitle() {return channelTitle;}

    /**
     * @author Tomas Pereira
     * Getter for the video thumbnail
     * @return String URL for the video thumbnail.
     */
    public String getThumbnail() {return thumbnail;}
}

