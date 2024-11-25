package models;

/**
 * @author Sam Collin
 *
 * This class represents a Youtube Channel.
 * It stores information such as id, title, description, publishedAt, country, customUrl, thumbnailUrl, subscriberCount, hiddenSubscriberCount, viewCount and videoCount.
 * It also contains all the getters.
 *
 */
public class Channel {

    private String id;
    private String title;
    private String description;
    private String publishedAt;
    private String country;
    private String customUrl;
    private String thumbnailUrl;
    private int subscriberCount;
    private boolean hiddenSubscriberCount;
    private int viewCount;
    private int videoCount;


    /**
     * @author Sam Collin
     * Constructor for a {@link Channel} object.
     * @param id Identifies the channel.
     * @param title Channel's title
     * @param description Channel's description
     * @param publishedAt Channel's date of creation
     * @param country Channel's country of origin, if not, N/A
     * @param customUrl Channel's URL, if not, N/A
     * @param thumbnailUrl Channel's thumnail URL
     * @param subscriberCount Channel's number of subscriber
     * @param hiddenSubscriberCount Inform if the subscriber count is hidden or not.
     * @param viewCount Channel's general view number
     * @param videoCount Channel's total video number
     */
    public Channel(String id, String title, String description, String publishedAt, String country, String customUrl, String thumbnailUrl, int subscriberCount, boolean hiddenSubscriberCount, int viewCount, int videoCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publishedAt = publishedAt;
        this.country = country;
        this.customUrl = customUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.subscriberCount = subscriberCount;
        this.hiddenSubscriberCount = hiddenSubscriberCount;
        this.viewCount = viewCount;
        this.videoCount = videoCount;
    }

    /**
     * @author Sam Collin
     * Getter for the Id.
     * @return String for the channel's Id
     */
    public String getId() {
        return id;
    }

    /**
     * @author Sam Collin
     * Getter for the title.
     * @return String for the channel's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @author Sam Collin
     * Getter for the description.
     * @return String for the channel's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @author Sam Collin
     * Getter for the published date.
     * @return String for the channel's published date
     */
    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * @author Sam Collin
     * Getter for the country.
     * @return String for the channel's country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @author Sam Collin
     * Getter for the custom url.
     * @return String for the channel's customUrl
     */
    public String getCustomUrl() {
        return customUrl;
    }

    /**
     * @author Sam Collin
     * Getter for the thumbnail url.
     * @return String for the channel's thumbnail url
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @author Sam Collin
     * Getter for the subscriber count.
     * @return String for the channel's subscriber count
     */
    public int getSubscriberCount() {
        return subscriberCount;
    }

    /**
     * @author Sam Collin
     * Getter for the state of the subscriber count.
     * @return Boolean for the state of the subscriber count
     */
    public boolean getHiddenSubscriberCount() {
        return hiddenSubscriberCount;
    }

    /**
     * @author Sam Collin
     * Getter for the view count.
     * @return String for the view count
     */
    public int getViewCount() {
        return viewCount;
    }

    /**
     * @author Sam Collin
     * Getter for the video count.
     * @return String for the video count
     */
    public int getVideoCount() {
        return videoCount;
    }
}
